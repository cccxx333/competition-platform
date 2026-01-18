package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AwardDetail {
    private Long awardId;
    private String awardName;
    private Long competitionId;
    private Long teamId;
    private LocalDateTime publishedAt;
}
