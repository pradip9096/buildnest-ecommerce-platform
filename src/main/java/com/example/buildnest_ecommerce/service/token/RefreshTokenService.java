package com.example.buildnest_ecommerce.service.token;

import com.example.buildnest_ecommerce.model.entity.RefreshToken;
import com.example.buildnest_ecommerce.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:2592000000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        // Revoke existing active tokens for this user
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshToken.setRevoked(false);

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user ID: {}", userId);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean validateRefreshToken(RefreshToken token) {
        if (token.isRevoked()) {
            log.warn("Attempted to use revoked refresh token: {}", token.getToken());
            return false;
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expired: {}", token.getToken());
            return false;
        }

        return true;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token
        refreshTokenRepository.revokeByToken(oldToken.getToken(), LocalDateTime.now());
        log.info("Revoked old refresh token for user ID: {}", oldToken.getUserId());

        // Create new token
        return createRefreshToken(oldToken.getUserId());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.revokeByToken(token, LocalDateTime.now());
        log.info("Revoked refresh token: {}", token);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        int revokedCount = refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("Revoked {} refresh tokens for user ID: {}", revokedCount, userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired refresh tokens", deletedCount);
        }
    }
}
