package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatsDto(
        @Schema(description = "Total number of registered users", example = "1024")
        long totalUsers,
        @Schema(description = "Total number of posts (all statuses)", example = "3200")
        long totalPosts,
        @Schema(description = "Number of published posts", example = "2800")
        long publishedPosts,
        @Schema(description = "Number of draft posts", example = "400")
        long draftPosts,
        @Schema(description = "Total number of comments across all posts", example = "15000")
        long totalComments,
        @Schema(description = "Total number of unique tags", example = "87")
        long totalTags
) {
}