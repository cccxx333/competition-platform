package com.competition.dto;

import lombok.Data;

@Data
public class SkillResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private Boolean isActive;
}
