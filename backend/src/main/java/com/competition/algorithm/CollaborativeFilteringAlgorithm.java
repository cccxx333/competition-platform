package com.competition.algorithm;

import com.competition.entity.UserBehavior;
import com.competition.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CollaborativeFilteringAlgorithm {

    private final UserBehaviorRepository userBehaviorRepository;
    private static final int NEIGHBOR_K = 20;

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
