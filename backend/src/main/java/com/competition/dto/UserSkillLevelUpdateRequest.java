package com.competition.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UserSkillLevelUpdateRequest {
    @JsonAlias("proficiency")
    private Integer level;
}
