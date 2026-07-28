package com.tanay.blogapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserDetailsDto(
        Long id,
        String name,
        String email,
        String bio,
        String providerType,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime joinedDate
) {
}