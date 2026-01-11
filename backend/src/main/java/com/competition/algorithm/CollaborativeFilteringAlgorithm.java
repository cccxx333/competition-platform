package com.competition.algorithm;

import com.competition.entity.*;
import com.competition.repository.UserBehaviorRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CollaborativeFilteringAlgorithm {

    private final UserBehaviorRepository userBehaviorRepository;
    private final UserRepository userRepository;

    /**
     * 基于用户的协同过滤推荐竞赛
     */
    public Map<Long, Double> calculateCompetitionRecommendation(Long userId, List<Competition> competitions) {
        // 获取用户行为矩阵
        Map<Long, Map<Long, Double>> userCompetitionMatrix = buildUserCompetitionMatrix();

        // 找到相似用户
        Map<Long, Double> similarUsers = findSimilarUsers(userId, userCompetitionMatrix);

        // 基于相似用户推荐竞赛
        Map<Long, Double> recommendations = new HashMap<>();

        for (Competition competition : competitions) {
            double score = calculateCompetitionScore(userId, competition.getId(),
                    similarUsers, userCompetitionMatrix);
            recommendations.put(competition.getId(), score);
        }

        return recommendations;
    }

    /**
     * 基于协同过滤推荐队伍
     */
    public Map<Long, Double> calculateTeamRecommendation(Long userId, List<Team> teams) {
        Map<Long, Map<Long, Double>> userTeamMatrix = buildUserTeamMatrix();
        Map<Long, Double> similarUsers = findSimilarUsers(userId, userTeamMatrix);

        Map<Long, Double> recommendations = new HashMap<>();

        for (Team team : teams) {
            double score = calculateTeamScore(userId, team.getId(),
                    similarUsers, userTeamMatrix);
            recommendations.put(team.getId(), score);
        }

        return recommendations;
    }

    /**
     * 构建用户-竞赛行为矩阵
     */
    private Map<Long, Map<Long, Double>> buildUserCompetitionMatrix() {
        List<UserBehavior> behaviors = userBehaviorRepository
                .findByTargetType(UserBehavior.TargetType.COMPETITION);

        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            Long userId = behavior.getUser().getId();
            Long competitionId = behavior.getTargetId();

            matrix.computeIfAbsent(userId, k -> new HashMap<>());

            // 根据行为类型给予不同权重
            double weight = getActionWeight(behavior.getBehaviorType());
            matrix.get(userId).merge(competitionId, weight, Double::sum);
        }

        // 归一化处理
        normalizeMatrix(matrix);

        return matrix;
    }

    /**
     * 构建用户-队伍行为矩阵
     */
    private Map<Long, Map<Long, Double>> buildUserTeamMatrix() {
        List<UserBehavior> behaviors = userBehaviorRepository
                .findByTargetType(UserBehavior.TargetType.TEAM);

        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            Long userId = behavior.getUser().getId();
            Long teamId = behavior.getTargetId();

            matrix.computeIfAbsent(userId, k -> new HashMap<>());

            double weight = getActionWeight(behavior.getBehaviorType());
            matrix.get(userId).merge(teamId, weight, Double::sum);
        }

        normalizeMatrix(matrix);

        return matrix;
    }

    /**
     * 找到相似用户
     */
    private Map<Long, Double> findSimilarUsers(Long targetUserId,
                                               Map<Long, Map<Long, Double>> matrix) {
        Map<Long, Double> targetUserVector = matrix.get(targetUserId);
        if (targetUserVector == null || targetUserVector.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, Double> similarities = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            Long userId = entry.getKey();
            if (userId.equals(targetUserId)) {
                continue;
            }

            Map<Long, Double> userVector = entry.getValue();
            double similarity = calculatePearsonCorrelation(targetUserVector, userVector);

            if (similarity > 0.1) { // 只保留相似度较高的用户
                similarities.put(userId, similarity);
            }
        }

        // 返回前K个最相似的用户
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(50)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 计算竞赛推荐分数
     */
    private double calculateCompetitionScore(Long userId, Long competitionId,
                                             Map<Long, Double> similarUsers,
                                             Map<Long, Map<Long, Double>> matrix) {
        Map<Long, Double> userVector = matrix.get(userId);

        // 如果用户已经对该竞赛有行为，降低推荐分数
        if (userVector != null && userVector.containsKey(competitionId)) {
            return userVector.get(competitionId) * 0.5;
        }

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (Map.Entry<Long, Double> entry : similarUsers.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();

            Map<Long, Double> similarUserVector = matrix.get(similarUserId);
            if (similarUserVector != null && similarUserVector.containsKey(competitionId)) {
                weightedSum += similarity * similarUserVector.get(competitionId);
                similaritySum += Math.abs(similarity);
            }
        }

        return similaritySum > 0 ? weightedSum / similaritySum : 0.0;
    }

    /**
     * 计算队伍推荐分数
     */
    private double calculateTeamScore(Long userId, Long teamId,
                                      Map<Long, Double> similarUsers,
                                      Map<Long, Map<Long, Double>> matrix) {
        return calculateCompetitionScore(userId, teamId, similarUsers, matrix);
    }

    /**
     * 计算皮尔逊相关系数
     */
    private double calculatePearsonCorrelation(Map<Long, Double> vector1,
                                               Map<Long, Double> vector2) {
        Set<Long> commonKeys = new HashSet<>(vector1.keySet());
        commonKeys.retainAll(vector2.keySet());

        if (commonKeys.size() < 2) {
            return 0.0;
        }

        double sum1 = 0.0, sum2 = 0.0;
        double sum1Sq = 0.0, sum2Sq = 0.0;
        double pSum = 0.0;

        for (Long key : commonKeys) {
            double val1 = vector1.get(key);
            double val2 = vector2.get(key);

            sum1 += val1;
            sum2 += val2;
            sum1Sq += val1 * val1;
            sum2Sq += val2 * val2;
            pSum += val1 * val2;
        }

        int n = commonKeys.size();
        double numerator = pSum - (sum1 * sum2 / n);
        double denominator = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));

        return denominator == 0 ? 0 : numerator / denominator;
    }

    /**
     * 获取行为权重
     */
    private double getActionWeight(UserBehavior.BehaviorType behaviorType) {
        switch (behaviorType) {
            case VIEW: return 1.0;
            case LIKE: return 2.0;
            case APPLY: return 3.0;
            case JOIN: return 5.0;
            default: return 1.0;
        }
    }

    /**
     * 矩阵归一化
     */
    private void normalizeMatrix(Map<Long, Map<Long, Double>> matrix) {
        for (Map<Long, Double> userVector : matrix.values()) {
            double max = userVector.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            if (max > 0) {
                userVector.replaceAll((k, v) -> v / max);
            }
        }
    }
}