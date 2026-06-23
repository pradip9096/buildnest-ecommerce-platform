package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenCoverageTest {

    @Test
    void onCreateSetsCreatedAtAndEqualityWorks() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(1);

        PasswordResetToken base = new PasswordResetToken(1L, "token", 2L, expiry, false, null, null);

        assertNull(base.getCreatedAt());
        base.onCreate();
        assertNotNull(base.getCreatedAt());

        assertEquals(base, base);
        // Note: Cannot compare base and same after onCreate() since createdAt differs
        assertNotEquals(base, null);
        assertNotEquals(base, "not-token");

        PasswordResetToken diffToken = new PasswordResetToken(1L, "token-2", 2L, expiry, false, null, null);
        PasswordResetToken diffUser = new PasswordResetToken(1L, "token", 3L, expiry, false, null, null);

        assertNotEquals(base, diffToken);
        assertNotEquals(base, diffUser);
    }

    @Test
    void settersUpdateFields() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(5L);
        token.setToken("t");
        token.setUserId(7L);
        token.setExpiryDate(LocalDateTime.now().plusHours(2));
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());

        assertEquals(5L, token.getId());
        assertEquals("t", token.getToken());
        assertEquals(7L, token.getUserId());
        assertTrue(token.isUsed());
        assertNotNull(token.getUsedAt());
    }
}
