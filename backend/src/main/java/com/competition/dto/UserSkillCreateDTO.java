package com.competition.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserSkillCreateDTO {
    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    @NotNull(message = "熟练度不能为空")
    @Min(value = 1, message = "熟练度最小值为1")
    @Max(value = 5, message = "熟练度最大值为5")
    private Integer proficiency;
}