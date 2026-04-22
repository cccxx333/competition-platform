package com.competition.dto;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String username;
    private String displayName;
    private String email;
    private String realName;
    private String phone;
    private String avatarUrl;
    private String school;
    private String major;
    private String grade;
    private String approvalStatus;
}
