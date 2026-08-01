package com.tanay.blogapp.job;

import com.tanay.blogapp.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistCleanupJob {

    private final TokenBlacklistService tokenBlacklistService;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredBlacklistEntries() {
        log.info("Running scheduled cleanup of expired blacklisted tokens");
        tokenBlacklistService.purgeExpiredTokens();
    }
}