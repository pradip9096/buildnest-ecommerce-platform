package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.repository.PasswordResetTokenRepository;
import com.example.buildnest_ecommerce.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenCleanupSchedulerTest {

    @Test
    void cleanupExpiredTokensInvokesRepositories() {
        RefreshTokenRepository refreshRepo = mock(RefreshTokenRepository.class);
        PasswordResetTokenRepository passwordRepo = mock(PasswordResetTokenRepository.class);
        when(refreshRepo.deleteExpiredTokens(any())).thenReturn(2);
        when(passwordRepo.deleteExpiredTokens(any())).thenReturn(3);

        TokenCleanupScheduler scheduler = new TokenCleanupScheduler(refreshRepo, passwordRepo);
        scheduler.cleanupExpiredRefreshTokens();
        scheduler.cleanupExpiredPasswordResetTokens();

        verify(refreshRepo).deleteExpiredTokens(any());
        verify(passwordRepo).deleteExpiredTokens(any());
    }

    @Test
    void cleanupHandlesExceptions() {
        RefreshTokenRepository refreshRepo = mock(RefreshTokenRepository.class);
        PasswordResetTokenRepository passwordRepo = mock(PasswordResetTokenRepository.class);
        when(refreshRepo.deleteExpiredTokens(any())).thenThrow(new RuntimeException("fail"));
        when(passwordRepo.deleteExpiredTokens(any())).thenThrow(new RuntimeException("fail"));

        TokenCleanupScheduler scheduler = new TokenCleanupScheduler(refreshRepo, passwordRepo);
        assertDoesNotThrow(scheduler::cleanupExpiredRefreshTokens);
        assertDoesNotThrow(scheduler::cleanupExpiredPasswordResetTokens);
    }
}
