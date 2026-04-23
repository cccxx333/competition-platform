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
            throw new ApiException(HttpStatus.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
        if (nextDeadline != null && nextStartDate != null && nextDeadline.isAfter(nextStartDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "报名截止日期不能晚于开始日期");
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
        // managerId: null means clear, non-null means set
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "指定的负责人不存在"));
            if (manager.getRole() != User.Role.TEACHER) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "负责人必须是教师角色");
            }
            competition.setManager(manager);
        }

        if (request.getRequiredSkills() != null) {
            // Remove old skills
            competitionSkillRepository.deleteByCompetitionId(competitionId);
            
            // Add new skills
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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "竞赛不存在"));

        // Check if teams exist under this competition
        boolean hasTeams = !teamRepository.findByCompetitionId(competitionId).isEmpty();
        if (hasTeams) {
            throw new ApiException(HttpStatus.CONFLICT, "该竞赛下已有队伍创建，无法删除");
        }

        // Check if teacher applications exist
        long appCount = teacherApplicationRepository.findAll().stream()
                .filter(a -> a.getCompetition() != null && competitionId.equals(a.getCompetition().getId()))
                .count();
        if (appCount > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "该竞赛下已有教师申请记录，无法删除");
        }

        competitionSkillRepository.deleteByCompetitionId(competitionId);
        competitionRepository.delete(competition);
    }
}
