package com.example.buildnest_ecommerce.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * Builds the httpOnly access/refresh token cookies for SEC-15 (cookie-based JWT auth).
 * The Secure flag mirrors SecurityConfig's enforceHttps computation so the two never drift apart.
 */
@Component
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final Environment environment;

    @Value("${jwt.expiration:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenExpirationMs;

    public AuthCookieService(Environment environment) {
        this.environment = environment;
    }

    private boolean isSecure() {
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        boolean isProductionProfile = Arrays.asList(environment.getActiveProfiles()).contains("production");
        return sslEnabled || isProductionProfile;
    }

    public ResponseCookie buildAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpirationMs))
                .build();
    }

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
