package com.competition.controller;

import com.competition.dto.TeamDiscussionPostCreateRequest;
import com.competition.dto.TeamDiscussionPostResponse;
import com.competition.service.TeamDiscussionPostService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TeamDiscussionPostController {

    private final TeamDiscussionPostService teamDiscussionPostService;
    private final JwtUtils jwtUtils;

    @GetMapping("/{teamId:\\d+}/posts")
    public ResponseEntity<List<TeamDiscussionPostResponse>> listPosts(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        List<TeamDiscussionPostResponse> posts = teamDiscussionPostService.listPosts(userId, teamId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{teamId:\\d+}/posts")
    public ResponseEntity<TeamDiscussionPostResponse> createPost(
            HttpServletRequest request,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamDiscussionPostCreateRequest createRequest) {
        Long userId = getUserIdFromToken(request);
        TeamDiscussionPostResponse created = teamDiscussionPostService.createPost(userId, teamId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("invalid token");
    }
}
