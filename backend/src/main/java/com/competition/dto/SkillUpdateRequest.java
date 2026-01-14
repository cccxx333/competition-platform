package com.competition.dto;

import lombok.Data;

@Data
public class SkillUpdateRequest {
    private String name;
    private String category;
    private String description;
    private Boolean isActive;
}
