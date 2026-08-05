package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record AddPostDto(
        @Schema(description = "Post title", example = "Getting Started with Spring Boot")
        @NotBlank(message = "Title is required")
        String title,
        @Schema(description = "Full post content (Markdown supported)", example = "Spring Boot makes it easy to create stand-alone...")
        @NotBlank(message = "Content is required")
        String content,
        @Schema(description = "Optional cover image URL", example = "https://example.com/images/cover.jpg")
        @URL(message = "Enter a valid URL")
//        @NotBlank(message = "Cover image URL is required")
        String coverImageUrl,

        @Schema(description = "Tags to attach to the post (max 100)", example = "[\"java\", \"spring\", \"tutorial\"]")
        @Size(max = 100)
        List<@NotBlank @Size(max = 50) String> tags
) {
}