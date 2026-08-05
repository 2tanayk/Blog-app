package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tanay.blogapp.entity.type.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PostSummaryDto(
        @Schema(description = "Unique post identifier", example = "42")
        Long id,
        @Schema(description = "Post title", example = "Getting Started with Spring Boot")
        String title,
        @Schema(description = "Short excerpt/summary of the post", example = "A quick introduction to Spring Boot...")
        String excerpt,
        @Schema(description = "Cover image URL", example = "https://example.com/images/cover.jpg")
        String coverImageUrl,
        @Schema(description = "Publication status of the post", example = "PUBLISHED")
        PostStatus status,
        @Schema(description = "When the post was created", example = "2026-07-27T15:30:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @Schema(description = "Author of the post")
        PostAuthorDto author,
        @Schema(description = "Total number of likes", example = "128")
        long likeCount
) {
    public PostSummaryDto withLikeCount(long likeCount) {
        return new PostSummaryDto(
                id, title, excerpt, coverImageUrl,
                status, createdAt,
                author,
                likeCount
        );
    }
}