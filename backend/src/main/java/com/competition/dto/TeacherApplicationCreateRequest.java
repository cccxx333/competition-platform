package com.competition.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeacherApplicationCreateRequest {
    private Long competitionId;
    private String teamName;
    private String description;
    private List<TeacherApplicationSkillDTO> skills;
}
