package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamDiscussionPostResponse {
    private Long id;
    private Long teamId;
    private Long authorId;
    private String authorRealName;
    private String authorUsername;
    private Long parentPostId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
