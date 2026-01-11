package com.competition.dto;

import lombok.Data;

@Data
public class SimpleUserSkillDTO {
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private Integer proficiency;
}