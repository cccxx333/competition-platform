package com.competition.controller;

import com.competition.dto.AdminTeacherApplicationListItemDTO;
import com.competition.dto.TeacherApplicationResponse;
import com.competition.dto.TeacherApplicationReviewRequest;
import com.competition.entity.TeacherApplication;
import com.competition.service.TeacherApplicationService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

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
     * Response contract: AdminTeacherApplicationListItemDTO page.
     */
    @GetMapping
    public ResponseEntity<Page<AdminTeacherApplicationListItemDTO>> getApplications(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TeacherApplication.Status status,
            @RequestParam(required = false) String keyword) {
        Long userId = getUserIdFromToken(httpRequest);
        Sort sort = Sort.by("appliedAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AdminTeacherApplicationListItemDTO> result = teacherApplicationService
                .listAdminApplications(userId, status, keyword, pageable);
        return ResponseEntity.ok(result);
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
