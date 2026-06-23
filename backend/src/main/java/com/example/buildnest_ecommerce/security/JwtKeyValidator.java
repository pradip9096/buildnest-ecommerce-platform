package com.example.buildnest_ecommerce.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * Validates JWT Secret Key configuration for security compliance
 * 
 * Critical Security Requirement (1.1): JWT Secret Key Management
 * - Minimum key length: 512 bits (64 bytes)
 * - Must be provided via environment variable JWT_SECRET
 * - No defaults allowed in production
 * - Rotation recommended every 90 days
 */
@Slf4j
@Component
public class JwtKeyValidator {

    private static final int MINIMUM_KEY_BITS = 512;
    private static final int MINIMUM_KEY_BYTES = MINIMUM_KEY_BITS / 8; // 64 bytes

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Validates JWT secret key on application startup
     * Ensures production-grade security standards
     * 
     * @throws IllegalArgumentException if key is invalid
     */
    public void validateJwtKey() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            String errorMsg = "CRITICAL SECURITY ERROR: JWT_SECRET environment variable is not set. " +
                    "Generate a secure key with: openssl rand -base64 64";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            // Attempt to decode base64 key
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);

            // Validate minimum length
            if (decodedKey.length < MINIMUM_KEY_BYTES) {
                String errorMsg = String.format(
                        "CRITICAL SECURITY ERROR: JWT_SECRET is too short. " +
                                "Current: %d bits, Required: %d bits minimum. " +
                                "Generate with: openssl rand -base64 64",
                        decodedKey.length * 8,
                        MINIMUM_KEY_BITS);
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            log.info("✅ JWT Secret Key validation successful (key length: {} bits)",
                    decodedKey.length * 8);

        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("CRITICAL SECURITY")) {
                throw e;
            }
            String errorMsg = "CRITICAL SECURITY ERROR: JWT_SECRET must be valid base64. " +
                    "Generate with: openssl rand -base64 64";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg, e);
        }
    }
}
