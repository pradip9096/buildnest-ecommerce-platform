package com.example.buildnest_ecommerce.security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationAndValidation}")
    private String jwtSecret;
    
    // Previous JWT secret for rotation support (optional)
    @Value("${jwt.secret.previous:}")
    private String jwtSecretPrevious;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    private SecretKey getPreviousSigningKey() {
        if (jwtSecretPrevious == null || jwtSecretPrevious.isEmpty()) {
            return null;
        }
        return Keys.hmacShaKeyFor(jwtSecretPrevious.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        org.springframework.security.core.userdetails.UserDetails userPrincipal = 
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error getting username from token", e);
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        // Try to validate with current secret
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException e) {
            // If current secret fails, try previous secret (for rotation support)
            SecretKey previousKey = getPreviousSigningKey();
            if (previousKey != null) {
                try {
                    Jwts.parser()
                            .verifyWith(previousKey)
                            .build()
                            .parseSignedClaims(authToken);
                    log.info("Token validated with previous secret (rotation in progress)");
                    return true;
                } catch (Exception ex) {
                    // Previous secret also failed, log original error
                    log.error("Invalid JWT signature (both current and previous secrets): {}", e.getMessage());
                }
            } else {
                log.error("Invalid JWT signature: {}", e.getMessage());
            }
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
