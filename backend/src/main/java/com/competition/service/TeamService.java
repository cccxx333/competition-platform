package com.competition.service;

import com.competition.dto.CompetitionDTO;
import com.competition.dto.TeamDTO;
import com.competition.dto.UserDTO;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.repository.TeamRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /**
     * 获取队伍列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<TeamDTO> getTeams(Pageable pageable) {
        Page<Team> teams = teamRepository.findAll(pageable);
        return teams.map(this::convertToDTO);
    }

    /**
     * 根据ID获取队伍
     */
    @Transactional(readOnly = true)
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("队伍不存在"));
    }

    /**
     * 根据ID获取队伍DTO
     */
    @Transactional(readOnly = true)
    public TeamDTO getTeamDTOById(Long id) {
        Team team = getTeamById(id);
        return convertToDTO(team);
    }

    /**
     * 创建队伍
     */
    public TeamDTO createTeam(Long userId, TeamDTO teamDTO) {
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());
        team.setLeader(leader);
        team.setMaxMembers(teamDTO.getMaxMembers());
        team.setCurrentMembers(1);
        team.setStatus(Team.TeamStatus.RECRUITING);

        Team savedTeam = teamRepository.save(team);

        // 添加队长为队伍成员
        TeamMember leaderMember = new TeamMember();
        leaderMember.setTeam(savedTeam);
        leaderMember.setUser(leader);
        leaderMember.setRole(TeamMember.Role.LEADER);
        teamMemberRepository.save(leaderMember);

        return convertToDTO(savedTeam);
    }

    /**
     * 加入队伍
     */
    public void joinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Team team = getTeamById(teamId);

        // 检查队伍状态
        if (team.getStatus() != Team.TeamStatus.RECRUITING) {
            throw new RuntimeException("队伍不在招募状态");
        }

        // 检查是否已满员
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("队伍已满员");
        }

        // 检查是否已是成员
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
        if (isMember) {
            throw new RuntimeException("您已经是该队伍成员");
        }

        // 添加成员
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole(TeamMember.Role.MEMBER);
        teamMemberRepository.save(member);

        // 更新队伍成员数量
        team.setCurrentMembers(team.getCurrentMembers() + 1);
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            team.setStatus(Team.TeamStatus.FULL);
        }
        teamRepository.save(team);
    }

    /**
     * 离开队伍
     */
    public void leaveTeam(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("您不是该队伍成员"));

        if (member.getRole() == TeamMember.Role.LEADER) {
            throw new RuntimeException("队长不能离开队伍，请先转让队长或解散队伍");
        }

        Team team = member.getTeam();
        teamMemberRepository.delete(member);

        // 更新队伍成员数量
        team.setCurrentMembers(team.getCurrentMembers() - 1);
        if (team.getStatus() == Team.TeamStatus.FULL) {
            team.setStatus(Team.TeamStatus.RECRUITING);
        }
        teamRepository.save(team);
    }

    /**
     * 获取可用队伍
     */
    @Transactional(readOnly = true)
    public List<Team> getAvailableTeams() {
        return teamRepository.findAvailableTeams();
    }

    /**
     * 搜索队伍
     */
    @Transactional(readOnly = true)
    public List<TeamDTO> searchTeams(String keyword) {
        List<Team> teams = teamRepository.findByKeyword(keyword);
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取候选用户（用于推荐）
     */
    @Transactional(readOnly = true)
    public List<User> getCandidateUsers(Long teamId) {
        // 简化实现：返回所有用户，实际应该排除已经在队伍中的用户
        return userRepository.findAll();
    }

    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setMaxMembers(team.getMaxMembers());
        dto.setCurrentMembers(team.getCurrentMembers());
        dto.setStatus(team.getStatus());
        dto.setCreatedAt(team.getCreatedAt());

        // 转换关联对象
        if (team.getLeader() != null) {
            dto.setLeader(convertUserToDTO(team.getLeader()));
        }

        if (team.getCompetition() != null) {
            dto.setCompetition(convertCompetitionToDTO(team.getCompetition()));
        }

        return dto;
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
        dto.setCategory(competition.getCategory());
        dto.setLevel(competition.getLevel());
        return dto;
    }
}