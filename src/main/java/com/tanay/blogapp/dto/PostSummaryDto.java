package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tanay.blogapp.entity.type.PostStatus;

import java.time.LocalDateTime;

public record PostSummaryDto(
        Long id,
        String title,
        String excerpt,
        String coverImageUrl,
        PostStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        PostAuthorDto author
) {
}