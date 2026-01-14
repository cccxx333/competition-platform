package com.competition.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String accountNo;
    private String role;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String school;
    private String major;
    private String grade;
}
