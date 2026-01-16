package com.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamDiscussionPostResponse {
    private Long id;
    private Long teamId;
    private Long authorId;
    private Long parentPostId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
