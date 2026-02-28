package com.competition.service;

import com.competition.entity.User;
import com.competition.entity.UserBehavior;
import com.competition.repository.UserBehaviorRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserBehaviorService {

    private static final int VIEW_WEIGHT = 1;
    private static final int APPLY_WEIGHT = 5;

    private final UserBehaviorRepository userBehaviorRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCompetitionView(Long userId, Long competitionId) {
        upsertBehavior(userId, competitionId, UserBehavior.BehaviorType.VIEW, VIEW_WEIGHT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCompetitionApply(Long userId, Long competitionId) {
        upsertBehavior(userId, competitionId, UserBehavior.BehaviorType.APPLY, APPLY_WEIGHT);
    }

    private void upsertBehavior(Long userId, Long competitionId, UserBehavior.BehaviorType behaviorType, int deltaWeight) {
        if (userId == null || competitionId == null) {
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        if (user.getRole() != User.Role.STUDENT) {
            return;
        }

        Optional<UserBehavior> existing = userBehaviorRepository
                .findFirstByUserIdAndTargetTypeAndTargetIdAndBehaviorType(
                        userId,
                        UserBehavior.TargetType.COMPETITION,
                        competitionId,
                        behaviorType);

        if (existing.isPresent()) {
            UserBehavior behavior = existing.get();
            int currentWeight = behavior.getWeight() != null ? behavior.getWeight() : 0;
            behavior.setWeight(currentWeight + deltaWeight);
            userBehaviorRepository.save(behavior);
            log.info("behavior={} userId={} competitionId={} deltaWeight={} action=update",
                    behaviorType, userId, competitionId, deltaWeight);
            return;
        }

        UserBehavior behavior = new UserBehavior();
        behavior.setUser(user);
        behavior.setTargetType(UserBehavior.TargetType.COMPETITION);
        behavior.setTargetId(competitionId);
        behavior.setBehaviorType(behaviorType);
        behavior.setWeight(deltaWeight);
        userBehaviorRepository.save(behavior);
        log.info("behavior={} userId={} competitionId={} deltaWeight={} action=insert",
                behaviorType, userId, competitionId, deltaWeight);
    }
}
