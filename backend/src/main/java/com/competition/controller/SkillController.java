package com.competition.controller;

import com.competition.dto.SkillDTO;
import com.competition.entity.Skill;
import com.competition.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<SkillDTO>> getAllSkills() {
        try {
            List<SkillDTO> skills = skillService.getAllSkills();
            log.info("返回技能数量: {}", skills.size());
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("获取技能列表失败: ", e);
            throw e;
        }
    }

    /**
     * 根据分类获取技能
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SkillDTO>> getSkillsByCategory(@PathVariable String category) {
        try {
            List<SkillDTO> skills = skillService.getSkillsByCategory(category);
            return ResponseEntity.ok(skills);
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
    public ResponseEntity<SkillDTO> getSkillById(@PathVariable Long id) {
        try {
            SkillDTO skill = skillService.getSkillById(id);
            return ResponseEntity.ok(skill);
        } catch (Exception e) {
            log.error("获取技能详情失败: ", e);
            throw e;
        }
    }

    /**
     * 搜索技能
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillDTO>> searchSkills(@RequestParam String keyword) {
        try {
            List<SkillDTO> skills = skillService.searchSkills(keyword);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("搜索技能失败: ", e);
            throw e;
        }
    }
}