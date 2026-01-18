package com.competition.controller;

import com.competition.dto.CompetitionAdminUpdateRequest;
import com.competition.dto.CompetitionResponse;
import com.competition.entity.Competition;
import com.competition.service.AdminCompetitionService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/competitions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminCompetitionController {

    private final AdminCompetitionService adminCompetitionService;
    private final JwtUtils jwtUtils;

    @PutMapping("/{competitionId}")
    public ResponseEntity<CompetitionResponse> updateCompetition(
            HttpServletRequest request,
            @PathVariable Long competitionId,
            @RequestBody CompetitionAdminUpdateRequest updateRequest) {
        Long userId = getUserIdFromToken(request);
        Competition updated = adminCompetitionService.updateCompetition(userId, competitionId, updateRequest);
        return ResponseEntity.ok(toResponse(updated));
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("invalid token");
    }

    private CompetitionResponse toResponse(Competition competition) {
        CompetitionResponse response = new CompetitionResponse();
        response.setId(competition.getId());
        response.setName(competition.getName());
        response.setDescription(competition.getDescription());
        response.setOrganizer(competition.getOrganizer());
        response.setStartDate(competition.getStartDate());
        response.setEndDate(competition.getEndDate());
        response.setRegistrationDeadline(competition.getRegistrationDeadline());
        response.setMinTeamSize(competition.getMinTeamSize());
        response.setMaxTeamSize(competition.getMaxTeamSize());
        response.setCategory(competition.getCategory());
        response.setLevel(competition.getLevel());
        response.setStatus(competition.getStatus());
        response.setCreatedById(competition.getCreatedBy() != null ? competition.getCreatedBy().getId() : null);
        response.setCreatedAt(competition.getCreatedAt());
        response.setUpdatedAt(competition.getUpdatedAt());
        return response;
    }
}
