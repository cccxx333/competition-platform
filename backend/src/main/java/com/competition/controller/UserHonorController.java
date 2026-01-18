package com.competition.controller;

import com.competition.dto.UserHonorsResponse;
import com.competition.service.UserHonorService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users/me/honors")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserHonorController {

    private final UserHonorService userHonorService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<UserHonorsResponse> getMyHonors(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        UserHonorsResponse response = userHonorService.getMyHonors(userId);
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
