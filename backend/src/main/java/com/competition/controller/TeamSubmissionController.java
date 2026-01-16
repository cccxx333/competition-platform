package com.competition.controller;

import com.competition.dto.TeamSubmissionResponse;
import com.competition.service.TeamSubmissionService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TeamSubmissionController {

    private final TeamSubmissionService teamSubmissionService;
    private final JwtUtils jwtUtils;

    @PostMapping("/{teamId:\\d+}/submissions")
    public ResponseEntity<TeamSubmissionResponse> createSubmission(
            HttpServletRequest request,
            @PathVariable Long teamId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "remark", required = false) String remark) {
        Long userId = getUserIdFromToken(request);
        TeamSubmissionResponse created = teamSubmissionService.createSubmission(userId, teamId, file, remark);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{teamId:\\d+}/submissions")
    public ResponseEntity<List<TeamSubmissionResponse>> listSubmissions(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        List<TeamSubmissionResponse> responses = teamSubmissionService.listSubmissions(userId, teamId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{teamId:\\d+}/submissions/current")
    public ResponseEntity<TeamSubmissionResponse> getCurrentSubmission(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long userId = getUserIdFromToken(request);
        TeamSubmissionResponse response = teamSubmissionService.getCurrentSubmission(userId, teamId);
        return ResponseEntity.ok(response);
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
