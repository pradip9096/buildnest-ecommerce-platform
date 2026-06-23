package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void onCreateSetsCreatedAt() {
        RefreshToken token = new RefreshToken();
        token.setToken("token");
        token.setUserId(1L);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));

        assertNull(token.getCreatedAt());
        token.onCreate();
        assertNotNull(token.getCreatedAt());
    }

    @Test
    void settersAndGettersWork() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken token = new RefreshToken(1L, "t", 2L, now.plusDays(1), false, now, null);
        token.setRevoked(true);
        token.setRevokedAt(now.plusHours(1));

        assertTrue(token.isRevoked());
        assertEquals(now.plusHours(1), token.getRevokedAt());
    }
}
