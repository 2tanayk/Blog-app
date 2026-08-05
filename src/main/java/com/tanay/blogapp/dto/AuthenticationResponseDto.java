package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationResponseDto(
        @Schema(description = "JWT access token. Send as `Authorization: Bearer <token>`", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.abc123")
        String token
) {
}