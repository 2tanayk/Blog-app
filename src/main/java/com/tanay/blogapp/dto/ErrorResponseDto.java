package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "Error message describing what went wrong", example = "Post with id 42 not found")
        String message,
        @Schema(description = "Timestamp of when the error occurred", example = "2026-07-27T15:30:00")
        LocalDateTime timestamp,
        @Schema(description = "Request path that triggered the error", example = "/posts/42")
        String path
) {
}