package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @Schema(description = "Display name of the user", example = "John Doe")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Email address used for login", example = "john@example.com")
        @Email(message = "Enter a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Password (minimum 6 characters)", example = "secret123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}