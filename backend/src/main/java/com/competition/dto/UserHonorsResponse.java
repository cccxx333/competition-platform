package com.competition.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserHonorsResponse {
    private Integer participationCount;
    private Integer awardCount;
    private List<AwardDetail> awards;
}
