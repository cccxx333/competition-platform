package com.competition.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CompetitionRequiredSkillDTO {
    @NotNull
    private Long skillId;
    private String skillName;
    private Integer importance;
}
