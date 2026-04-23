package com.competition.dto;

import com.competition.entity.Competition;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompetitionAdminUpdateRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Competition.CompetitionStatus status;
    private String description;
    private Long managerId;
    private java.util.List<CompetitionRequiredSkillDTO> requiredSkills;
}
