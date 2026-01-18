package com.competition.service;

import com.competition.dto.CompetitionDTO;
import com.competition.dto.TeamDTO;
import com.competition.dto.UserDTO;
import com.competition.dto.TeamMemberViewResponse;
import com.competition.entity.Competition;
import com.competition.entity.Application;
import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.repository.ApplicationRepository;
import com.competition.exception.ApiException;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public Page<TeamDTO> getTeams(Pageable pageable) {
        Page<Team> teams = teamRepository.findAll(pageable);
        return teams.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("闃熶紞涓嶅瓨鍦�"));
    }

    @Transactional(readOnly = true)
    public TeamDTO getTeamDTOById(Long id) {
        Team team = getTeamById(id);
        return convertToDTO(team);
    }

    public TeamDTO createTeam(Long userId, TeamDTO teamDTO) {
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("鐢ㄦ埛涓嶅瓨鍦�"));

        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());
        team.setLeader(leader);
        team.setStatus(Team.TeamStatus.RECRUITING);

        Team savedTeam = teamRepository.save(team);

        TeamMember leaderMember = new TeamMember();
        leaderMember.setTeam(savedTeam);
        leaderMember.setUser(leader);
        teamMemberRepository.save(leaderMember);

        return convertToDTO(savedTeam);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberViewResponse> listTeamMembers(Long currentUserId, Long teamId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));

        if (currentUser.getRole() == User.Role.TEACHER) {
            if (team.getLeader() == null || !team.getLeader().getId().equals(currentUserId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team leader");
            }
        } else if (currentUser.getRole() == User.Role.STUDENT) {
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserIdAndLeftAtIsNull(teamId, currentUserId);
            if (!isMember) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team member");
            }
        }

        return teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId).stream()
                .map(this::toMemberView)
                .collect(Collectors.toList());
    }

    public void joinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));

        if (team.getStatus() == Team.TeamStatus.DISBANDED) {
            throw new ApiException(HttpStatus.CONFLICT, "team is disbanded");
        }
        if (team.getStatus() != Team.TeamStatus.RECRUITING) {
            throw new ApiException(HttpStatus.CONFLICT, "team is not recruiting");
        }

        long currentMembers = teamMemberRepository.countByTeamIdAndLeftAtIsNull(teamId);
        Integer maxSize = team.getCompetition() != null ? team.getCompetition().getMaxTeamSize() : null;
        if (maxSize != null && currentMembers >= maxSize) {
            throw new RuntimeException("team is full");
        }

        boolean isMember = teamMemberRepository.existsByTeamIdAndUserIdAndLeftAtIsNull(teamId, userId);
        if (isMember) {
            throw new RuntimeException("already in team");
        }

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        teamMemberRepository.save(member);

        if (maxSize != null && currentMembers + 1 >= maxSize) {
            team.setStatus(Team.TeamStatus.CLOSED);
            teamRepository.save(team);
        }
    }

    public void leaveTeam(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("鎮ㄤ笉鏄闃熶紞鎴愬憳"));

        Team team = member.getTeam();
        if (team.getStatus() == Team.TeamStatus.DISBANDED) {
            throw new ApiException(HttpStatus.CONFLICT, "team is disbanded");
        }
        if (team.getLeader() != null && team.getLeader().getId().equals(userId)) {
            throw new RuntimeException("闃熼暱涓嶈兘绂诲紑闃熶紞锛岃鍏堣浆璁╅槦闀挎垨瑙ｆ暎闃熶紞");
        }

        teamMemberRepository.delete(member);

        Integer maxSize = team.getCompetition() != null ? team.getCompetition().getMaxTeamSize() : null;
        if (team.getStatus() == Team.TeamStatus.CLOSED) {
            long currentMembers = teamMemberRepository.countByTeamId(teamId);
            if (maxSize == null || currentMembers < maxSize) {
                team.setStatus(Team.TeamStatus.RECRUITING);
                teamRepository.save(team);
            }
        }
    }

    public TeamDTO closeTeam(Long currentUserId, Long teamId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (currentUser.getRole() == User.Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN or TEACHER can close team");
        }

        Team team = getTeamById(teamId);
        if (team.getStatus() == Team.TeamStatus.CLOSED) {
            throw new ApiException(HttpStatus.CONFLICT, "team already closed");
        }
        if (team.getStatus() == Team.TeamStatus.DISBANDED) {
            throw new ApiException(HttpStatus.CONFLICT, "team is disbanded");
        }

        if (currentUser.getRole() == User.Role.TEACHER) {
            if (team.getLeader() == null || !team.getLeader().getId().equals(currentUserId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "only team leader can close team");
            }
        }

        team.setStatus(Team.TeamStatus.CLOSED);
        team.setClosedAt(LocalDateTime.now());
        team.setClosedBy(currentUser);
        team.setUpdatedAt(LocalDateTime.now());
        Team saved = teamRepository.save(team);

        return convertToDTO(saved);
    }

    public void removeMember(Long currentUserId, Long teamId, Long userId, String reason) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (currentUser.getRole() == User.Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN or TEACHER can remove member");
        }

        Team team = getTeamById(teamId);
        if (team.getStatus() == Team.TeamStatus.DISBANDED) {
            throw new ApiException(HttpStatus.CONFLICT, "team is disbanded");
        }
        if (team.getStatus() == Team.TeamStatus.CLOSED && currentUser.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can remove member from closed team");
        }
        if (currentUser.getRole() == User.Role.TEACHER) {
            if (team.getLeader() == null || !team.getLeader().getId().equals(currentUserId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "only team leader can remove member");
            }
        }

        if (team.getLeader() != null && team.getLeader().getId().equals(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "cannot remove team leader");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserIdAndLeftAtIsNull(teamId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "member already removed"));

        LocalDateTime now = LocalDateTime.now();
        member.setLeftAt(now);
        teamMemberRepository.save(member);

        if (team.getStatus() == Team.TeamStatus.CLOSED && team.getCompetition() != null) {
            Integer minSize = team.getCompetition().getMinTeamSize();
            if (minSize != null) {
                long remainingCount = teamMemberRepository.countByTeamIdAndLeftAtIsNull(teamId);
                if (remainingCount < minSize) {
                    team.setStatus(Team.TeamStatus.DISBANDED);
                    teamRepository.save(team);
                }
            }
        }

        if (team.getCompetition() != null) {
            Long competitionId = team.getCompetition().getId();
            Application application = applicationRepository
                    .findFirstByStudent_IdAndCompetition_IdAndTeam_IdAndStatusAndIsActiveTrue(
                            userId,
                            competitionId,
                            teamId,
                            Application.Status.APPROVED
                    )
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "approved active application not found"));
            applicationRepository.deleteAllInactiveByStudentAndCompetition(userId, competitionId);
            application.setStatus(Application.Status.REMOVED);
            application.setIsActive(false);
            application.setRemovedAt(now);
            application.setRemovedBy(currentUser);
            application.setReason(reason);
            try {
                applicationRepository.save(application);
            } catch (DataIntegrityViolationException ex) {
                throw new ApiException(HttpStatus.CONFLICT, "application state conflict, please retry");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Team> getAvailableTeams() {
        List<Team> recruitingTeams = teamRepository.findAvailableTeams();
        return recruitingTeams.stream()
                .filter(team -> {
                    Integer maxSize = team.getCompetition() != null ? team.getCompetition().getMaxTeamSize() : null;
                    if (maxSize == null) {
                        return true;
                    }
                    long count = team.getTeamMembers() != null ? team.getTeamMembers().size() : 0;
                    return count < maxSize;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> searchTeams(String keyword) {
        List<Team> teams = teamRepository.findByKeyword(keyword);
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getCandidateUsers(Long teamId) {
        return userRepository.findAll();
    }

    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setStatus(team.getStatus());
        dto.setCreatedAt(team.getCreatedAt());

        Integer maxSize = team.getCompetition() != null ? team.getCompetition().getMaxTeamSize() : null;
        dto.setMaxMembers(maxSize);
        int currentMemberCount = team.getTeamMembers() != null ? team.getTeamMembers().size() : 0;
        dto.setCurrentMembers(currentMemberCount);

        if (team.getLeader() != null) {
            dto.setLeader(convertUserToDTO(team.getLeader()));
        }

        if (team.getCompetition() != null) {
            dto.setCompetition(convertCompetitionToDTO(team.getCompetition()));
        }

        return dto;
    }

    private TeamMemberViewResponse toMemberView(TeamMember member) {
        TeamMemberViewResponse response = new TeamMemberViewResponse();
        if (member.getUser() != null) {
            response.setUserId(member.getUser().getId());
            response.setUsername(member.getUser().getUsername());
            response.setRealName(member.getUser().getRealName());
        }
        response.setRole(member.getRole() != null ? member.getRole().name() : null);
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    private CompetitionDTO convertCompetitionToDTO(Competition competition) {
        CompetitionDTO dto = new CompetitionDTO();
        dto.setId(competition.getId());
        dto.setName(competition.getName());
        dto.setDescription(competition.getDescription());
        dto.setOrganizer(competition.getOrganizer());
        dto.setStartDate(competition.getStartDate());
        dto.setEndDate(competition.getEndDate());
        dto.setRegistrationDeadline(competition.getRegistrationDeadline());
        dto.setMaxTeamSize(competition.getMaxTeamSize());
        dto.setStatus(competition.getStatus());
        return dto;
    }
}
