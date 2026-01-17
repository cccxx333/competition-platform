package com.competition.service;

import com.competition.algorithm.CollaborativeFilteringAlgorithm;
import com.competition.algorithm.ContentBasedAlgorithm;
import com.competition.dto.RecommendationResult;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.entity.UserSkill;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ContentBasedAlgorithm contentBasedAlgorithm;
    private final CollaborativeFilteringAlgorithm collaborativeFilteringAlgorithm;
    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final TeamService teamService;

    private final UserRepository userRepository; // 添加

    private final UserSkillRepository userSkillRepository;


    // 混合推荐算法权重
    private static final double CONTENT_WEIGHT = 0.6;
    private static final double COLLABORATIVE_WEIGHT = 0.4;

    /**
     * 为用户推荐竞赛
     */
    /**
     * 为用户推荐竞赛
     */
    public List<RecommendationResult<Competition>> recommendCompetitions(Long userId, int limit) {
        log.info("开始为用户 {} 推荐竞赛", userId);

        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("找到用户: {}", user.getUsername());

        List<Competition> allCompetitions = competitionRepository.findAvailableCompetitions(LocalDate.now());
        log.info("可用竞赛数量: {}", allCompetitions.size());

        if (allCompetitions.isEmpty()) {
            log.warn("没有可用的竞赛");
            return new ArrayList<>();
        }

        // 检查用户技能
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        log.info("用户技能数量: {}", userSkills.size());

        try {
            // 基于内容的推荐
            Map<Long, Double> contentScores = contentBasedAlgorithm
                    .calculateCompetitionSimilarity(userId, allCompetitions);
            log.info("内容推荐分数数量: {}", contentScores.size());

            // 协同过滤推荐
            Map<Long, Double> collaborativeScores = collaborativeFilteringAlgorithm
                    .calculateCompetitionRecommendation(userId, allCompetitions);
            log.info("协同过滤分数数量: {}", collaborativeScores.size());

            // 混合推荐算法
            List<RecommendationResult<Competition>> results = allCompetitions.stream()
                    .map(competition -> {
                        double contentScore = contentScores.getOrDefault(competition.getId(), 0.0);
                        double collaborativeScore = collaborativeScores.getOrDefault(competition.getId(), 0.0);

                        double finalScore = CONTENT_WEIGHT * contentScore +
                                COLLABORATIVE_WEIGHT * collaborativeScore;

                        log.debug("竞赛 {} - 内容分数: {}, 协同分数: {}, 最终分数: {}",
                                competition.getName(), contentScore, collaborativeScore, finalScore);

                        return new RecommendationResult<>(competition, finalScore,
                                buildCompetitionExplanation(contentScore, collaborativeScore));
                    })
                    .filter(result -> result.getScore() > 0.01) // 降低阈值
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("最终推荐结果数量: {}", results.size());
            return results;

        } catch (Exception e) {
            log.error("推荐竞赛时发生错误: ", e);
            // 如果推荐算法失败，返回随机推荐
            return allCompetitions.stream()
                    .limit(Math.min(limit, allCompetitions.size()))
                    .map(competition -> new RecommendationResult<>(competition, 0.5, "随机推荐"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 为用户推荐队伍
     */
    /**
     * 为用户推荐队伍
     */
    public List<RecommendationResult<Team>> recommendTeams(Long userId, int limit) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Team> availableTeams = teamService.getAvailableTeams();

        if (availableTeams.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 基于内容的推荐（技能匹配）
            Map<Long, Double> contentScores = contentBasedAlgorithm
                    .calculateTeamSimilarity(userId, availableTeams);

            // 协同过滤推荐
            Map<Long, Double> collaborativeScores = collaborativeFilteringAlgorithm
                    .calculateTeamRecommendation(userId, availableTeams);

            // 混合推荐算法
            List<RecommendationResult<Team>> results = availableTeams.stream()
                    .map(team -> {
                        double contentScore = contentScores.getOrDefault(team.getId(), 0.0);
                        double collaborativeScore = collaborativeScores.getOrDefault(team.getId(), 0.0);

                        double finalScore = CONTENT_WEIGHT * contentScore +
                                COLLABORATIVE_WEIGHT * collaborativeScore;

                        return new RecommendationResult<>(team, finalScore,
                                buildTeamExplanation(contentScore, collaborativeScore));
                    })
                    .filter(result -> result.getScore() > 0.01)
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            return results;

        } catch (Exception e) {
            log.error("推荐队伍时发生错误: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * 为队伍推荐合适的成员
     */
    public List<RecommendationResult<User>> recommendTeamMembers(Long teamId, int limit) {
        Team team = teamService.getTeamById(teamId);
        List<User> candidateUsers = teamService.getCandidateUsers(teamId);

        // 基于技能匹配的推荐
        Map<Long, Double> skillMatchScores = contentBasedAlgorithm
                .calculateUserTeamMatch(team, candidateUsers);

        List<RecommendationResult<User>> results = candidateUsers.stream()
                .map(user -> {
                    double score = skillMatchScores.getOrDefault(user.getId(), 0.0);
                    return new RecommendationResult<>(user, score,
                            "技能匹配度: " + String.format("%.2f", score));
                })
                .filter(result -> result.getScore() > 0.1)
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(limit)
                .collect(Collectors.toList());

        return results;
    }

    private String buildCompetitionExplanation(double contentScore, double collaborativeScore) {
        StringBuilder explanation = new StringBuilder();
        if (contentScore > 0.5) {
            explanation.append("技能匹配度高 ");
        }
        if (collaborativeScore > 0.5) {
            explanation.append("相似用户也感兴趣 ");
        }
        return explanation.toString().trim();
    }

    private String buildTeamExplanation(double contentScore, double collaborativeScore) {
        StringBuilder explanation = new StringBuilder();
        if (contentScore > 0.5) {
            explanation.append("技能互补性强 ");
        }
        if (collaborativeScore > 0.5) {
            explanation.append("合作潜力大 ");
        }
        return explanation.toString().trim();
    }
    public String getRecommendFallbackReason(Long userId) {
        if (userId == null) {
            return "NO_LOGIN";
        }
        if (!userRepository.existsById(userId)) {
            return "NO_LOGIN";
        }
        Long skillCount = userSkillRepository.countByUserId(userId);
        if (skillCount == null || skillCount <= 0) {
            return "NO_SKILLS";
        }
        return null;
    }

    public boolean canRecommendForUser(Long userId) {
        return getRecommendFallbackReason(userId) == null;
    }

    public Map<Long, Double> calculateCompetitionMatchScores(Long userId, List<Competition> competitions) {
        if (!canRecommendForUser(userId) || competitions == null || competitions.isEmpty()) {
            return Collections.emptyMap();
        }
        return contentBasedAlgorithm.calculateCompetitionSimilarity(userId, competitions);
    }

    public String buildCompetitionRecommendReason(Long userId, Competition competition) {
        if (userId == null || competition == null) {
            return null;
        }
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        if (userSkills.isEmpty()) {
            return "Matched: none";
        }

        Map<Long, Integer> userSkillLevels = new HashMap<>();
        Map<Long, String> userSkillNames = new HashMap<>();
        for (UserSkill userSkill : userSkills) {
            if (userSkill.getSkill() == null) {
                continue;
            }
            Long skillId = userSkill.getSkill().getId();
            if (skillId == null) {
                continue;
            }
            int level = userSkill.getProficiency() != null ? userSkill.getProficiency() : 1;
            userSkillLevels.merge(skillId, level, Integer::max);
            userSkillNames.put(skillId, userSkill.getSkill().getName());
        }

        List<CompetitionSkill> competitionSkills = competitionSkillRepository.findByCompetitionId(competition.getId());
        if (competitionSkills.isEmpty()) {
            return "Matched: none";
        }

        Map<Long, Integer> competitionImportance = new HashMap<>();
        Map<Long, String> competitionSkillNames = new HashMap<>();
        for (CompetitionSkill competitionSkill : competitionSkills) {
            if (competitionSkill.getSkill() == null) {
                continue;
            }
            Long skillId = competitionSkill.getSkill().getId();
            if (skillId == null) {
                continue;
            }
            int importance = competitionSkill.getImportance() != null ? competitionSkill.getImportance() : 1;
            competitionImportance.put(skillId, importance);
            competitionSkillNames.put(skillId, competitionSkill.getSkill().getName());
        }

        List<SkillContribution> contributions = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : competitionImportance.entrySet()) {
            Long skillId = entry.getKey();
            Integer importance = entry.getValue();
            Integer level = userSkillLevels.get(skillId);
            if (level != null) {
                int contrib = level * importance;
                String name = competitionSkillNames.getOrDefault(skillId, userSkillNames.get(skillId));
                contributions.add(new SkillContribution(name, level, importance, contrib));
            }
        }

        if (contributions.isEmpty()) {
            return "Matched: none";
        }

        contributions.sort(Comparator.comparingInt(SkillContribution::getContribution).reversed());
        List<SkillContribution> topContributions = contributions.subList(0, Math.min(3, contributions.size()));

        String reason = topContributions.stream()
                .map(SkillContribution::format)
                .collect(Collectors.joining(", ", "Matched: ", ""));

        if (reason.length() > 120) {
            return reason.substring(0, 117) + "...";
        }
        return reason;
    }

    private static final class SkillContribution {
        private final String name;
        private final int level;
        private final int importance;
        private final int contribution;

        private SkillContribution(String name, int level, int importance, int contribution) {
            this.name = name == null ? "Skill" : name;
            this.level = level;
            this.importance = importance;
            this.contribution = contribution;
        }

        private int getContribution() {
            return contribution;
        }

        private String format() {
            return String.format("%s(%dx%d)", name, level, importance);
        }
    }
}
