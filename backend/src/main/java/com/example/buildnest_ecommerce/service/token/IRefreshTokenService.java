package com.example.buildnest_ecommerce.service.token;

import com.example.buildnest_ecommerce.model.entity.RefreshToken;

import java.util.Optional;

/**
 * Interface for Refresh Token Service operations.
 * Defines contract for refresh token lifecycle management.
 */
public interface IRefreshTokenService {

    /**
     * Create a new refresh token for a user.
     * 
     * @param userId User ID
     * @return Created refresh token
     */
    RefreshToken createRefreshToken(Long userId);

    /**
     * Find refresh token by token string.
     * 
     * @param token Token string
     * @return Optional refresh token
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Validate refresh token.
     * 
     * @param token Refresh token
     * @return true if valid, false otherwise
     */
    boolean validateRefreshToken(RefreshToken token);

    /**
     * Rotate refresh token (revoke old, create new).
     * 
     * @param oldToken Old refresh token
     * @return New refresh token
     */
    RefreshToken rotateRefreshToken(RefreshToken oldToken);

    /**
     * Revoke a refresh token.
     * 
     * @param token Token string to revoke
     */
    void revokeRefreshToken(String token);

    /**
     * Revoke all refresh tokens for a user.
     * 
     * @param userId User ID
     */
    void revokeAllUserTokens(Long userId);

    /**
     * Clean up expired tokens (scheduled task).
     */
    void cleanupExpiredTokens();
}
