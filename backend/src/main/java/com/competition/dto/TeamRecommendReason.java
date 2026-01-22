package com.competition.dto;

import lombok.Data;

@Data
public class TeamRecommendReason {
    private Long skillId;
    private String skillName;
    private Integer weight;
}
