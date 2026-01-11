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
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    @JsonIgnore // 避免序列化竞赛引用，在DTO中处理
    private Competition competition;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    @ToString.Include
    private User leader;

    @Column(name = "max_members")
    @ToString.Include
    private Integer maxMembers = 5;

    @Column(name = "current_members")
    @ToString.Include
    private Integer currentMembers = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @ToString.Include
    private TeamStatus status = TeamStatus.RECRUITING;

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 避免序列化队伍成员集合
    private Set<TeamMember> teamMembers = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TeamStatus {
        RECRUITING, FULL, DISBANDED
    }
}