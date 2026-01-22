package com.competition.service;

import com.competition.dto.SkillCreateRequest;
import com.competition.dto.SkillDTO;
import com.competition.dto.SkillUpdateRequest;
import com.competition.entity.Skill;
import com.competition.exception.ApiException;
import com.competition.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * Get all skills.
     */
    public List<SkillDTO> getAllSkills() {
        return getAllSkillsSorted("name");
    }

    /**
     * Get all skills (sorted).
     */
    public List<SkillDTO> getAllSkillsSorted(String sortBy) {
        String sortField = "name";
        if ("createdAt".equalsIgnoreCase(sortBy)) {
            sortField = "createdAt";
        }
        List<Skill> skills = skillRepository.findAll(Sort.by(Sort.Direction.ASC, sortField));
        return skills.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * 根据分类获取技能
     */
    public List<SkillDTO> getSkillsByCategory(String category) {
        List<Skill> skills = skillRepository.findByCategory(category);
        return skills.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有技能分类
     */
    public List<String> getAllCategories() {
        return skillRepository.findDistinctCategories();
    }

    /**
     * 根据ID获取技能
     */
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        return convertToDTO(skill);
    }

    /**
     * 搜索技能
     */
    public List<SkillDTO> searchSkills(String keyword) {
        List<Skill> skills = skillRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword);
        return skills.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create skill.
     */
    @Transactional
    public SkillDTO createSkill(SkillCreateRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "技能名称不能为空");
        }
        String name = request.getName().trim();
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "技能名称已存在");
        }
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            skill.setIsActive(request.getIsActive());
        }
        Skill saved = skillRepository.save(skill);
        return convertToDTO(saved);
    }

    @Transactional
    public SkillDTO updateSkill(Long id, SkillUpdateRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请求不能为空");
        }
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "技能不存在"));
        String currentName = skill.getName();
        String nextName = request.getName() != null ? request.getName().trim() : null;
        if (nextName != null) {
            if (nextName.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "技能名称不能为空");
            }
            if (!nextName.equalsIgnoreCase(currentName) && skillRepository.existsByNameIgnoreCase(nextName)) {
                throw new ApiException(HttpStatus.CONFLICT, "技能名称已存在");
            }
            skill.setName(nextName);
        }
        if (request.getCategory() != null) {
            skill.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }
        Skill saved = skillRepository.save(skill);
        return convertToDTO(saved);
    }

    private SkillDTO convertToDTO(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        dto.setDescription(skill.getDescription());
        dto.setIsActive(skill.getIsActive());
        return dto;
    }
}
