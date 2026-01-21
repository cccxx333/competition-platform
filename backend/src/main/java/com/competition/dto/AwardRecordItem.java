package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AwardRecordItem {
    private Long awardId;
    private Long competitionId;
    private Long teamId;
    private String awardName;
    private Integer recipientCount;
    private LocalDateTime publishedAt;
}
