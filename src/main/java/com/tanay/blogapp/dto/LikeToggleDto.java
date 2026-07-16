package com.tanay.blogapp.dto;

public record LikeToggleDto(
        Boolean liked,
        Long likeCount
) {
}
