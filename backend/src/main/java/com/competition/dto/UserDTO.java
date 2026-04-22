package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String accountNo;
    private String role;
    private String approvalStatus;
    private String username;
    private String displayName;
    private String email;
    private String realName;
    private String school;
    private String major;
    private String grade;
    private String phone;
    private String avatarUrl;
    private LocalDateTime createdAt;
}

