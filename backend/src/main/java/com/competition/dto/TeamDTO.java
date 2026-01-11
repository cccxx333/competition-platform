package com.competition.dto;

import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamDTO {
    private Long id;
    private String name;
    private String description;
    private CompetitionDTO competition;
    private UserDTO leader;
    private Integer maxMembers;
    private Integer currentMembers;
    private Team.TeamStatus status;
    private LocalDateTime createdAt;
    private List<TeamMember> teamMembers;
}