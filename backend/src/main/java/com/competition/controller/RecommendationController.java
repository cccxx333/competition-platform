package com.competition.controller;

import com.competition.dto.RecommendationResult;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.service.RecommendationService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final JwtUtils jwtUtils;

    /**
     * 获取竞赛推荐
     */
    @GetMapping("/competitions")
    public ResponseEntity<List<RecommendationResult<Competition>>> getCompetitionRecommendations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = getUserIdFromToken(request);
        List<RecommendationResult<Competition>> recommendations =
                recommendationService.recommendCompetitions(userId, limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * 获取队伍推荐
     */
    @GetMapping("/teams")
    public ResponseEntity<List<RecommendationResult<Team>>> getTeamRecommendations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = getUserIdFromToken(request);
        List<RecommendationResult<Team>> recommendations =
                recommendationService.recommendTeams(userId, limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * 获取队员推荐
     */
    @GetMapping("/members/{teamId}")
    public ResponseEntity<List<RecommendationResult<User>>> getMemberRecommendations(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "10") int limit) {

        List<RecommendationResult<User>> recommendations =
                recommendationService.recommendTeamMembers(teamId, limit);

        return ResponseEntity.ok(recommendations);
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