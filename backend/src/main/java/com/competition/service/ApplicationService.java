package com.competition.service;

import com.competition.dto.ApplicationCreateRequest;
import com.competition.dto.ApplicationResponse;
import com.competition.dto.ApplicationReviewRequest;
import com.competition.entity.Application;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.ApplicationRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

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

        LocalDateTime now = LocalDateTime.now();
        Application application = applicationRepository
                .findFirstByStudent_IdAndCompetition_IdAndIsActiveFalseOrderByIdDesc(
                        student.getId(),
                        competition.getId()
                )
                .orElseGet(Application::new);
        application.setStudent(student);
        application.setCompetition(competition);
        application.setTeam(team);
        application.setStatus(Application.Status.PENDING);
        application.setIsActive(true);
        application.setAppliedAt(now);
        application.setReviewedAt(null);
        application.setReviewedBy(null);
        application.setRemovedAt(null);
        application.setRemovedBy(null);
        application.setReason(null);

        Application saved;
        try {
            saved = applicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "active application exists");
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForTeacher(Long currentUserId, Long teamIdOptional, String statusOptional) {
        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only TEACHER can view applications");
        }

        List<Team> teams = teamRepository.findByLeaderId(teacher.getId());
        List<Long> teamIds = teams.stream().map(Team::getId).collect(Collectors.toList());
        if (teamIds.isEmpty()) {
            return List.of();
        }

        if (teamIdOptional != null && !teamIds.contains(teamIdOptional)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not team leader");
        }

        List<Long> targetTeamIds = teamIdOptional != null ? List.of(teamIdOptional) : teamIds;
        if (statusOptional != null && !statusOptional.isBlank()) {
            Application.Status status;
            try {
                status = Application.Status.valueOf(statusOptional);
            } catch (IllegalArgumentException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "invalid status");
            }
            return applicationRepository.findByTeamIdInAndStatus(targetTeamIds, status).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return applicationRepository.findByTeamIdIn(targetTeamIds).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse review(Long currentUserId, Long applicationId, ApplicationReviewRequest req) {
        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only TEACHER can review");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "application not found"));

        Team teamRef = application.getTeam();
        if (teamRef == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "team not found");
        }
        Team team = teamRepository.findById(teamRef.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));

        if (team.getLeader() == null || !teacher.getId().equals(team.getLeader().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not team leader");
        }

        if (application.getStatus() != Application.Status.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "application already reviewed");
        }

        boolean approved = req != null && Boolean.TRUE.equals(req.getApproved());
        if (approved) {
            if (team.getStatus() != Team.TeamStatus.RECRUITING) {
                throw new ApiException(HttpStatus.CONFLICT, "team is not recruiting");
            }

            Competition competitionRef = team.getCompetition();
            if (competitionRef == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "competition not found");
            }
            Competition competition = competitionRepository.findById(competitionRef.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));
            Integer maxSize = competition.getMaxTeamSize();
            long currentCount = teamMemberRepository.countByTeamIdAndLeftAtIsNull(team.getId());
            if (maxSize != null && currentCount >= maxSize) {
                throw new ApiException(HttpStatus.CONFLICT, "team is full");
            }

            User student = application.getStudent();
            if (student == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "student not found");
            }
            boolean alreadyMember = teamMemberRepository.existsByTeamIdAndUserIdAndLeftAtIsNull(team.getId(), student.getId());
            if (alreadyMember) {
                throw new ApiException(HttpStatus.CONFLICT, "student already in team");
            }

            TeamMember member = new TeamMember();
            member.setTeam(team);
            member.setUser(student);
            member.setRole(TeamMember.Role.MEMBER);
            member.setJoinedAt(LocalDateTime.now());
            teamMemberRepository.save(member);

            application.setStatus(Application.Status.APPROVED);
            application.setIsActive(true);
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewedBy(teacher);
            application.setReason(req != null ? req.getReason() : null);
        } else {
            application.setStatus(Application.Status.REJECTED);
            application.setIsActive(false);
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewedBy(teacher);
            application.setReason(req != null ? req.getReason() : null);
        }

        Application saved = applicationRepository.save(application);
        return toResponse(saved);
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
