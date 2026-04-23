package com.competition.controller;

import com.competition.dto.CompetitionCreateRequest;
import com.competition.dto.CompetitionResponse;
import com.competition.dto.CompetitionUpdateRequest;
import com.competition.dto.TeamRecommendationResponse;
import com.competition.entity.Competition;
import com.competition.exception.ApiException;
import com.competition.service.CompetitionService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CompetitionController {

    private final CompetitionService competitionService;
    private final JwtUtils jwtUtils;

    /**
     * 获取竞赛列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<CompetitionResponse>> getCompetitions(
            HttpServletRequest request,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Competition.CompetitionStatus status,
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "false") boolean managedOnly,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean recommend,
            @RequestParam(defaultValue = "false") boolean applyable,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (recommend && applyable) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "参数冲突：算法推荐模式不支持 applyable 过滤");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Long userId = recommend ? tryGetUserId(request) : null;
        Long managerFilterId = managerId;
        if (managedOnly) {
            managerFilterId = tryGetUserId(request);
            if (managerFilterId == null) {
                throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅登录用户可查看负责竞赛");
            }
        }
        Page<CompetitionResponse> competitions = competitionService.getCompetitions(
                pageable, name, status, keyword, recommend, applyable, userId, topK, managerFilterId);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 根据 ID 获取竞赛详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getCompetitionById(@PathVariable Long id) {
        CompetitionResponse competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(competition);
    }

    /**
     * 搜索竞赛
     */
    @GetMapping("/search")
    public ResponseEntity<List<CompetitionResponse>> searchCompetitions(
            @RequestParam String keyword) {
        List<CompetitionResponse> competitions = competitionService.searchCompetitions(keyword);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 根据分类获取竞赛
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CompetitionResponse>> getCompetitionsByCategory(
            @PathVariable String category) {
        List<CompetitionResponse> competitions = competitionService.getCompetitionsByCategory(category);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 创建竞赛
     */
    @PostMapping
    public ResponseEntity<CompetitionResponse> createCompetition(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CompetitionCreateRequest request) {
        Long userId = getAdminUserIdFromToken(httpRequest);
        CompetitionResponse created = competitionService.createCompetition(userId, request);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新竞赛
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> updateCompetition(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @Valid @RequestBody CompetitionUpdateRequest request) {
        Long userId = getAdminUserIdFromToken(httpRequest);
        CompetitionResponse updated = competitionService.updateCompetition(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 获取可用竞赛（公开接口）
     */
    @GetMapping("/public/available")
    public ResponseEntity<List<CompetitionResponse>> getAvailableCompetitions() {
        List<CompetitionResponse> competitions = competitionService.getAvailableCompetitions();
        return ResponseEntity.ok(competitions);
    }

    @GetMapping("/{competitionId}/teams/recommend")
    public ResponseEntity<List<TeamRecommendationResponse>> recommendTeams(
            HttpServletRequest request,
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "10") int topK) {
        Long userId = getUserIdFromToken(request);
        List<TeamRecommendationResponse> responses = competitionService.recommendTeams(userId, competitionId, topK);
        return ResponseEntity.ok(responses);
    }

    private Long tryGetUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                return jwtUtils.getUserIdFromToken(token.substring(7));
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtils.getUserIdFromToken(token);
            } catch (Exception ex) {
                throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅学生可进行竞赛报名推荐");
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅学生可进行竞赛报名推荐");
    }

    private Long getAdminUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtils.getUserIdFromToken(token);
            } catch (Exception ex) {
                throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅管理员可修改竞赛状态");
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅管理员可修改竞赛状态");
    }
}
