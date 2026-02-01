package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordResetToken - Conditional Branch Coverage Tests")
class PasswordResetTokenConditionalTest {

    @Test
    @DisplayName("ID field - null vs non-null")
    void testIdField() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(null);
        assertNull(token.getId());

        token.setId(1L);
        assertEquals(1L, token.getId());
    }

    @Test
    @DisplayName("Token field - null vs populated")
    void testTokenField() {
        PasswordResetToken token = new PasswordResetToken();

        token.setToken(null);
        assertNull(token.getToken());

        token.setToken("abc123xyz");
        assertEquals("abc123xyz", token.getToken());
    }

    @Test
    @DisplayName("UserId field - null and various values")
    void testUserIdField() {
        PasswordResetToken token = new PasswordResetToken();

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
        PasswordResetToken token = new PasswordResetToken();

        token.setExpiryDate(null);
        assertNull(token.getExpiryDate());

        LocalDateTime future = LocalDateTime.now().plusHours(24);
        token.setExpiryDate(future);
        assertEquals(future, token.getExpiryDate());
    }

    @Test
    @DisplayName("Used field - boolean true/false/null variations")
    void testUsedField() {
        PasswordResetToken token = new PasswordResetToken();

        // Default false
        assertFalse(token.isUsed());

        // Set to true
        token.setUsed(true);
        assertTrue(token.isUsed());

        // Set to false
        token.setUsed(false);
        assertFalse(token.isUsed());
    }

    @Test
    @DisplayName("CreatedAt field - null vs LocalDateTime")
    void testCreatedAtField() {
        PasswordResetToken token = new PasswordResetToken();

        token.setCreatedAt(null);
        assertNull(token.getCreatedAt());

        LocalDateTime now = LocalDateTime.now();
        token.setCreatedAt(now);
        assertEquals(now, token.getCreatedAt());
    }

    @Test
    @DisplayName("UsedAt field - null vs LocalDateTime")
    void testUsedAtField() {
        PasswordResetToken token = new PasswordResetToken();

        token.setUsedAt(null);
        assertNull(token.getUsedAt());

        LocalDateTime used = LocalDateTime.now();
        token.setUsedAt(used);
        assertEquals(used, token.getUsedAt());
    }

    @Test
    @DisplayName("Complete token object with all fields")
    void testCompletePasswordResetToken() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(24);
        LocalDateTime used = now.plusMinutes(5);

        PasswordResetToken token = new PasswordResetToken(
                1L,
                "reset-token-abc123",
                100L,
                future,
                true,
                now,
                used);

        assertEquals(1L, token.getId());
        assertEquals("reset-token-abc123", token.getToken());
        assertEquals(100L, token.getUserId());
        assertEquals(future, token.getExpiryDate());
        assertTrue(token.isUsed());
        assertEquals(now, token.getCreatedAt());
        assertEquals(used, token.getUsedAt());
    }

    @Test
    @DisplayName("NoArgsConstructor - default state")
    void testNoArgsConstructor() {
        PasswordResetToken token = new PasswordResetToken();
        assertNull(token.getId());
        assertNull(token.getToken());
        assertFalse(token.isUsed());
    }

    @Test
    @DisplayName("AllArgsConstructor - 7 parameter constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken token = new PasswordResetToken(1L, "token", 50L, now, false, now, null);

        assertEquals(1L, token.getId());
        assertEquals("token", token.getToken());
    }

    @Test
    @DisplayName("Lombok equals() with matching tokens")
    void testEquals() {
        PasswordResetToken token1 = new PasswordResetToken();
        token1.setId(1L);
        token1.setToken("abc123");

        PasswordResetToken token2 = new PasswordResetToken();
        token2.setId(1L);
        token2.setToken("abc123");

        assertEquals(token1, token2);
    }

    @Test
    @DisplayName("Lombok hashCode() consistency")
    void testHashCode() {
        PasswordResetToken token1 = new PasswordResetToken();
        token1.setId(1L);
        token1.setToken("abc123");

        PasswordResetToken token2 = new PasswordResetToken();
        token2.setId(1L);
        token2.setToken("abc123");

        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    @DisplayName("Lombok toString()")
    void testToString() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);

        String str = token.toString();
        assertNotNull(str);
        assertTrue(str.contains("PasswordResetToken"));
    }

    @Test
    @DisplayName("Multiple tokens with various used states")
    void testMultipleTokensWithVariousStates() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            PasswordResetToken token = new PasswordResetToken();
            token.setId((long) i);
            token.setUserId((long) (i + 100));
            token.setToken("token-" + i);
            token.setExpiryDate(now.plusHours(i));
            token.setUsed(i % 2 == 0); // Alternates used/unused

            assertNotNull(token.getToken());
            assertEquals((i % 2 == 0), token.isUsed()); // Branch coverage for boolean
        }
    }

    @Test
    @DisplayName("Token lifecycle - creation, usage, expiry")
    void testTokenLifecycle() {
        PasswordResetToken token = new PasswordResetToken();

        // Created state
        token.setId(1L);
        token.setToken("reset-abc");
        token.setUserId(50L);
        token.setCreatedAt(LocalDateTime.now());
        assertFalse(token.isUsed());
        assertNull(token.getUsedAt());

        // Used state
        token.setUsed(true);
        LocalDateTime usedTime = LocalDateTime.now();
        token.setUsedAt(usedTime);
        assertTrue(token.isUsed());
        assertEquals(usedTime, token.getUsedAt());
    }

    @Test
    @DisplayName("Token expiry validation")
    void testTokenExpiryValidation() {
        PasswordResetToken token = new PasswordResetToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(1);
        LocalDateTime future = now.plusHours(1);

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
        PasswordResetToken token = new PasswordResetToken();

        // All fields null/default
        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUserId());
        assertNull(token.getExpiryDate());
        assertFalse(token.isUsed());
        assertNull(token.getCreatedAt());
        assertNull(token.getUsedAt());
    }

    @Test
    @DisplayName("Token field maximum length")
    void testTokenFieldMaxLength() {
        PasswordResetToken token = new PasswordResetToken();

        // Max 500 chars as per column definition
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longToken.append("a");
        }

        token.setToken(longToken.toString());
        assertEquals(500, token.getToken().length());
    }
}
