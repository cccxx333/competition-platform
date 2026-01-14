package com.competition.dto;

import com.competition.entity.Competition;
import lombok.Data;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;

@Data
public class CompetitionCreateRequest {
    @NotBlank
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
}
