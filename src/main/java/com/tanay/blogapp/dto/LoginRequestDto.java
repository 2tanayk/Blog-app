package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @Schema(description = "Registered email address", example = "john@example.com")
        @Email(message = "Enter a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Account password", example = "secret123")
        @NotBlank(message = "Password is required")
        String password
) {
}