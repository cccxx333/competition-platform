package com.competition.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_behaviors")
public class UserBehavior {
    public enum TargetType {
        COMPETITION, TEAM, SKILL
    }

    public enum BehaviorType {
        VIEW, LIKE, FAVORITE, APPLY, JOIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 20)
    @ToString.Include
    private BehaviorType behaviorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    @ToString.Include
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    @ToString.Include
    private Long targetId;

    @Column(name = "weight")
    private Integer weight = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (weight == null) {
            weight = 1;
        }
    }
}
