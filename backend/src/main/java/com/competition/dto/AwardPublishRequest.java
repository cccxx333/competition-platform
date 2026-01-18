package com.competition.dto;

import lombok.Data;

@Data
public class AwardPublishRequest {
    private Long competitionId;
    private Long teamId;
    private String awardName;
}
