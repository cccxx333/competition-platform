package com.competition.dto;

import com.competition.entity.Competition;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompetitionAdminUpdateRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Competition.CompetitionStatus status;
    private String description;
}
