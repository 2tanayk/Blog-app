package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tanay.blogapp.entity.type.PostStatus;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String title,
        String content,
        String coverImageUrl,
        PostStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        PostAuthorDto author
) {
}
