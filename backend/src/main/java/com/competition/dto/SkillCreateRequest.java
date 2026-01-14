package com.competition.dto;

import lombok.Data;

@Data
public class SkillCreateRequest {
    private String name;
    private String category;
    private String description;
    private Boolean isActive;
}
