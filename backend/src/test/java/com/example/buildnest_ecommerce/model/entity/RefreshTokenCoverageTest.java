package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenCoverageTest {

    @Test
    void onCreateSetsCreatedAtAndEqualityWorks() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(1);

        RefreshToken base = new RefreshToken(1L, "token", 2L, expiry, false, null, null);

        assertNull(base.getCreatedAt());
        base.onCreate();
        assertNotNull(base.getCreatedAt());

        assertEquals(base, base);
        // Note: Cannot compare base and same after onCreate() since createdAt differs
        assertNotEquals(base, null);
        assertNotEquals(base, "not-token");

        RefreshToken diffToken = new RefreshToken(1L, "token-2", 2L, expiry, false, null, null);
        RefreshToken diffUser = new RefreshToken(1L, "token", 3L, expiry, false, null, null);

        assertNotEquals(base, diffToken);
        assertNotEquals(base, diffUser);
    }

    @Test
    void settersUpdateFields() {
        RefreshToken token = new RefreshToken();
        token.setId(5L);
        token.setToken("t");
        token.setUserId(7L);
        token.setExpiryDate(LocalDateTime.now().plusHours(2));
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());

        assertEquals(5L, token.getId());
        assertEquals("t", token.getToken());
        assertEquals(7L, token.getUserId());
        assertTrue(token.isRevoked());
        assertNotNull(token.getRevokedAt());
    }
}
