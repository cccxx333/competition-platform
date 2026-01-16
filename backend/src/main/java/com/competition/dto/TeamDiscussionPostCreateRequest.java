package com.competition.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TeamDiscussionPostCreateRequest {
    @NotBlank(message = "content is required")
    @Size(max = 2000, message = "content is too long")
    private String content;
}
