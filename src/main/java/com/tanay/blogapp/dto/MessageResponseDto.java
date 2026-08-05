package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageResponseDto(
        @Schema(description = "Human-readable result message", example = "Password updated successfully")
        String message
) {
}