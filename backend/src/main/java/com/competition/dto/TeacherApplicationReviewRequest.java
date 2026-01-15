package com.competition.dto;

import lombok.Data;

@Data
public class TeacherApplicationReviewRequest {
    private Boolean approved;
    private String reviewComment;
}
