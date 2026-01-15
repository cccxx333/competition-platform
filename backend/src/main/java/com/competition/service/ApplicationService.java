package com.competition.service;

import com.competition.dto.ApplicationCreateRequest;
import com.competition.dto.ApplicationResponse;
import com.competition.dto.ApplicationReviewRequest;
import com.competition.entity.Application;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.ApplicationRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;

    public ApplicationResponse createApplication(Long currentUserId, ApplicationCreateRequest req) {
        if (req == null || req.getCompetitionId() == null || req.getTeamId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "competitionId and teamId required");
        }

        User student = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (student.getRole() != User.Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only STUDENT can apply");
        }

        Competition competition = competitionRepository.findById(req.getCompetitionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));
        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));

        if (team.getCompetition() == null || !team.getCompetition().getId().equals(competition.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "team does not belong to competition");
        }
        if (team.getStatus() != Team.TeamStatus.RECRUITING) {
            throw new ApiException(HttpStatus.CONFLICT, "team is not recruiting");
        }

        LocalDate deadline = competition.getRegistrationDeadline();
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "registration deadline passed");
        }

        boolean exists = applicationRepository.existsByStudentIdAndCompetitionIdAndIsActiveTrueAndStatusIn(
                student.getId(),
                competition.getId(),
                Set.of(Application.Status.PENDING, Application.Status.APPROVED)
        );
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "active application exists");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setCompetition(competition);
        application.setTeam(team);
        application.setStatus(Application.Status.PENDING);
        application.setIsActive(true);
        application.setAppliedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForTeacher(Long currentUserId, Long teamIdOptional, String statusOptional) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    public ApplicationResponse review(Long currentUserId, Long applicationId, ApplicationReviewRequest req) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    private ApplicationResponse toResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setCompetitionId(application.getCompetition() != null ? application.getCompetition().getId() : null);
        response.setTeamId(application.getTeam() != null ? application.getTeam().getId() : null);
        response.setStudentId(application.getStudent() != null ? application.getStudent().getId() : null);
        response.setStatus(application.getStatus());
        response.setIsActive(application.getIsActive());
        response.setAppliedAt(application.getAppliedAt());
        response.setReviewedAt(application.getReviewedAt());
        response.setReviewedBy(application.getReviewedBy() != null ? application.getReviewedBy().getId() : null);
        response.setRemovedAt(application.getRemovedAt());
        response.setRemovedBy(application.getRemovedBy() != null ? application.getRemovedBy().getId() : null);
        response.setReason(application.getReason());
        return response;
    }
}
