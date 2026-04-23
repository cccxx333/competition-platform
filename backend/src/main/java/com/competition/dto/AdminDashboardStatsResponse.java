package com.competition.dto;

import lombok.Data;

@Data
public class AdminDashboardStatsResponse {
    private long pendingTeacherUserCount;
    private long finishedPendingAwardCompetitionCount;
    private long ongoingCompetitionCount;
    private long competitionTotalCount;
}
