package com.competition.dto;

import com.competition.entity.TeacherApplication;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherApplicationListItemDTO {
    private Long id;
    private Long competitionId;
    private String competitionName;
    private TeacherApplication.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reviewComment;
    private String description;
    private String teamDescription;
    private List<TeacherApplicationSkillDTO> skills;
}
