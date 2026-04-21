package com.competition.algorithm;

import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.entity.User;
import com.competition.entity.UserSkill;
import com.competition.repository.CompetitionSkillRepository;
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
    private final CompetitionSkillRepository competitionSkillRepository;

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

    private double calculateCompetitionSkillSimilarity(Map<Long, Integer> userSkills, Competition competition) {
        Map<Long, Integer> requiredSkills = getCompetitionSkillMap(competition);

        if (requiredSkills.isEmpty()) {
            return 0.0;
        }

        return calculateCosineSimilarity(userSkills, requiredSkills);
    }

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

    public double calculateSkillCosineSimilarity(Map<Long, Integer> vector1, Map<Long, Integer> vector2) {
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }
        return calculateCosineSimilarity(vector1, vector2);
    }

    private Map<Long, Integer> getUserSkillMap(User user) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
        return userSkills.stream()
                .collect(Collectors.toMap(
                        us -> us.getSkill().getId(),
                        UserSkill::getProficiency,
                        Integer::max
                ));
    }

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
