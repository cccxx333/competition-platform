package com.competition.service;

import com.competition.dto.TeacherApplicationCreateRequest;
import com.competition.dto.TeacherApplicationResponse;
import com.competition.dto.TeacherApplicationSkillDTO;
import com.competition.entity.Competition;
import com.competition.entity.Skill;
import com.competition.entity.TeacherApplication;
import com.competition.entity.TeacherApplicationSkill;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.SkillRepository;
import com.competition.repository.TeacherApplicationRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherApplicationService {

    private final TeacherApplicationRepository teacherApplicationRepository;
    private final CompetitionRepository competitionRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public TeacherApplicationResponse createApplication(Long userId, TeacherApplicationCreateRequest request) {
        if (request == null || request.getCompetitionId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "competitionId is required");
        }

        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "teacher not found"));
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only TEACHER can apply");
        }

        Competition competition = competitionRepository.findById(request.getCompetitionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));

        boolean exists = teacherApplicationRepository
                .existsByCompetitionIdAndTeacherId(competition.getId(), teacher.getId());
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "application already exists");
        }

        TeacherApplication application = new TeacherApplication();
        application.setCompetition(competition);
        application.setTeacher(teacher);
        application.setStatus(TeacherApplication.Status.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        List<TeacherApplicationSkillDTO> requestSkills = request.getSkills();
        if (requestSkills != null && !requestSkills.isEmpty()) {
            for (TeacherApplicationSkillDTO skillDTO : requestSkills) {
                if (skillDTO == null || skillDTO.getSkillId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "skillId is required");
                }
                Skill skill = skillRepository.findById(skillDTO.getSkillId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "skill not found: " + skillDTO.getSkillId()));

                TeacherApplicationSkill applicationSkill = new TeacherApplicationSkill();
                applicationSkill.setTeacherApplication(application);
                applicationSkill.setSkill(skill);
                applicationSkill.setWeight(skillDTO.getWeight() != null ? skillDTO.getWeight() : 1);
                application.getApplicationSkills().add(applicationSkill);
            }
        }

        TeacherApplication saved = teacherApplicationRepository.save(application);
        return toResponse(saved);
    }

    private TeacherApplicationResponse toResponse(TeacherApplication application) {
        TeacherApplicationResponse response = new TeacherApplicationResponse();
        response.setId(application.getId());
        response.setCompetitionId(application.getCompetition() != null ? application.getCompetition().getId() : null);
        response.setTeacherId(application.getTeacher() != null ? application.getTeacher().getId() : null);
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setReviewedAt(application.getReviewedAt());
        response.setReviewedBy(application.getReviewedBy() != null ? application.getReviewedBy().getId() : null);
        response.setReviewComment(application.getReviewComment());
        response.setGeneratedTeamId(application.getGeneratedTeam() != null ? application.getGeneratedTeam().getId() : null);

        if (application.getApplicationSkills() != null && !application.getApplicationSkills().isEmpty()) {
            List<TeacherApplicationSkillDTO> skills = new ArrayList<>();
            for (TeacherApplicationSkill applicationSkill : application.getApplicationSkills()) {
                TeacherApplicationSkillDTO dto = new TeacherApplicationSkillDTO();
                dto.setSkillId(applicationSkill.getSkill() != null ? applicationSkill.getSkill().getId() : null);
                dto.setWeight(applicationSkill.getWeight());
                skills.add(dto);
            }
            response.setSkills(skills);
        }

        return response;
    }
}
