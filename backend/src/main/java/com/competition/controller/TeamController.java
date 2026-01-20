package com.competition.controller;

import com.competition.dto.TeamDTO;
import com.competition.dto.TeamMemberViewResponse;
import com.competition.service.TeamService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TeamController {

    private final TeamService teamService;
    private final JwtUtils jwtUtils;

    /**
     * 获取队伍列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<TeamDTO>> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TeamDTO> teams = teamService.getTeams(pageable);
        return ResponseEntity.ok(teams);
    }

    /**
     * 根据ID获取队伍详情
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        TeamDTO team = teamService.getTeamDTOById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * 创建队伍
     */
    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(
            HttpServletRequest request,
            @RequestBody TeamDTO teamDTO) {
        Long userId = getUserIdFromToken(request);
        TeamDTO created = teamService.createTeam(userId, teamDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * 申请加入队伍
     */
    @PostMapping("/{teamId:\\d+}/join")
    public ResponseEntity<String> joinTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        teamService.joinTeam(userId, teamId);
        return ResponseEntity.ok("申请加入队伍成功");
    }

    /**
     * 离开队伍
     */
    @PostMapping("/{teamId:\\d+}/leave")
    public ResponseEntity<String> leaveTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        teamService.leaveTeam(userId, teamId);
        return ResponseEntity.ok("离开队伍成功");
    }

    /**
     * 获取队伍成员列表（仅在队成员）
     */
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberViewResponse>> listTeamMembers(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        List<TeamMemberViewResponse> members = teamService.listTeamMembers(userId, teamId);
        return ResponseEntity.ok(members);
    }

    /**
     * 移除队伍成员
     */
    @DeleteMapping("/{teamId:\\d+}/members/{userId:\\d+}")
    public ResponseEntity<Map<String, Object>> removeMember(
            HttpServletRequest request,
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        Long currentUserId = getUserIdFromToken(request);
        teamService.removeMember(currentUserId, teamId, userId, reason);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * 结束组队
     */
    @PutMapping("/{teamId:\\d+}/close")
    public ResponseEntity<TeamDTO> closeTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        TeamDTO closedTeam = teamService.closeTeam(userId, teamId);
        return ResponseEntity.ok(closedTeam);
    }

    /**
     * 搜索队伍
     */
    @GetMapping("/search")
    public ResponseEntity<List<TeamDTO>> searchTeams(@RequestParam String keyword) {
        List<TeamDTO> teams = teamService.searchTeams(keyword);
        return ResponseEntity.ok(teams);
    }

    /**
     * 获取当前教师名下队伍
     */
    @GetMapping("/mine")
    public ResponseEntity<List<TeamDTO>> listMyTeams(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword) {
        Long userId = getUserIdFromToken(request);
        List<TeamDTO> teams = teamService.listMyTeams(userId, keyword);
        return ResponseEntity.ok(teams);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("无效的token");
    }
}
