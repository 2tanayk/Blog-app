package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentDto(
        @Schema(description = "Comment text (max 1000 characters)", example = "Great article, thanks for sharing!")
        @NotBlank
        @Size(max = 1000)
        String content
) {
}