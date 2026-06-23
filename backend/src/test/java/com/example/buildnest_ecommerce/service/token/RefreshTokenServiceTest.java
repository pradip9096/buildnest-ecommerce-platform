package com.example.buildnest_ecommerce.service.token;

import com.example.buildnest_ecommerce.model.entity.RefreshToken;
import com.example.buildnest_ecommerce.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Should create refresh token and revoke existing")
    void testCreateRefreshToken() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 60000L);

        RefreshToken saved = new RefreshToken();
        saved.setToken("new-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(saved);

        RefreshToken result = refreshTokenService.createRefreshToken(10L);

        assertEquals("new-token", result.getToken());
        verify(refreshTokenRepository).revokeAllByUserId(eq(10L), any(LocalDateTime.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should validate refresh token expiration and revoke flags")
    void testValidateRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("token");
        token.setRevoked(false);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        assertTrue(refreshTokenService.validateRefreshToken(token));

        token.setRevoked(true);
        assertFalse(refreshTokenService.validateRefreshToken(token));

        token.setRevoked(false);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        assertFalse(refreshTokenService.validateRefreshToken(token));
    }

    @Test
    @DisplayName("Should rotate refresh token")
    void testRotateRefreshToken() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old");
        oldToken.setUserId(5L);

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 60000L);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken rotated = refreshTokenService.rotateRefreshToken(oldToken);

        assertNotNull(rotated.getToken());
        verify(refreshTokenRepository).revokeByToken(eq("old"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should find token by value")
    void testFindByToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("find");
        when(refreshTokenRepository.findByToken("find")).thenReturn(Optional.of(token));

        assertTrue(refreshTokenService.findByToken("find").isPresent());
    }

    @Test
    @DisplayName("Should revoke single and all tokens")
    void testRevokeTokens() {
        refreshTokenService.revokeRefreshToken("x");
        verify(refreshTokenRepository).revokeByToken(eq("x"), any(LocalDateTime.class));

        when(refreshTokenRepository.revokeAllByUserId(eq(1L), any(LocalDateTime.class))).thenReturn(2);
        refreshTokenService.revokeAllUserTokens(1L);
        verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should cleanup expired tokens")
    void testCleanupExpiredTokens() {
        when(refreshTokenRepository.deleteExpiredTokens(any(LocalDateTime.class))).thenReturn(1);
        refreshTokenService.cleanupExpiredTokens();
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }
}
