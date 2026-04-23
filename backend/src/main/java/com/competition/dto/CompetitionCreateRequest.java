package com.competition.dto;

import com.competition.entity.Competition;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CompetitionCreateRequest {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String organizer;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private LocalDate registrationDeadline;
    @NotNull
    @Min(1)
    private Integer minTeamSize;
    @NotNull
    @Min(1)
    private Integer maxTeamSize;
    private String category;
    @NotBlank
    private String level;
    private Competition.CompetitionStatus status;
    private Long createdById;
    @NotNull
    private Long managerId;
    @NotEmpty
    @Valid
    private List<CompetitionRequiredSkillDTO> requiredSkills;
}
