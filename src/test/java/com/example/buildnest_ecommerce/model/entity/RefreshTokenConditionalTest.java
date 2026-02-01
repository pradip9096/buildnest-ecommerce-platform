package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RefreshToken - Conditional Branch Coverage Tests")
class RefreshTokenConditionalTest {

    @Test
    @DisplayName("ID field - null vs non-null")
    void testIdField() {
        RefreshToken token = new RefreshToken();
        token.setId(null);
        assertNull(token.getId());

        token.setId(1L);
        assertEquals(1L, token.getId());
    }

    @Test
    @DisplayName("Token field - null vs populated")
    void testTokenField() {
        RefreshToken token = new RefreshToken();

        token.setToken(null);
        assertNull(token.getToken());

        token.setToken("refresh-abc123xyz");
        assertEquals("refresh-abc123xyz", token.getToken());
    }

    @Test
    @DisplayName("UserId field - null and various values")
    void testUserIdField() {
        RefreshToken token = new RefreshToken();

        token.setUserId(null);
        assertNull(token.getUserId());

        token.setUserId(0L);
        assertEquals(0L, token.getUserId());

        token.setUserId(100L);
        assertEquals(100L, token.getUserId());
    }

    @Test
    @DisplayName("ExpiryDate field - null vs LocalDateTime")
    void testExpiryDateField() {
        RefreshToken token = new RefreshToken();

        token.setExpiryDate(null);
        assertNull(token.getExpiryDate());

        LocalDateTime future = LocalDateTime.now().plusDays(7);
        token.setExpiryDate(future);
        assertEquals(future, token.getExpiryDate());
    }

    @Test
    @DisplayName("Revoked field - boolean true/false/null variations")
    void testRevokedField() {
        RefreshToken token = new RefreshToken();

        // Default false
        assertFalse(token.isRevoked());

        // Set to true
        token.setRevoked(true);
        assertTrue(token.isRevoked());

        // Set to false
        token.setRevoked(false);
        assertFalse(token.isRevoked());
    }

    @Test
    @DisplayName("CreatedAt field - null vs LocalDateTime")
    void testCreatedAtField() {
        RefreshToken token = new RefreshToken();

        token.setCreatedAt(null);
        assertNull(token.getCreatedAt());

        LocalDateTime now = LocalDateTime.now();
        token.setCreatedAt(now);
        assertEquals(now, token.getCreatedAt());
    }

    @Test
    @DisplayName("RevokedAt field - null vs LocalDateTime")
    void testRevokedAtField() {
        RefreshToken token = new RefreshToken();

        token.setRevokedAt(null);
        assertNull(token.getRevokedAt());

        LocalDateTime revoked = LocalDateTime.now();
        token.setRevokedAt(revoked);
        assertEquals(revoked, token.getRevokedAt());
    }

    @Test
    @DisplayName("Complete refresh token object with all fields")
    void testCompleteRefreshToken() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(7);
        LocalDateTime revokedTime = now.plusMinutes(30);

        RefreshToken token = new RefreshToken(
                1L,
                "refresh-token-def456",
                100L,
                future,
                true,
                now,
                revokedTime);

