package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.repository.PasswordResetTokenRepository;
import com.example.buildnest_ecommerce.repository.RefreshTokenRepository;
import com.example.buildnest_ecommerce.service.inventory.InventoryMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Scheduler service tests")
class SchedulerServiceTest {

    @Test
    @DisplayName("Should invoke inventory monitoring")
    void testInventoryMonitoringScheduler() {
        InventoryMonitoringService monitoringService = mock(InventoryMonitoringService.class);
        InventoryMonitoringScheduler scheduler = new InventoryMonitoringScheduler(monitoringService);

        scheduler.monitorInventoryLevels();
        verify(monitoringService).monitorInventoryLevels();
    }

    @Test
    @DisplayName("Should cleanup tokens")
    void testTokenCleanupScheduler() {
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        PasswordResetTokenRepository passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        when(refreshTokenRepository.deleteExpiredTokens(any())).thenReturn(1);
        when(passwordResetTokenRepository.deleteExpiredTokens(any())).thenReturn(1);

        TokenCleanupScheduler scheduler = new TokenCleanupScheduler(refreshTokenRepository,
                passwordResetTokenRepository);

        scheduler.cleanupExpiredRefreshTokens();
        scheduler.cleanupExpiredPasswordResetTokens();

        verify(refreshTokenRepository).deleteExpiredTokens(any());
        verify(passwordResetTokenRepository).deleteExpiredTokens(any());
    }
}
