package com.competition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "competitions")
public class Competition {
    public enum CompetitionStatus {
        UPCOMING, ONGOING, FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 128)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 128)
    @ToString.Include
    private String organizer;

    @Column(name = "start_date")
    @ToString.Include
    private LocalDate startDate;

    @Column(name = "end_date")
    @ToString.Include
    private LocalDate endDate;

    @Column(name = "registration_deadline")
    @ToString.Include
    private LocalDate registrationDeadline;

    @Column(name = "min_team_size")
    private Integer minTeamSize = 1;

    @Column(name = "max_team_size", nullable = false)
    private Integer maxTeamSize = 1;

    @Column(length = 64)
    @ToString.Include
    private String category;

    @Column(length = 64)
    @ToString.Include
    private String level;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @ToString.Include
    private CompetitionStatus status = CompetitionStatus.UPCOMING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<CompetitionSkill> competitionSkills = new HashSet<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Team> teams = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (minTeamSize == null) {
            minTeamSize = 1;
        }
        if (maxTeamSize == null) {
            maxTeamSize = 1;
        }
        if (status == null) {
            status = CompetitionStatus.UPCOMING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == null) {
            status = CompetitionStatus.UPCOMING;
        }
        updatedAt = LocalDateTime.now();
    }
}
