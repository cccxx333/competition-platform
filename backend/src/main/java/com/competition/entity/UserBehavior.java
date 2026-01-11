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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    @ToString.Include
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    @ToString.Include
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 20)
    @ToString.Include
    private BehaviorType behaviorType;

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TargetType {
        COMPETITION, TEAM
    }

    public enum BehaviorType {
        VIEW, LIKE, APPLY, JOIN
    }
}