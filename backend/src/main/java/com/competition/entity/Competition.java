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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 200)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
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

    @Column(name = "max_team_size")
    @ToString.Include
    private Integer maxTeamSize = 5;

    @Column(length = 50)
    @ToString.Include
    private String category;

    @Column(length = 20)
    @ToString.Include
    private String level;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @ToString.Include
    private CompetitionStatus status = CompetitionStatus.UPCOMING;

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 忽略这个字段的序列化，避免循环引用
    private Set<CompetitionSkill> competitionSkills = new HashSet<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 忽略这个字段的序列化
    private Set<Team> teams = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CompetitionStatus {
        UPCOMING, ONGOING, FINISHED
    }
}