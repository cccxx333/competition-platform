package com.competition.controller;

import com.competition.dto.AwardPublishRequest;
import com.competition.dto.AwardPublishResponse;
import com.competition.service.AwardAdminService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/awards")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminAwardController {

    private final AwardAdminService awardAdminService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<AwardPublishResponse> publishAward(
            HttpServletRequest request,
            @RequestBody AwardPublishRequest publishRequest) {
        Long userId = getUserIdFromToken(request);
        AwardPublishResponse response = awardAdminService.publishAward(userId, publishRequest);
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
