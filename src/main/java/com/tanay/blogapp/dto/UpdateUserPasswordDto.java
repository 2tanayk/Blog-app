package com.tanay.blogapp.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordDto(
        String currentPassword,
        @NotBlank(message = "New password is invalid")
        String newPassword
) {
}