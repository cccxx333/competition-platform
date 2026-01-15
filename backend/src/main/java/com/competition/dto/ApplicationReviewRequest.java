package com.competition.dto;

import lombok.Data;

@Data
public class ApplicationReviewRequest {
    private Boolean approved;
    private String reason;
}
