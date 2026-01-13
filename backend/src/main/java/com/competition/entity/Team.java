package com.competition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teams_competition_leader", columnNames = {"competition_id", "leader_id"})
})
public class Team {
    public enum TeamStatus {
        RECRUITING, CLOSED, DISBANDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    @JsonIgnore
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    @ToString.Include
    private User leader; // 教师组组长=指导教师

    @Column(nullable = false, length = 128)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @ToString.Include
    private TeamStatus status = TeamStatus.RECRUITING;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    @JsonIgnore
    private User closedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<TeamMember> teamMembers = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<TeamSkill> teamSkills = new HashSet<>();

    @Transient
    private Integer maxMembers;

    @Transient
    private Integer currentMembers;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TeamStatus.RECRUITING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == null) {
            status = TeamStatus.RECRUITING;
        }
        updatedAt = LocalDateTime.now();
    }
}
