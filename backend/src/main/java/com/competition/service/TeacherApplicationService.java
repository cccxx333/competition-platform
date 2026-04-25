package com.competition.service;

import com.competition.dto.AdminTeacherApplicationListItemDTO;
import com.competition.dto.TeacherApplicationCreateRequest;
import com.competition.dto.TeacherApplicationListItemDTO;
import com.competition.dto.TeacherApplicationResponse;
import com.competition.dto.TeacherApplicationReviewRequest;
import com.competition.dto.TeacherApplicationSkillDTO;
import com.competition.entity.Competition;
import com.competition.entity.Skill;
import com.competition.entity.Team;
import com.competition.entity.TeamSkill;
import com.competition.entity.TeacherApplication;
import com.competition.entity.TeacherApplicationSkill;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.SkillRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.TeamSkillRepository;
import com.competition.repository.TeacherApplicationRepository;
import com.competition.repository.TeacherApplicationSkillRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherApplicationService {
    private static final int MIN_REQUIRED_SKILLS = 1;
    private static final int MAX_REQUIRED_SKILLS = 8;

    private final TeacherApplicationRepository teacherApplicationRepository;
    private final CompetitionRepository competitionRepository;
    private final SkillRepository skillRepository;
    private final TeamRepository teamRepository;
    private final TeamSkillRepository teamSkillRepository;
    private final UserRepository userRepository;
    private final TeacherApplicationSkillRepository teacherApplicationSkillRepository;

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

        if (competition.getManager() != null && competition.getManager().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "负责人不可申请自己负责的竞赛");
        }

        if (competition.getStatus() != Competition.CompetitionStatus.UPCOMING) {
            throw new ApiException(HttpStatus.CONFLICT, "competition status must be UPCOMING");
        }
        if (competition.getRegistrationDeadline() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "registration deadline unavailable");
        }
        if (LocalDate.now().isAfter(competition.getRegistrationDeadline())) {
            throw new ApiException(HttpStatus.CONFLICT, "registration deadline passed");
        }
        validateRequiredSkills(request.getSkills());

        TeacherApplication application = new TeacherApplication();
        application.setCompetition(competition);
        application.setTeacher(teacher);
        application.setTeamName(normalizeTeamName(request.getTeamName()));
        application.setStatus(TeacherApplication.Status.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        TeacherApplication saved = teacherApplicationRepository.save(application);
        applyApplicationSkills(saved, request.getSkills(), true);
        return toResponse(saved);
    }

    private void validateRequiredSkills(List<TeacherApplicationSkillDTO> requestSkills) {
        if (requestSkills == null || requestSkills.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "at least one preferred skill is required");
        }
        Set<Long> uniqueSkillIds = requestSkills.stream()
                .filter(Objects::nonNull)
                .map(TeacherApplicationSkillDTO::getSkillId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueSkillIds.size() < MIN_REQUIRED_SKILLS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "at least one preferred skill is required");
        }
        if (uniqueSkillIds.size() > MAX_REQUIRED_SKILLS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "preferred skills cannot exceed 8 items");
        }
    }

    @Transactional(readOnly = true)
    public Page<TeacherApplicationListItemDTO> listMyApplications(Long userId,
                                                                  TeacherApplication.Status status,
                                                                  Pageable pageable) {
        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only TEACHER can view applications");
        }

        Page<TeacherApplication> page = (status == null)
                ? teacherApplicationRepository.findByTeacher_Id(teacher.getId(), pageable)
                : teacherApplicationRepository.findByTeacher_IdAndStatus(teacher.getId(), status, pageable);

        List<TeacherApplication> applications = page.getContent();
        Map<Long, List<TeacherApplicationSkillDTO>> skillsMap = new HashMap<>();
        if (!applications.isEmpty()) {
            List<Long> ids = applications.stream()
                    .map(TeacherApplication::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<TeacherApplicationSkill> skills = teacherApplicationSkillRepository
                        .findByTeacherApplication_IdIn(ids);
                for (TeacherApplicationSkill skill : skills) {
                    if (skill == null || skill.getTeacherApplication() == null) {
                        continue;
                    }
                    Long applicationId = skill.getTeacherApplication().getId();
                    if (applicationId == null) {
                        continue;
                    }
                    TeacherApplicationSkillDTO dto = new TeacherApplicationSkillDTO();
                    dto.setSkillId(skill.getSkill() != null ? skill.getSkill().getId() : null);
                    dto.setWeight(skill.getWeight());
                    skillsMap.computeIfAbsent(applicationId, key -> new ArrayList<>()).add(dto);
                }
            }
        }

        List<TeacherApplicationListItemDTO> items = applications.stream()
                .map(application -> {
                    TeacherApplicationListItemDTO dto = toTeacherListItem(application);
                    List<TeacherApplicationSkillDTO> skills = skillsMap.get(application.getId());
                    dto.setSkills(skills != null ? skills : new ArrayList<>());
                    dto.setDescription(dto.getTeamDescription());
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(items, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<AdminTeacherApplicationListItemDTO> listAdminApplications(Long adminUserId,
                                                                          TeacherApplication.Status status,
                                                                          String keyword,
                                                                          Pageable pageable) {
        User user = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));

        Long managerId = null;
        if (user.getRole() == User.Role.ADMIN) {
            managerId = null;
        } else if (user.getRole() == User.Role.TEACHER) {
            managerId = user.getId();
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权限查看");
        }

        Page<TeacherApplication> page = teacherApplicationRepository.searchAdmin(status, keyword, managerId, pageable);
        return page.map(this::toAdminListItem);
    }

    public TeacherApplicationResponse reviewApplication(Long adminUserId, Long applicationId, TeacherApplicationReviewRequest request) {
        if (request == null || request.getApproved() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "approved is required");
        }

        User user = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));

        TeacherApplication application = teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "application not found"));

        boolean hasPermission = false;
        if (user.getRole() == User.Role.ADMIN) {
            hasPermission = true;
        } else if (user.getRole() == User.Role.TEACHER) {
            Competition competition = application.getCompetition();
            if (competition != null && competition.getManager() != null && competition.getManager().getId().equals(user.getId())) {
                hasPermission = true;
            }
        }

        if (!hasPermission) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅管理员或该竞赛负责人可审核");
        }

        if (application.getStatus() != TeacherApplication.Status.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "application already reviewed");
        }

        if (request.getApproved()) {
            Competition competition = application.getCompetition();
            if (competition == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "竞赛不存在");
            }
            if (competition.getManager() != null
                    && application.getTeacher() != null
                    && competition.getManager().getId() != null
                    && competition.getManager().getId().equals(application.getTeacher().getId())) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "competition manager cannot be approved for team creation in the same competition"
                );
            }            if (competition.getStatus() == Competition.CompetitionStatus.FINISHED) {
                throw new ApiException(HttpStatus.CONFLICT, "竞赛已结束，不能通过申请");
            }
            Team team = ensureTeamForApplication(application);
            syncTeamSkillsFromApplication(application, team);
            application.setGeneratedTeam(team);
            application.setStatus(TeacherApplication.Status.APPROVED);
            if (application.getTeacher() != null) {
                application.getTeacher().setApprovalStatus(User.ApprovalStatus.APPROVED);
                userRepository.save(application.getTeacher());
            }
        } else {
            application.setStatus(TeacherApplication.Status.REJECTED);
            if (application.getTeacher() != null) {
                application.getTeacher().setApprovalStatus(User.ApprovalStatus.REJECTED);
                userRepository.save(application.getTeacher());
            }
        }
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(user);
        application.setReviewComment(request.getReviewComment());

        TeacherApplication saved = teacherApplicationRepository.save(application);
        return toResponse(saved);
    }

    private Team ensureTeamForApplication(TeacherApplication application) {
        if (application.getGeneratedTeam() != null) {
            return application.getGeneratedTeam();
        }

        Competition competition = application.getCompetition();
        User teacher = application.getTeacher();
        if (competition == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "competition not found");
        }
        if (teacher == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "teacher not found");
        }

        Team team = new Team();
        team.setCompetition(competition);
        team.setLeader(teacher);
        team.setStatus(Team.TeamStatus.RECRUITING);
        team.setName(resolveTeamName(application, null));
        team.setDescription(null);

        try {
            Team savedTeam = teamRepository.save(team);
            String finalName = resolveTeamName(application, savedTeam.getId());
            if (!finalName.equals(savedTeam.getName())) {
                savedTeam.setName(finalName);
                return teamRepository.save(savedTeam);
            }
            return savedTeam;
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "failed to create team for application");
        }
    }

    private String normalizeTeamName(String teamName) {
        if (teamName == null) {
            return null;
        }
        String trimmed = teamName.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "teamName length must be <= 100");
        }
        return trimmed;
    }

    private String resolveTeamName(TeacherApplication application, Long teamId) {
        String requestedName = application != null ? normalizeTeamName(application.getTeamName()) : null;
        if (requestedName != null) {
            return requestedName;
        }
        return buildDefaultTeamName(teamId);
    }

    private String buildDefaultTeamName(Long teamId) {
        if (teamId == null) {
            return "队伍待命名";
        }
        return "队伍T" + teamId;
    }

    private void syncTeamSkillsFromApplication(TeacherApplication application, Team team) {
        if (team == null || team.getId() == null || application == null || application.getId() == null) {
            return;
        }
        if (teamSkillRepository.existsByTeam_Id(team.getId())) {
            return;
        }

        List<TeacherApplicationSkill> applicationSkills = new ArrayList<>();
        Set<TeacherApplicationSkill> attachedSkills = application.getApplicationSkills();
        if (attachedSkills != null && !attachedSkills.isEmpty()) {
            applicationSkills.addAll(attachedSkills);
        } else {
            applicationSkills = teacherApplicationSkillRepository
                    .findByTeacherApplication_Id(application.getId());
        }

        if (applicationSkills == null || applicationSkills.isEmpty()) {
            return;
        }

        List<TeamSkill> teamSkills = new ArrayList<>();
        for (TeacherApplicationSkill applicationSkill : applicationSkills) {
            if (applicationSkill == null || applicationSkill.getSkill() == null) {
                continue;
            }
            TeamSkill teamSkill = new TeamSkill();
            teamSkill.setTeam(team);
            teamSkill.setSkill(applicationSkill.getSkill());
            teamSkill.setWeight(applicationSkill.getWeight() != null ? applicationSkill.getWeight() : 1);
            teamSkills.add(teamSkill);
        }

        if (!teamSkills.isEmpty()) {
            teamSkillRepository.saveAll(teamSkills);
        }
    }

    private TeacherApplicationResponse toResponse(TeacherApplication application) {
        TeacherApplicationResponse response = new TeacherApplicationResponse();
        response.setId(application.getId());
        response.setCompetitionId(application.getCompetition() != null ? application.getCompetition().getId() : null);
        response.setTeacherId(application.getTeacher() != null ? application.getTeacher().getId() : null);
        response.setTeamName(application.getTeamName());
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

    private TeacherApplicationListItemDTO toTeacherListItem(TeacherApplication application) {
        TeacherApplicationListItemDTO dto = new TeacherApplicationListItemDTO();
        dto.setId(application.getId());
        if (application.getCompetition() != null) {
            dto.setCompetitionId(application.getCompetition().getId());
            dto.setCompetitionName(application.getCompetition().getName());
        }
        dto.setTeamName(application.getTeamName());
        dto.setStatus(application.getStatus());
        dto.setCreatedAt(application.getAppliedAt());
        dto.setUpdatedAt(application.getReviewedAt());
        dto.setReviewComment(application.getReviewComment());
        dto.setTeamDescription(application.getGeneratedTeam() != null ? application.getGeneratedTeam().getDescription() : null);
        dto.setDescription(dto.getTeamDescription());

        if (application.getApplicationSkills() != null && !application.getApplicationSkills().isEmpty()) {
            List<TeacherApplicationSkillDTO> skills = new ArrayList<>();
            for (TeacherApplicationSkill applicationSkill : application.getApplicationSkills()) {
                TeacherApplicationSkillDTO skillDto = new TeacherApplicationSkillDTO();
                skillDto.setSkillId(applicationSkill.getSkill() != null ? applicationSkill.getSkill().getId() : null);
                skillDto.setWeight(applicationSkill.getWeight());
                skills.add(skillDto);
            }
            dto.setSkills(skills);
        }
        return dto;
    }

    private AdminTeacherApplicationListItemDTO toAdminListItem(TeacherApplication application) {
        AdminTeacherApplicationListItemDTO dto = new AdminTeacherApplicationListItemDTO();
        dto.setId(application.getId());
        if (application.getTeacher() != null) {
            dto.setTeacherId(application.getTeacher().getId());
            dto.setTeacherName(resolveTeacherName(application.getTeacher()));
            dto.setTeacherAccountNo(application.getTeacher().getAccountNo());
        }
        if (application.getCompetition() != null) {
            dto.setCompetitionId(application.getCompetition().getId());
            dto.setCompetitionName(application.getCompetition().getName());
        }
        dto.setTeamName(application.getTeamName());
        dto.setStatus(application.getStatus());
        dto.setCreatedAt(application.getAppliedAt());
        return dto;
    }

    private String resolveTeacherName(User teacher) {
        if (teacher == null) {
            return null;
        }
        if (teacher.getRealName() != null && !teacher.getRealName().isBlank()) {
            return teacher.getRealName();
        }
        if (teacher.getUsername() != null && !teacher.getUsername().isBlank()) {
            return teacher.getUsername();
        }
        return teacher.getAccountNo();
    }

    private void applyApplicationSkills(TeacherApplication application,
                                        List<TeacherApplicationSkillDTO> requestSkills,
                                        boolean clearExisting) {
        if (application == null) {
            return;
        }
        if (clearExisting && application.getId() != null) {
            teacherApplicationSkillRepository.deleteByTeacherApplication_Id(application.getId());
            if (application.getApplicationSkills() != null) {
                application.getApplicationSkills().clear();
            }
        }
        if (requestSkills == null || requestSkills.isEmpty()) {
            return;
        }

        Map<Long, Skill> uniqueSkills = new LinkedHashMap<>();
        Map<Long, Integer> uniqueWeights = new LinkedHashMap<>();
        for (TeacherApplicationSkillDTO skillDTO : requestSkills) {
            if (skillDTO == null || skillDTO.getSkillId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "skillId is required");
            }
            if (uniqueSkills.containsKey(skillDTO.getSkillId())) {
                continue;
            }
            Skill skill = skillRepository.findById(skillDTO.getSkillId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "skill not found: " + skillDTO.getSkillId()));
            uniqueSkills.put(skillDTO.getSkillId(), skill);
            uniqueWeights.put(skillDTO.getSkillId(), skillDTO.getWeight() != null ? skillDTO.getWeight() : 1);
        }

        if (uniqueSkills.isEmpty()) {
            return;
        }

        List<TeacherApplicationSkill> skillRows = new ArrayList<>();
        for (Map.Entry<Long, Skill> entry : uniqueSkills.entrySet()) {
            TeacherApplicationSkill applicationSkill = new TeacherApplicationSkill();
            applicationSkill.setTeacherApplication(application);
            applicationSkill.setSkill(entry.getValue());
            applicationSkill.setWeight(uniqueWeights.get(entry.getKey()));
            skillRows.add(applicationSkill);
        }
        List<TeacherApplicationSkill> savedSkills = teacherApplicationSkillRepository.saveAll(skillRows);
        if (application.getApplicationSkills() != null) {
            application.getApplicationSkills().addAll(savedSkills);
        }
    }
}
