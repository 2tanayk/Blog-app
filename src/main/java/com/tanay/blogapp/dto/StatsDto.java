package com.tanay.blogapp.dto;

public record StatsDto(
        long totalUsers,
        long totalPosts,
        long publishedPosts,
        long draftPosts,
        long totalComments,
        long totalTags
) {
}
