package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostAuthorDto(
        @Schema(description = "Author's user ID", example = "7")
        Long id,
        @Schema(description = "Author's display name", example = "John Doe")
        String name
) {
}