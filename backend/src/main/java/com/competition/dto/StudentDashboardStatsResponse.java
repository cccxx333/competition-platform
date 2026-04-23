package com.competition.dto;

import lombok.Data;

@Data
public class StudentDashboardStatsResponse {
    private long pendingTeacherReviewCount;
    private long ongoingCompetitionCount;
    private long participationCount;
    private long awardCount;
}
