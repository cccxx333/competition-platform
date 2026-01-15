package com.competition.dto;

import com.competition.entity.TeacherApplication;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherApplicationResponse {
    private Long id;
    private Long competitionId;
    private Long teacherId;
    private TeacherApplication.Status status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String reviewComment;
    private Long generatedTeamId;
    private List<TeacherApplicationSkillDTO> skills;
}
