package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamSubmissionResponse {
    private Long id;
    private Long teamId;
    private Long competitionId;
    private Long submittedBy;
    private String fileName;
    private String fileUrl;
    private String remark;
    private LocalDateTime submittedAt;
    private Boolean isCurrent;
}
