package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}