package com.competition.dto;

import com.competition.entity.TeacherApplication;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminTeacherApplicationListItemDTO {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long competitionId;
    private String competitionName;
    private TeacherApplication.Status status;
    private LocalDateTime createdAt;
}
