package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LikeToggleDto(
        @Schema(description = "Whether the post is now liked by the current user", example = "true")
        Boolean liked,
        @Schema(description = "Updated like count after the toggle", example = "129")
        Long likeCount
) {
}