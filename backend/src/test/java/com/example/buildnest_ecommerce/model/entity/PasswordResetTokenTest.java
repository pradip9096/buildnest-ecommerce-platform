package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void onCreateSetsCreatedAt() {
        PasswordResetToken token = new PasswordResetToken();
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
        PasswordResetToken token = new PasswordResetToken(1L, "t", 2L, now.plusDays(1), false, now, null);
        token.setUsed(true);
        token.setUsedAt(now.plusHours(1));

        assertTrue(token.isUsed());
        assertEquals(now.plusHours(1), token.getUsedAt());
    }
}
