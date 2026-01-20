package com.competition.service;

import com.competition.dto.TeamSubmissionResponse;
import com.competition.entity.Team;
import com.competition.entity.TeamSubmission;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.TeamSubmissionRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeamSubmissionService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final TeamSubmissionRepository teamSubmissionRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public TeamSubmissionResponse createSubmission(Long currentUserId, Long teamId, MultipartFile file, String remark) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file is required");
        }
        Team team = loadTeam(teamId);
        if (team.getStatus() == Team.TeamStatus.DISBANDED) {
            throw new ApiException(HttpStatus.CONFLICT, "team is disbanded");
        }
        User currentUser = loadUser(currentUserId);
        enforceTeamAccess(currentUser, team);

        Path baseDir = Paths.get(uploadDir, "teams", String.valueOf(teamId));
        try {
            Files.createDirectories(baseDir);
        } catch (IOException ex) {
            log.error("file dir create failed, teamId={}, baseDir={}", teamId, baseDir, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file save failed");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String storedName = System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;

        Path target = baseDir.resolve(storedName).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("file save failed, teamId={}, target={}", teamId, target, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file save failed");
        }

        teamSubmissionRepository.clearCurrentByTeamId(teamId);

        TeamSubmission submission = new TeamSubmission();
        submission.setTeam(team);
        submission.setCompetition(team.getCompetition());
        submission.setSubmittedBy(currentUser);
        submission.setFileName(original == null || original.isBlank()
                ? storedName
                : Paths.get(original).getFileName().toString());
        submission.setFileUrl("/files/teams/" + teamId + "/" + storedName);
        submission.setRemark(remark);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setIsCurrent(true);

        TeamSubmission saved = teamSubmissionRepository.save(submission);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamSubmissionResponse> listSubmissions(Long currentUserId, Long teamId) {
        Team team = loadTeam(teamId);
        User currentUser = loadUser(currentUserId);
        enforceTeamAccess(currentUser, team);

        return teamSubmissionRepository.findByTeam_IdOrderBySubmittedAtDesc(teamId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamSubmissionResponse getCurrentSubmission(Long currentUserId, Long teamId) {
        Team team = loadTeam(teamId);
        User currentUser = loadUser(currentUserId);
        enforceTeamAccess(currentUser, team);

        TeamSubmission submission = teamSubmissionRepository.findFirstByTeam_IdAndIsCurrentTrue(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "current submission not found"));
        return toResponse(submission);
    }

    private Team loadTeam(Long teamId) {
        if (teamId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "teamId is required");
        }
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));
    }

    private User loadUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private void enforceTeamAccess(User currentUser, Team team) {
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == User.Role.TEACHER) {
            if (team.getLeader() == null || !team.getLeader().getId().equals(currentUser.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team leader");
            }
            return;
        }
        if (currentUser.getRole() == User.Role.STUDENT) {
            boolean isMember = teamMemberRepository
                    .existsByTeamIdAndUserIdAndLeftAtIsNull(team.getId(), currentUser.getId());
            if (!isMember) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team member");
            }
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "no permission");
    }

    private TeamSubmissionResponse toResponse(TeamSubmission submission) {
        TeamSubmissionResponse response = new TeamSubmissionResponse();
        response.setId(submission.getId());
        response.setTeamId(submission.getTeam() != null ? submission.getTeam().getId() : null);
        response.setCompetitionId(submission.getCompetition() != null ? submission.getCompetition().getId() : null);
        response.setSubmittedBy(submission.getSubmittedBy() != null ? submission.getSubmittedBy().getId() : null);
        response.setSubmitterUsername(submission.getSubmittedBy() != null ? submission.getSubmittedBy().getUsername() : null);
        response.setFileName(submission.getFileName());
        response.setFileUrl(submission.getFileUrl());
        response.setRemark(submission.getRemark());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setIsCurrent(submission.getIsCurrent());
        return response;
    }
}
