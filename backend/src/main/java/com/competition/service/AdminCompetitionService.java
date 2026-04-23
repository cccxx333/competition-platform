package com.competition.service;

import com.competition.dto.CompetitionAdminUpdateRequest;
import com.competition.entity.Competition;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.TeacherApplicationRepository;
import com.competition.repository.TeacherApplicationSkillRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final TeacherApplicationSkillRepository teacherApplicationSkillRepository;
    private final TeacherApplicationRepository teacherApplicationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public Competition updateCompetition(Long adminUserId, Long competitionId, CompetitionAdminUpdateRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "request is required");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can update competition");
        }

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));

        LocalDate nextStartDate = request.getStartDate() != null ? request.getStartDate() : competition.getStartDate();
        LocalDate nextEndDate = request.getEndDate() != null ? request.getEndDate() : competition.getEndDate();
        LocalDate nextDeadline = request.getRegistrationDeadline() != null
                ? request.getRegistrationDeadline()
                : competition.getRegistrationDeadline();

        if (nextStartDate != null && nextEndDate != null && nextStartDate.isAfter(nextEndDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be before or equal to endDate");
        }
        if (nextDeadline != null && nextStartDate != null && nextDeadline.isAfter(nextStartDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "registrationDeadline must be before or equal to startDate");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            competition.setName(request.getName().trim());
        }
        if (request.getStartDate() != null) {
            competition.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            competition.setEndDate(request.getEndDate());
        }
        if (request.getRegistrationDeadline() != null) {
            competition.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getMinTeamSize() != null) {
            competition.setMinTeamSize(request.getMinTeamSize());
        }
        if (request.getMaxTeamSize() != null) {
            competition.setMaxTeamSize(request.getMaxTeamSize());
        }
        if (request.getStatus() != null) {
            competition.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            competition.setDescription(request.getDescription());
        }

        if (Boolean.TRUE.equals(request.getClearManager())) {
            competition.setManager(null);
        } else if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "manager not found"));
            if (manager.getRole() != User.Role.TEACHER) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "manager must be TEACHER");
            }
            competition.setManager(manager);
        }

        if (request.getRequiredSkills() != null) {
            competitionSkillRepository.deleteByCompetitionId(competitionId);

            java.util.Set<com.competition.entity.CompetitionSkill> newSkills = request.getRequiredSkills().stream()
                    .map(dto -> {
                        com.competition.entity.CompetitionSkill cs = new com.competition.entity.CompetitionSkill();
                        cs.setCompetition(competition);
                        com.competition.entity.Skill skill = new com.competition.entity.Skill();
                        skill.setId(dto.getSkillId());
                        cs.setSkill(skill);
                        cs.setImportance(dto.getImportance() != null ? dto.getImportance() : 1);
                        return cs;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            competitionSkillRepository.saveAll(newSkills);
            competition.setCompetitionSkills(newSkills);
        }

        return competitionRepository.save(competition);
    }

    public void deleteCompetition(Long adminUserId, Long competitionId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can delete competition");
        }

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));

        boolean hasTeams = !teamRepository.findByCompetitionId(competitionId).isEmpty();
        if (hasTeams) {
            throw new ApiException(HttpStatus.CONFLICT, "cannot delete competition with existing teams");
        }

        long appCount = teacherApplicationRepository.findAll().stream()
                .filter(a -> a.getCompetition() != null && competitionId.equals(a.getCompetition().getId()))
                .count();
        if (appCount > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "cannot delete competition with existing teacher applications");
        }

        competitionSkillRepository.deleteByCompetitionId(competitionId);
        competitionRepository.delete(competition);
    }
}
