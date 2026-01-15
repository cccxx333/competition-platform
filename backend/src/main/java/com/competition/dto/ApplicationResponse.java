package com.competition.dto;

import com.competition.entity.Application;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Long studentId;
    private Application.Status status;
    private Boolean isActive;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private LocalDateTime removedAt;
    private Long removedBy;
    private String reason;
}
