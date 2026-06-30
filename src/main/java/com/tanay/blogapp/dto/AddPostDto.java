package com.tanay.blogapp.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record AddPostDto(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        @URL(message = "Enter a valid URL")
//        @NotBlank(message = "Cover image URL is required")
        String coverImageUrl
) {
}
