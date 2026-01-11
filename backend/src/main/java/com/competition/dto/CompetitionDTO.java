package com.competition.dto;

import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompetitionDTO {
    private Long id;
    private String name;
    private String description;
    private String organizer;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer maxTeamSize;
    private String category;
    private String level;
    private Competition.CompetitionStatus status;
    private LocalDateTime createdAt;
    private List<CompetitionSkill> requiredSkills;
}