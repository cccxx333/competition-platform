package com.competition.dto;

import com.competition.entity.Team;
import lombok.Data;

@Data
public class ManagedCompetitionTeamDTO {
    private Long teamId;
    private String teamName;
    private Long competitionId;
    private String competitionName;
    private Long teacherId;
    private String teacherName;
    private Integer currentMemberCount;
    private Integer maxMemberCount;
    private Team.TeamStatus status;
}

