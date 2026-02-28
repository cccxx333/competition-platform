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
    private static final int NEIGHBOR_K = 20;

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

    public Map<Long, Double> scoreCompetitionsForUser(Long userId, int topK) {
        if (userId == null) {
            return Collections.emptyMap();
        }

        List<UserBehavior> userBehaviors = userBehaviorRepository.findByUserIdAndTargetType(
                userId, UserBehavior.TargetType.COMPETITION);
        if (userBehaviors.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Double> prefsU = aggregatePreferences(userBehaviors);
        if (prefsU.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserBehavior> overlapBehaviors = userBehaviorRepository.findByTargetTypeAndTargetIdIn(
                UserBehavior.TargetType.COMPETITION, prefsU.keySet());
        Set<Long> candidateUserIds = overlapBehaviors.stream()
                .map(behavior -> behavior.getUser() != null ? behavior.getUser().getId() : null)
                .filter(Objects::nonNull)
                .filter(otherId -> !otherId.equals(userId))
                .collect(Collectors.toSet());
        if (candidateUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserBehavior> candidateBehaviors = userBehaviorRepository.findByTargetTypeAndUserIdIn(
                UserBehavior.TargetType.COMPETITION, candidateUserIds);
        if (candidateBehaviors.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Map<Long, Double>> prefsByUser = aggregatePreferencesByUser(candidateBehaviors);
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : prefsByUser.entrySet()) {
            double sim = calculateCosineSimilarity(prefsU, entry.getValue());
            if (sim > 0.0) {
                similarities.put(entry.getKey(), sim);
            }
        }
        if (similarities.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map.Entry<Long, Double>> topNeighbors = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(NEIGHBOR_K)
                .collect(Collectors.toList());

        Map<Long, Double> weightedSum = new HashMap<>();
        Map<Long, Double> similaritySum = new HashMap<>();
        for (Map.Entry<Long, Double> neighbor : topNeighbors) {
            Long neighborId = neighbor.getKey();
            double sim = neighbor.getValue();
            Map<Long, Double> neighborPrefs = prefsByUser.get(neighborId);
            if (neighborPrefs == null || neighborPrefs.isEmpty()) {
                continue;
            }
            for (Map.Entry<Long, Double> itemEntry : neighborPrefs.entrySet()) {
                Long itemId = itemEntry.getKey();
                if (prefsU.containsKey(itemId)) {
                    continue;
                }
                double value = itemEntry.getValue();
                weightedSum.merge(itemId, sim * value, Double::sum);
                similaritySum.merge(itemId, Math.abs(sim), Double::sum);
            }
        }

        Map<Long, Double> rawScores = new HashMap<>();
        for (Map.Entry<Long, Double> entry : weightedSum.entrySet()) {
            double denom = similaritySum.getOrDefault(entry.getKey(), 0.0);
            if (denom > 0.0) {
                rawScores.put(entry.getKey(), entry.getValue() / denom);
            }
        }
        if (rawScores.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Double> normalized = normalizeScores(rawScores);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        if (topK > 0 && normalized.size() > topK) {
            return normalized.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(topK)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }

        return normalized;
    }

    private Map<Long, Double> aggregatePreferences(List<UserBehavior> behaviors) {
        Map<Long, Double> prefs = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior == null || behavior.getTargetId() == null) {
                continue;
            }
            double weight = behavior.getWeight() != null ? behavior.getWeight() : 1.0;
            prefs.merge(behavior.getTargetId(), weight, Double::sum);
        }
        return prefs;
    }

    private Map<Long, Map<Long, Double>> aggregatePreferencesByUser(List<UserBehavior> behaviors) {
        Map<Long, Map<Long, Double>> prefsByUser = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior == null || behavior.getUser() == null || behavior.getUser().getId() == null) {
                continue;
            }
            Long uid = behavior.getUser().getId();
            prefsByUser.computeIfAbsent(uid, key -> new HashMap<>());
            if (behavior.getTargetId() == null) {
                continue;
            }
            double weight = behavior.getWeight() != null ? behavior.getWeight() : 1.0;
            prefsByUser.get(uid).merge(behavior.getTargetId(), weight, Double::sum);
        }
        return prefsByUser;
    }

    private double calculateCosineSimilarity(Map<Long, Double> vector1, Map<Long, Double> vector2) {
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
        for (Double value : vector1.values()) {
            norm1 += value * value;
        }
        for (Double value : vector2.values()) {
            norm2 += value * value;
        }
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<Long, Double> normalizeScores(Map<Long, Double> scores) {
        if (scores.isEmpty()) {
            return Collections.emptyMap();
        }
        double min = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= min) {
            Map<Long, Double> normalized = new HashMap<>();
            for (Long key : scores.keySet()) {
                normalized.put(key, 0.0);
            }
            return normalized;
        }
        Map<Long, Double> normalized = new HashMap<>();
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            double value = (entry.getValue() - min) / (max - min);
            normalized.put(entry.getKey(), value);
        }
        return normalized;
    }
}
