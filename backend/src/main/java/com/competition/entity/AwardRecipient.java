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
@Table(name = "award_recipients", uniqueConstraints = {
        @UniqueConstraint(name = "uk_award_recipients_award_user",
                columnNames = {"team_award_id", "user_id"})
})
public class AwardRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "team_award_id", nullable = false)
    private Long teamAwardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
