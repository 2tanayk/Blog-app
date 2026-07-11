package com.tanay.blogapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentDto(
        @NotBlank
        @Size(max = 1000)
        String content
) {
}