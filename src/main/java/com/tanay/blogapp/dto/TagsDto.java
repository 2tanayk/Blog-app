package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TagsDto(
        @Schema(description = "List of all available tag names", example = "[\"java\", \"spring\", \"web\"]")
        List<String> tags
) {
}