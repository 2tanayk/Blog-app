package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordDto(
        @Schema(description = "Current password for verification", example = "oldPass123")
        String currentPassword,
        @Schema(description = "New password (minimum 6 characters)", example = "newSecurePass456")
        @NotBlank(message = "New password is invalid")
        String newPassword
) {
}