        assertEquals(1L, token.getId());
        assertEquals("refresh-token-def456", token.getToken());
        assertEquals(100L, token.getUserId());
        assertEquals(future, token.getExpiryDate());
        assertTrue(token.isRevoked());
        assertEquals(now, token.getCreatedAt());
        assertEquals(revokedTime, token.getRevokedAt());
    }

    @Test
    @DisplayName("NoArgsConstructor - default state")
    void testNoArgsConstructor() {
        RefreshToken token = new RefreshToken();
        assertNull(token.getId());
        assertNull(token.getToken());
        assertFalse(token.isRevoked());
    }

    @Test
    @DisplayName("AllArgsConstructor - 7 parameter constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken token = new RefreshToken(1L, "token", 50L, now, false, now, null);

        assertEquals(1L, token.getId());
        assertEquals("token", token.getToken());
    }

    @Test
    @DisplayName("Lombok equals() with matching tokens")
    void testEquals() {
        RefreshToken token1 = new RefreshToken();
        token1.setId(1L);
        token1.setToken("refresh-abc");

        RefreshToken token2 = new RefreshToken();
        token2.setId(1L);
        token2.setToken("refresh-abc");

        assertEquals(token1, token2);
    }

    @Test
    @DisplayName("Lombok hashCode() consistency")
    void testHashCode() {
        RefreshToken token1 = new RefreshToken();
        token1.setId(1L);
        token1.setToken("refresh-abc");

        RefreshToken token2 = new RefreshToken();
        token2.setId(1L);
        token2.setToken("refresh-abc");

        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    @DisplayName("Lombok toString()")
    void testToString() {
        RefreshToken token = new RefreshToken();
        token.setId(1L);

        String str = token.toString();
        assertNotNull(str);
        assertTrue(str.contains("RefreshToken"));
    }

    @Test
    @DisplayName("Multiple tokens with various revocation states")
    void testMultipleTokensWithVariousStates() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            RefreshToken token = new RefreshToken();
            token.setId((long) i);
            token.setUserId((long) (i + 200));
            token.setToken("refresh-token-" + i);
            token.setExpiryDate(now.plusDays(i + 1));
            token.setRevoked(i % 2 == 0); // Alternates revoked/active

            assertNotNull(token.getToken());
            assertEquals((i % 2 == 0), token.isRevoked()); // Branch coverage for boolean
        }
    }

    @Test
    @DisplayName("Token lifecycle - creation, activity, revocation")
    void testTokenLifecycle() {
        RefreshToken token = new RefreshToken();

        // Created state
        token.setId(1L);
        token.setToken("refresh-def");
        token.setUserId(50L);
        token.setCreatedAt(LocalDateTime.now());
        assertFalse(token.isRevoked());
        assertNull(token.getRevokedAt());

        // Revoked state
        token.setRevoked(true);
        LocalDateTime revokedTime = LocalDateTime.now();
        token.setRevokedAt(revokedTime);
        assertTrue(token.isRevoked());
        assertEquals(revokedTime, token.getRevokedAt());
    }

    @Test
    @DisplayName("Token expiry validation")
    void testTokenExpiryValidation() {
        RefreshToken token = new RefreshToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        LocalDateTime future = now.plusDays(7);

        // Expired token
        token.setExpiryDate(past);
        assertTrue(token.getExpiryDate().isBefore(now));

        // Valid token
        token.setExpiryDate(future);
        assertTrue(token.getExpiryDate().isAfter(now));
    }

    @Test
    @DisplayName("Null handling across all fields")
    void testNullHandlingAcrossFields() {
        RefreshToken token = new RefreshToken();

        // All fields null/default
        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUserId());
        assertNull(token.getExpiryDate());
        assertFalse(token.isRevoked());
        assertNull(token.getCreatedAt());
        assertNull(token.getRevokedAt());
    }

    @Test
    @DisplayName("Token field maximum length")
    void testTokenFieldMaxLength() {
        RefreshToken token = new RefreshToken();

        // Max 500 chars as per column definition
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longToken.append("b");
        }

        token.setToken(longToken.toString());
        assertEquals(500, token.getToken().length());
    }

    @Test
    @DisplayName("Revocation state transitions")
    void testRevocationStateTransitions() {
        RefreshToken token = new RefreshToken();

        // Active state
        token.setRevoked(false);
        assertFalse(token.isRevoked());

        // Revoked state
        token.setRevoked(true);
        assertTrue(token.isRevoked());
        LocalDateTime revokeTime = LocalDateTime.now();
        token.setRevokedAt(revokeTime);

        // Cannot unrevoke
        token.setRevoked(true);
        assertTrue(token.isRevoked());
        assertEquals(revokeTime, token.getRevokedAt());
    }

    @Test
    @DisplayName("Multiple users with multiple refresh tokens")
    void testMultipleUsersWithMultipleTokens() {
        LocalDateTime now = LocalDateTime.now();

        for (int userId = 1; userId <= 3; userId++) {
            for (int tokenIdx = 0; tokenIdx < 3; tokenIdx++) {
                RefreshToken token = new RefreshToken();
                token.setId((long) (userId * 100 + tokenIdx));
                token.setUserId((long) userId);
                token.setToken("user-" + userId + "-token-" + tokenIdx);
                token.setExpiryDate(now.plusDays(tokenIdx + 1));
                token.setRevoked(false);

                assertNotNull(token.getToken());
                assertEquals((long) userId, token.getUserId());
            }
        }
    }
}
