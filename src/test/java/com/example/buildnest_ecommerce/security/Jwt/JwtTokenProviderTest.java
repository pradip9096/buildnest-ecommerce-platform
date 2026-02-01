package com.example.buildnest_ecommerce.security.Jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String testSecret;
    private final long testExpiration = 900000L; // 15 minutes

    @BeforeEach
    void setUp() {
        testSecret = "b".repeat(64);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecretPrevious", "");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", testExpiration);
    }

    @Test
    void testGenerateTokenFromUsername() {
        // Act
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateTokenFromAuthentication() {
        UserDetails principal = new User("testuser", "password", List.of());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testGetUsernameFromToken() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testGetUsernameFromInvalidTokenReturnsNull() {
        String username = jwtTokenProvider.getUsernameFromToken("invalid.jwt.token");

        assertNull(username);
    }

    @Test
    void testValidateToken() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("expiredUser")
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithEmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithNullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithPreviousSecret() {
        String previousSecret = "c".repeat(64);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecretPrevious", previousSecret);

        String tokenWithPrevious = Jwts.builder()
                .subject("legacyUser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(Keys.hmacShaKeyFor(previousSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(jwtTokenProvider.validateToken(tokenWithPrevious));
    }

    @Test
    void testValidateTokenWithInvalidSignature() {
        String otherSecret = "d".repeat(64);
        String tokenWithDifferentSecret = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertFalse(jwtTokenProvider.validateToken(tokenWithDifferentSecret));
    }
}
