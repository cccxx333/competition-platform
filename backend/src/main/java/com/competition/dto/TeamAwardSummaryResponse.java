package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamAwardSummaryResponse {
    private boolean hasAward;
    private Long awardId;
    private Long competitionId;
    private Long teamId;
    private String awardName;
    private LocalDateTime publishedAt;
}
