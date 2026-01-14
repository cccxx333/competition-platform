package com.competition.controller;

import com.competition.dto.SkillCreateRequest;
import com.competition.dto.SkillDTO;
import com.competition.dto.SkillResponse;
import com.competition.dto.SkillUpdateRequest;
import com.competition.entity.Skill;
import com.competition.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class SkillController {

    private final SkillService skillService;

    /**
     * 获取所有技能
     */
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        try {
            List<SkillDTO> skills = skillService.getAllSkills();
            log.info("返回技能数量: {}", skills.size());
            return ResponseEntity.ok(skills.stream()
                    .map(this::toSkillResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("获取技能列表失败: ", e);
            throw e;
        }
    }

    /**
     * 根据分类获取技能
     */
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@RequestBody SkillCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @RequestBody SkillUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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



}
