package com.competition.service;

import com.competition.algorithm.ContentBasedAlgorithm;
import com.competition.dto.TeamRecommendReason;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.entity.Team;
import com.competition.entity.TeamSkill;
import com.competition.entity.UserSkill;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.TeamSkillRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final CompetitionSkillRepository competitionSkillRepository;
    private final TeamSkillRepository teamSkillRepository;

    private final UserRepository userRepository; // 添加

    private final UserSkillRepository userSkillRepository;


    // 混合推荐算法权重

    /**
     * 为用户推荐竞赛
     */
    /**
     * 为用户推荐竞赛
     */
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

    public Map<Long, Double> calculateTeamMatchScores(Long userId, List<Team> teams) {
        if (!canRecommendForUser(userId) || teams == null || teams.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> userSkillLevels = buildUserSkillLevelMap(userId);
        if (userSkillLevels.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Double> scores = new HashMap<>();
        for (Team team : teams) {
            Map<Long, Integer> teamSkillWeights = buildTeamSkillWeightMap(team);
            double score = teamSkillWeights.isEmpty()
                    ? 0.0
                    : contentBasedAlgorithm.calculateSkillCosineSimilarity(userSkillLevels, teamSkillWeights);
            scores.put(team.getId(), score);
        }
        return scores;
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

    public List<TeamRecommendReason> buildTeamRecommendReasons(Long userId, Team team) {
        if (userId == null || team == null) {
            return List.of();
        }
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        if (userSkills.isEmpty()) {
            return List.of();
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

        List<TeamSkill> teamSkills = teamSkillRepository.findByTeam_Id(team.getId());
        if (teamSkills.isEmpty()) {
            return List.of();
        }

        List<SkillContribution> contributions = new ArrayList<>();
        for (TeamSkill teamSkill : teamSkills) {
            if (teamSkill.getSkill() == null) {
                continue;
            }
            Long skillId = teamSkill.getSkill().getId();
            if (skillId == null) {
                continue;
            }
            Integer level = userSkillLevels.get(skillId);
            if (level == null) {
                continue;
            }
            int weight = teamSkill.getWeight() != null ? teamSkill.getWeight() : 1;
            int contribution = level * weight;
            String name = teamSkill.getSkill().getName();
            if (name == null) {
                name = userSkillNames.get(skillId);
            }
            contributions.add(new SkillContribution(skillId, name, level, weight, contribution));
        }

        if (contributions.isEmpty()) {
            return List.of();
        }

        contributions.sort(Comparator.comparingInt(SkillContribution::getContribution).reversed());
        List<SkillContribution> topContributions = contributions.subList(0, Math.min(3, contributions.size()));

        return topContributions.stream()
                .map(contribution -> {
                    TeamRecommendReason reason = new TeamRecommendReason();
                    reason.setSkillId(contribution.getSkillId());
                    reason.setSkillName(contribution.getName());
                    reason.setWeight(contribution.getWeight());
                    return reason;
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Integer> buildUserSkillLevelMap(Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        Map<Long, Integer> levels = new HashMap<>();
        for (UserSkill userSkill : userSkills) {
            if (userSkill.getSkill() == null) {
                continue;
            }
            Long skillId = userSkill.getSkill().getId();
            if (skillId == null) {
                continue;
            }
            int level = userSkill.getProficiency() != null ? userSkill.getProficiency() : 1;
            levels.merge(skillId, level, Integer::max);
        }
        return levels;
    }

    private Map<Long, Integer> buildTeamSkillWeightMap(Team team) {
        List<TeamSkill> teamSkills = teamSkillRepository.findByTeam_Id(team.getId());
        Map<Long, Integer> weights = new HashMap<>();
        for (TeamSkill teamSkill : teamSkills) {
            if (teamSkill.getSkill() == null) {
                continue;
            }
            Long skillId = teamSkill.getSkill().getId();
            if (skillId == null) {
                continue;
            }
            int weight = teamSkill.getWeight() != null ? teamSkill.getWeight() : 1;
            weights.merge(skillId, weight, Integer::max);
        }
        return weights;
    }

    private static final class SkillContribution {
        private final Long skillId;
        private final String name;
        private final int level;
        private final int weight;
        private final int contribution;

        private SkillContribution(String name, int level, int weight, int contribution) {
            this(null, name, level, weight, contribution);
        }

        private SkillContribution(Long skillId, String name, int level, int weight, int contribution) {
            this.skillId = skillId;
            this.name = name == null ? "Skill" : name;
            this.level = level;
            this.weight = weight;
            this.contribution = contribution;
        }

        private Long getSkillId() {
            return skillId;
        }

        private String getName() {
            return name;
        }

        private int getLevel() {
            return level;
        }

        private int getWeight() {
            return weight;
        }

        private int getContribution() {
            return contribution;
        }

        private String format() {
            return String.format("%s(%dx%d)", name, level, weight);
        }
    }
}
