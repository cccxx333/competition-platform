package com.competition.repository;

import com.competition.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findByUserId(Long userId);
    List<UserBehavior> findByTargetType(UserBehavior.TargetType targetType);

    @Query("select ub from UserBehavior ub where ub.user.id = :userId and ub.targetType = :targetType")
    List<UserBehavior> findByUserIdAndTargetType(Long userId, UserBehavior.TargetType targetType);

    default List<UserBehavior> findByUserIdAndTargetType(Long userId, String targetType) {
        return findByUserIdAndTargetType(userId, UserBehavior.TargetType.valueOf(targetType));
    }

    @Query("select ub from UserBehavior ub where ub.targetType = :targetType and ub.targetId in :targetIds")
    List<UserBehavior> findByTargetTypeAndTargetIdIn(UserBehavior.TargetType targetType, Collection<Long> targetIds);

    default List<UserBehavior> findByTargetTypeAndTargetIdIn(String targetType, Collection<Long> targetIds) {
        return findByTargetTypeAndTargetIdIn(UserBehavior.TargetType.valueOf(targetType), targetIds);
    }

    @Query("select ub from UserBehavior ub where ub.targetType = :targetType and ub.user.id in :userIds")
    List<UserBehavior> findByTargetTypeAndUserIdIn(UserBehavior.TargetType targetType, Collection<Long> userIds);

    default List<UserBehavior> findByTargetTypeAndUserIdIn(String targetType, Collection<Long> userIds) {
        return findByTargetTypeAndUserIdIn(UserBehavior.TargetType.valueOf(targetType), userIds);
    }

    @Query("select ub from UserBehavior ub " +
            "where ub.user.id = :userId and ub.targetType = :targetType " +
            "and ub.targetId = :targetId and ub.behaviorType = :behaviorType")
    Optional<UserBehavior> findFirstByUserIdAndTargetTypeAndTargetIdAndBehaviorType(
            Long userId,
            UserBehavior.TargetType targetType,
            Long targetId,
            UserBehavior.BehaviorType behaviorType);

    default Optional<UserBehavior> findFirstByUserIdAndTargetTypeAndTargetIdAndBehaviorType(
            Long userId,
            String targetType,
            Long targetId,
            String behaviorType) {
        return findFirstByUserIdAndTargetTypeAndTargetIdAndBehaviorType(
                userId,
                UserBehavior.TargetType.valueOf(targetType),
                targetId,
                UserBehavior.BehaviorType.valueOf(behaviorType));
    }
}
