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
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
})
public class User {
    public enum Role {
        ADMIN, TEACHER, STUDENT
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

    @Column(name = "real_name", length = 64)
    @ToString.Include
    private String realName;

    @Column(length = 128)
    @ToString.Include
    private String email;

    @Column(length = 32)
    private String phone;

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
    @JsonIgnore // 防止序列化用户技能集合
    private Set<UserSkill> userSkills = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (accountNo == null) {
            accountNo = username;
        }
        if (role == null) {
            role = Role.STUDENT;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (role == null) {
            role = Role.STUDENT;
        }
        updatedAt = LocalDateTime.now();
    }
}
