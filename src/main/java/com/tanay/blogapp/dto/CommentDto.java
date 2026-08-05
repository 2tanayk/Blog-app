package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentDto(
        @Schema(description = "Unique comment identifier", example = "15")
        Long id,
        @Schema(description = "Comment body text", example = "Great article, thanks for sharing!")
        String content,
        @Schema(description = "Author of the comment")
        PostAuthorDto author,
        @Schema(description = "When the comment was posted", example = "2026-07-27T16:00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
}