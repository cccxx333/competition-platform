package com.competition.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String realName;
    private String phone;
    private String avatarUrl;
    private String school;
    private String major;
    private String grade;
}
