package com.competition.controller;

import com.competition.dto.AdminDashboardStatsResponse;
import com.competition.dto.StudentDashboardStatsResponse;
import com.competition.service.DashboardStatsService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DashboardStatsController {

    private final DashboardStatsService dashboardStatsService;
    private final JwtUtils jwtUtils;

    @GetMapping("/student-stats")
    public ResponseEntity<StudentDashboardStatsResponse> getStudentStats(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(dashboardStatsService.getStudentStats(userId));
    }

    @GetMapping("/admin-stats")
    public ResponseEntity<AdminDashboardStatsResponse> getAdminStats(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(dashboardStatsService.getAdminStats(userId));
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
