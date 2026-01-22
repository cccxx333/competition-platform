package com.competition.dto;

import com.competition.entity.Team;
import lombok.Data;
import java.util.List;

@Data
public class TeamRecommendationResponse {
    private Long teamId;
    private String teamName;
    private Team.TeamStatus teamStatus;
    private Double matchScore;
    private List<TeamRecommendReason> reasons;
    private boolean fallbackSorted;
}
