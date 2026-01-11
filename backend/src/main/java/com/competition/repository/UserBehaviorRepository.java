package com.competition.repository;

import com.competition.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findByUserId(Long userId);
    List<UserBehavior> findByTargetType(UserBehavior.TargetType targetType);
    List<UserBehavior> findByUserIdAndTargetType(Long userId, UserBehavior.TargetType targetType);
}