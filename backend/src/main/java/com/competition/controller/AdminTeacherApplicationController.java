package com.competition.controller;

import com.competition.dto.TeacherApplicationResponse;
import com.competition.dto.TeacherApplicationReviewRequest;
import com.competition.entity.TeacherApplication;
import com.competition.service.TeacherApplicationService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin/teacher-applications")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminTeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;
    private final JwtUtils jwtUtils;

    /**
     * Admin list teacher applications.
     * Default sort: appliedAt desc.
     * Response contract: TeacherApplicationResponse list.
     */
    @GetMapping
    public ResponseEntity<List<TeacherApplicationResponse>> getApplications(
            @RequestParam(required = false) TeacherApplication.Status status,
            @RequestParam(required = false) Long competitionId) {
        return ResponseEntity.ok().build();
    }

    /**
     * Admin review teacher application.
     * Response contract: TeacherApplicationResponse.
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<TeacherApplicationResponse> reviewApplication(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody TeacherApplicationReviewRequest request) {
        Long userId = getUserIdFromToken(httpRequest);
        TeacherApplicationResponse response = teacherApplicationService.reviewApplication(userId, id, request);
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
