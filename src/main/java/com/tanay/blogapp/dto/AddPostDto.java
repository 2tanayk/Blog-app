package com.tanay.blogapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record AddPostDto(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        @URL(message = "Enter a valid URL")
//        @NotBlank(message = "Cover image URL is required")
        String coverImageUrl,

        @Size(max = 100)
        List<@NotBlank @Size(max = 50) String> tags
) {
}
