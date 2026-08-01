package com.tanay.blogapp.service;

import com.tanay.blogapp.entity.TokenBlacklist;
import com.tanay.blogapp.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Transactional
    public void blacklistToken(String jti, Date expiresAt) {
        LocalDateTime expiresAtLocal = Instant.ofEpochMilli(expiresAt.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist entry = TokenBlacklist.builder()
                .jti(jti)
                .expiresAt(expiresAtLocal)
                .build();

        tokenBlacklistRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return tokenBlacklistRepository.existsByJti(jti);
    }

    @Transactional
    public void purgeExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}