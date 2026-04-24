package com.competition.controller;

import com.competition.dto.ManagedCompetitionTeamDTO;
import com.competition.exception.ApiException;
import com.competition.service.TeamService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/managed-competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TeacherManagedCompetitionController {

    private final TeamService teamService;
    private final JwtUtils jwtUtils;

    @GetMapping("/{competitionId:\\d+}/teams")
    public ResponseEntity<List<ManagedCompetitionTeamDTO>> listManagedCompetitionTeams(
            HttpServletRequest request,
            @PathVariable Long competitionId) {
        Long userId = getUserIdFromToken(request);
        List<ManagedCompetitionTeamDTO> teams = teamService.listManagedCompetitionTeams(userId, competitionId);
        return ResponseEntity.ok(teams);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid token");
    }
}

