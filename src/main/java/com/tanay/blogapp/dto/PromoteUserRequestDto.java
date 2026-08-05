package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PromoteUserRequestDto(
        @Schema(description = "Email of the user to promote to admin", example = "john@example.com")
        @Email(message = "Enter a valid email address")
        @NotBlank(message = "Email is required")
        String email
) {
}