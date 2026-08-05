package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateUserProfileDto(
        @Schema(description = "New display name (omit to keep current)", example = "John Updated")
        String name,
        @Schema(description = "New biography (omit to keep current)", example = "Updated bio here!")
        String bio
) {
}