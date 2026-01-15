package com.competition.controller;

import com.competition.dto.ApplicationResponse;
import com.competition.dto.ApplicationReviewRequest;
import com.competition.service.ApplicationService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/applications")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TeacherTeamApplicationController {

    private final ApplicationService applicationService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> listApplications(
            HttpServletRequest request,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String status) {
        Long userId = getUserIdFromToken(request);
        List<ApplicationResponse> response = applicationService.listForTeacher(userId, teamId, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApplicationResponse> reviewApplication(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ApplicationReviewRequest body) {
        Long userId = getUserIdFromToken(request);
        ApplicationResponse response = applicationService.review(userId, id, body);
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
