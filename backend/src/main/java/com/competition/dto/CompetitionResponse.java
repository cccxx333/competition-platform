package com.competition.dto;

import com.competition.entity.Competition;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompetitionResponse {
    private Long id;
    private String name;
    private String description;
    private String organizer;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private String category;
    private String level;
    private Competition.CompetitionStatus status;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
