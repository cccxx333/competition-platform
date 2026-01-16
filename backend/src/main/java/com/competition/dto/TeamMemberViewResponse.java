package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMemberViewResponse {
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private LocalDateTime joinedAt;
}
