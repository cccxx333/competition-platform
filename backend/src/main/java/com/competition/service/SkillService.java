package com.competition.service;

import com.competition.dto.SkillDTO;
import com.competition.entity.Skill;
import com.competition.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 获取所有技能
     */
    public List<SkillDTO> getAllSkills() {
        List<Skill> skills = skillRepository.findAll();
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
     * 转换为DTO
     */
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
