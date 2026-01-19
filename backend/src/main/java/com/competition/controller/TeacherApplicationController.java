package com.competition.controller;

import com.competition.dto.TeacherApplicationCreateRequest;
import com.competition.dto.TeacherApplicationListItemDTO;
import com.competition.dto.TeacherApplicationResponse;
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
@RequestMapping("/api/teacher-applications")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;
    private final JwtUtils jwtUtils;

    /**
     * Submit teacher application.
     * Response contract: TeacherApplicationResponse.
     */
    @PostMapping
    public ResponseEntity<TeacherApplicationResponse> createApplication(
            HttpServletRequest httpRequest,
            @RequestBody TeacherApplicationCreateRequest request) {
        Long userId = getUserIdFromToken(httpRequest);
        TeacherApplicationResponse response = teacherApplicationService.createApplication(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TeacherApplicationListItemDTO>> listMyApplications(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TeacherApplication.Status status) {
        Long userId = getUserIdFromToken(httpRequest);
        Sort sort = Sort.by("appliedAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeacherApplicationListItemDTO> result = teacherApplicationService.listMyApplications(userId, status, pageable);
        return ResponseEntity.ok(result);
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
