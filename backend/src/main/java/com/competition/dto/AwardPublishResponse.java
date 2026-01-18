package com.competition.dto;

import lombok.Data;
import java.util.List;

@Data
public class AwardPublishResponse {
    private Long awardId;
    private Long competitionId;
    private Long teamId;
    private String awardName;
    private Integer recipientCount;
    private List<Long> recipientUserIds;
}
