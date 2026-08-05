package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserDetailsDto(
        @Schema(description = "Unique user identifier", example = "7")
        Long id,
        @Schema(description = "Display name of the user", example = "John Doe")
        String name,
        @Schema(description = "Email address", example = "john@example.com")
        String email,
        @Schema(description = "Short user biography", example = "I love writing about tech!")
        String bio,
        @Schema(description = "Authentication provider type", example = "EMAIL")
        String providerType,
        @Schema(description = "When the user joined", example = "2026-01-15T10:00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime joinedDate
) {
}