package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tanay.blogapp.entity.type.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostDto(
        @Schema(description = "Unique post identifier", example = "42")
        Long id,
        @Schema(description = "Post title", example = "Getting Started with Spring Boot")
        String title,
        @Schema(description = "Full post content", example = "Spring Boot makes it easy to create stand-alone...")
        String content,
        @Schema(description = "Cover image URL", example = "https://example.com/images/cover.jpg")
        String coverImageUrl,
        @Schema(description = "Publication status of the post", example = "PUBLISHED")
        PostStatus status,
        @Schema(description = "When the post was created", example = "2026-07-27T15:30:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @Schema(description = "When the post was last updated", example = "2026-07-27T18:00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        @Schema(description = "Author of the post")
        PostAuthorDto author,
        @Schema(description = "Total number of likes", example = "128")
        long likeCount,
        @Schema(description = "Whether the current user has liked this post", example = "true")
        boolean likedByCurrentUser,
        @Schema(description = "List of tag names", example = "[\"java\", \"spring\"]")
        List<String> tags
) {
    public PostDto withLikes(long likeCount, boolean likedByCurrentUser) {
        return new PostDto(
                id, title, content, coverImageUrl,
                status, createdAt, updatedAt,
                author,
                likeCount,
                likedByCurrentUser,
                tags
        );
    }
}