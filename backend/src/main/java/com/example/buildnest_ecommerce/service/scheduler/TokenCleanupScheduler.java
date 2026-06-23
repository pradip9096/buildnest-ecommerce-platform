package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.repository.PasswordResetTokenRepository;
import com.example.buildnest_ecommerce.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task service for token cleanup and maintenance.
 * Runs periodic cleanup of expired tokens to maintain database hygiene.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Clean up expired refresh tokens.
     * Runs every 24 hours at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Cleaned up {} expired refresh tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during refresh token cleanup", e);
        }
    }
    
    /**
     * Clean up expired password reset tokens.
     * Runs every 6 hours.
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        try {
            int deletedCount = passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Cleaned up {} expired password reset tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during password reset token cleanup", e);
        }
    }
}
