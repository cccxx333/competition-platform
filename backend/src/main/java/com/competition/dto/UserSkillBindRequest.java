package com.competition.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UserSkillBindRequest {
    private Long skillId;
    @JsonAlias("level")
    private Integer proficiency;
}
