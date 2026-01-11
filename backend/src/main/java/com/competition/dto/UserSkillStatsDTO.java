package com.competition.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserSkillStatsDTO {
    private Long totalSkills;
    private Double averageProficiency;
    private Map<String, Long> categoryStats; // 按分类统计
    private Map<Integer, Long> proficiencyStats; // 按熟练度统计
}