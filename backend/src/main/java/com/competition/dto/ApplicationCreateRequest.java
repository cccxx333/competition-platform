package com.competition.dto;

import lombok.Data;

@Data
public class ApplicationCreateRequest {
    private Long competitionId;
    private Long teamId;
}
