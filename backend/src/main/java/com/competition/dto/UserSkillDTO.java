package com.competition.dto;

import lombok.Data;

@Data
public class UserSkillDTO {
    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private Integer proficiency;
}