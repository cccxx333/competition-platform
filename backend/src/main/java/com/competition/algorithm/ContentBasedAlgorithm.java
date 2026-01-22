package com.competition.algorithm;

import com.competition.entity.*;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContentBasedAlgorithm {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    private final TeamMemberRepository teamMemberRepository;

    private final CompetitionSkillRepository competitionSkillRepository;

    /**
     * 计算用户与竞赛的相似度（基于技能匹配）
     */
    public Map<Long, Double> calculateCompetitionSimilarity(Long userId, List<Competition> competitions) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new HashMap<>();
        }

        User user = userOpt.get();
        Map<Long, Integer> userSkillMap = getUserSkillMap(user);

        Map<Long, Double> similarities = new HashMap<>();

        for (Competition competition : competitions) {
            double similarity = calculateCompetitionSkillSimilarity(userSkillMap, competition);
            similarities.put(competition.getId(), similarity);
        }

        return similarities;
    }

    /**
     * 计算用户与队伍的相似度（基于技能互补性）
     */
    public Map<Long, Double> calculateTeamSimilarity(Long userId, List<Team> teams) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new HashMap<>();
        }

        User user = userOpt.get();
        Map<Long, Integer> userSkillMap = getUserSkillMap(user);

        Map<Long, Double> similarities = new HashMap<>();

        for (Team team : teams) {
            double similarity = calculateTeamSkillComplementarity(userSkillMap, team);
            similarities.put(team.getId(), similarity);
        }

        return similarities;
    }

    /**
     * 计算用户与队伍的匹配度（为队伍推荐成员使用）
     */
    public Map<Long, Double> calculateUserTeamMatch(Team team, List<User> users) {
        // 获取队伍当前成员的技能分布
        Map<Long, Integer> teamSkillMap = getTeamSkillMap(team);
        // 获取竞赛需要的技能
        Map<Long, Integer> requiredSkillMap = getCompetitionSkillMap(team.getCompetition());

        Map<Long, Double> matches = new HashMap<>();

        for (User user : users) {
            Map<Long, Integer> userSkillMap = getUserSkillMap(user);
            double match = calculateSkillComplementarity(teamSkillMap, userSkillMap, requiredSkillMap);
            matches.put(user.getId(), match);
        }

        return matches;
    }

    /**
     * 计算用户技能与竞赛要求的相似度
     */
    private double calculateCompetitionSkillSimilarity(Map<Long, Integer> userSkills, Competition competition) {
        Map<Long, Integer> requiredSkills = getCompetitionSkillMap(competition);

        if (requiredSkills.isEmpty()) {
            return 0.0;
        }

        return calculateCosineSimilarity(userSkills, requiredSkills);
    }

    /**
     * 计算用户技能与队伍的互补性
     */
    private double calculateTeamSkillComplementarity(Map<Long, Integer> userSkills, Team team) {
        Map<Long, Integer> teamSkills = getTeamSkillMap(team);
        Map<Long, Integer> requiredSkills = getCompetitionSkillMap(team.getCompetition());

        return calculateSkillComplementarity(teamSkills, userSkills, requiredSkills);
    }

    /**
     * 计算技能互补性
     */
    private double calculateSkillComplementarity(Map<Long, Integer> teamSkills,
                                                 Map<Long, Integer> userSkills,
                                                 Map<Long, Integer> requiredSkills) {
        if (requiredSkills.isEmpty()) {
            return 0.0;
        }

        double complementarityScore = 0.0;
        int totalRequiredSkills = requiredSkills.size();

        for (Map.Entry<Long, Integer> entry : requiredSkills.entrySet()) {
            Long skillId = entry.getKey();
            Integer importance = entry.getValue();

            int teamLevel = teamSkills.getOrDefault(skillId, 0);
            int userLevel = userSkills.getOrDefault(skillId, 0);

            // 如果用户在队伍缺乏的技能上有优势，给予更高分数
            if (userLevel > teamLevel) {
                complementarityScore += (userLevel - teamLevel) * importance / 5.0;
            }

            // 如果用户有队伍需要但缺乏的技能
            if (teamLevel == 0 && userLevel > 0) {
                complementarityScore += userLevel * importance / 5.0 * 1.5; // 加权
            }
        }

        return Math.min(complementarityScore / totalRequiredSkills, 1.0);
    }

    /**
     * 余弦相似度计算
     */
    private double calculateCosineSimilarity(Map<Long, Integer> vector1, Map<Long, Integer> vector2) {
        Set<Long> commonKeys = new HashSet<>(vector1.keySet());
        commonKeys.retainAll(vector2.keySet());

        if (commonKeys.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long key : commonKeys) {
            dotProduct += vector1.get(key) * vector2.get(key);
        }

        for (Integer value : vector1.values()) {
            norm1 += value * value;
        }

        for (Integer value : vector2.values()) {
            norm2 += value * value;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 复用余弦相似度用于外部技能向量匹配
     */
    public double calculateSkillCosineSimilarity(Map<Long, Integer> vector1, Map<Long, Integer> vector2) {
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }
        return calculateCosineSimilarity(vector1, vector2);
    }

    /**
     * 获取用户技能映射 - 使用Repository查询避免懒加载
     */
    private Map<Long, Integer> getUserSkillMap(User user) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
        return userSkills.stream()
                .collect(Collectors.toMap(
                        us -> us.getSkill().getId(),
                        UserSkill::getProficiency,
                        Integer::max
                ));
    }

    /**
     * 获取队伍技能映射 - 使用Repository查询
     */
    private Map<Long, Integer> getTeamSkillMap(Team team) {
        Map<Long, Integer> teamSkills = new HashMap<>();

        // 通过Repository查询队伍成员，避免懒加载
        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());

        for (TeamMember member : members) {
            List<UserSkill> memberSkills = userSkillRepository.findByUserId(member.getUser().getId());
            for (UserSkill userSkill : memberSkills) {
                teamSkills.merge(userSkill.getSkill().getId(), userSkill.getProficiency(), Integer::max);
            }
        }

        return teamSkills;
    }

    /**
     * 获取竞赛技能要求映射 - 使用Repository查询
     */
    private Map<Long, Integer> getCompetitionSkillMap(Competition competition) {
        List<CompetitionSkill> competitionSkills = competitionSkillRepository.findByCompetitionId(competition.getId());
        return competitionSkills.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getSkill().getId(),
                        CompetitionSkill::getImportance,
                        Integer::max
                ));
    }
}
