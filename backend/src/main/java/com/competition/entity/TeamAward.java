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
@Table(name = "team_awards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_team_awards_competition_team_award",
                columnNames = {"competition_id", "team_id", "award_name"})
})
public class TeamAward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "competition_id", nullable = false)
    private Long competitionId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "award_name", nullable = false, length = 64)
    @ToString.Include
    private String awardName;

    @Column(name = "published_by", nullable = false)
    private Long publishedBy;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT")
    private Byte isActive = 1;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = 1;
        }
    }

    @Transient
    public boolean isActive() {
        return isActive != null && isActive == 1;
    }

    public void deactivate() {
        this.isActive = 0;
    }
}
