package com.competition.dto;

import lombok.Data;
import java.util.List;

@Data
public class SimpleUserDTO {
    private Long id;
    private String username;
    private String realName;
    private String school;
    private String major;
    private List<SimpleUserSkillDTO> skills;
}