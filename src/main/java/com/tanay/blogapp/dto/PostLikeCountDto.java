package com.tanay.blogapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostLikeCountDto(
        @Schema(description = "Post identifier", example = "42")
        Long postId,
        @Schema(description = "Number of likes on the post", example = "128")
        Long likeCount
) {
}