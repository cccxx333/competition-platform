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
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_account_no", columnNames = "account_no"),
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
})
public class User {
    public enum Role {
        ADMIN, TEACHER, STUDENT
    }
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "account_no", nullable = false, length = 32)
    @ToString.Include
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @ToString.Include
    private Role role = Role.STUDENT;

    @Column(length = 64)
    @ToString.Include
    private String username;

    @Column(name = "display_name", length = 64)
    @ToString.Include
    private String displayName;

    @Column(name = "real_name", length = 64)
    @ToString.Include
    private String realName;

    @Column(length = 128)
    @ToString.Include
    private String email;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @ToString.Include
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(length = 128)
    @ToString.Include
    private String school;

    @Column(length = 128)
    @ToString.Include
    private String major;

    @Column(length = 32)
    @ToString.Include
    private String grade;

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 闃叉搴忓垪鍖栫敤鎴锋妧鑳介泦鍚?
    private Set<UserSkill> userSkills = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (accountNo == null) {
            accountNo = username;
        }
        if (role == null) {
            role = Role.STUDENT;
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.APPROVED;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (role == null) {
            role = Role.STUDENT;
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.APPROVED;
        }
        updatedAt = LocalDateTime.now();
    }
}

