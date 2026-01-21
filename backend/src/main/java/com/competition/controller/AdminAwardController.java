package com.competition.controller;

import com.competition.dto.AwardPublishRequest;
import com.competition.dto.AwardPublishResponse;
import com.competition.dto.AwardRecordItem;
import com.competition.service.AwardAdminService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @GetMapping("/records")
    public ResponseEntity<List<AwardRecordItem>> listAwardRecords(
            HttpServletRequest request,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = getUserIdFromToken(request);
        List<AwardRecordItem> records = awardAdminService.listAwardRecords(userId, competitionId, teamId, size);
        return ResponseEntity.ok(records);
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
