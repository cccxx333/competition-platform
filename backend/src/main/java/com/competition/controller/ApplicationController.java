package com.competition.controller;

import com.competition.dto.ApplicationCreateRequest;
import com.competition.dto.ApplicationResponse;
import com.competition.service.ApplicationService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            HttpServletRequest request,
            @RequestBody ApplicationCreateRequest body) {
        Long userId = getUserIdFromToken(request);
        ApplicationResponse response = applicationService.createApplication(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
