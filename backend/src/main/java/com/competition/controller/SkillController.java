package com.competition.controller;

import com.competition.dto.SkillCreateRequest;
import com.competition.dto.SkillDTO;
import com.competition.dto.SkillResponse;
import com.competition.dto.SkillUpdateRequest;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.UserRepository;
import com.competition.service.SkillService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class SkillController {

    private final SkillService skillService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    /**
     * 获取所有技能
     */
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills(
            @RequestParam(value = "sortBy", required = false, defaultValue = "name") String sortBy) {
        try {
            List<SkillDTO> skills = skillService.getAllSkillsSorted(sortBy);
            log.info("Skills size {}", skills.size());
            return ResponseEntity.ok(skills.stream()
                    .map(this::toSkillResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Failed to get skills", e);
            throw e;
        }
    }


    /**
     * 根据分类获取技能
     */
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(
            HttpServletRequest httpRequest,
            @RequestBody SkillCreateRequest request) {
        requireAdmin(httpRequest);
        SkillDTO skill = skillService.createSkill(request);
        return ResponseEntity.ok(toSkillResponse(skill));
    }


    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody SkillUpdateRequest request) {
        requireAdmin(httpRequest);
        SkillDTO skill = skillService.updateSkill(id, request);
        return ResponseEntity.ok(toSkillResponse(skill));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SkillResponse>> getSkillsByCategory(@PathVariable String category) {
        try {
            List<SkillDTO> skills = skillService.getSkillsByCategory(category);
            return ResponseEntity.ok(skills.stream()
                    .map(this::toSkillResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("根据分类获取技能失败: ", e);
            throw e;
        }
    }

    /**
     * 获取所有技能分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        try {
            List<String> categories = skillService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("获取技能分类失败: ", e);
            throw e;
        }
    }

    /**
     * 根据ID获取技能详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        try {
            SkillDTO skill = skillService.getSkillById(id);
            return ResponseEntity.ok(toSkillResponse(skill));
        } catch (Exception e) {
            log.error("获取技能详情失败: ", e);
            throw e;
        }
    }

    /**
     * 搜索技能
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(@RequestParam String keyword) {
        try {
            List<SkillDTO> skills = skillService.searchSkills(keyword);
            return ResponseEntity.ok(skills.stream()
                    .map(this::toSkillResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("搜索技能失败: ", e);
            throw e;
        }
    }

    private SkillResponse toSkillResponse(SkillDTO dto) {
        SkillResponse response = new SkillResponse();
        response.setId(dto.getId());
        response.setName(dto.getName());
        response.setCategory(dto.getCategory());
        response.setDescription(dto.getDescription());
        response.setIsActive(dto.getIsActive());
        return response;
    }

    private void requireAdmin(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (user.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅管理员可操作技能");
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "缺少登录信息");
    }



}
