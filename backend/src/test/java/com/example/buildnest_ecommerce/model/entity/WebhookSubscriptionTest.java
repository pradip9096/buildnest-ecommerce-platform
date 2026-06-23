package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSubscriptionTest {

    @Test
    void defaultsAndAccessorsWork() {
        WebhookSubscription sub = new WebhookSubscription();

        assertTrue(sub.getActive());
        assertEquals(0, sub.getFailureCount());
        assertNotNull(sub.getCreatedAt());

        sub.setId(1L);
        sub.setEventType("ORDER_CREATED");
        sub.setTargetUrl("https://example.com/webhook");
        sub.setSecret("secret");
        sub.setLastDeliveryStatus("SUCCESS");
        sub.setUpdatedAt(LocalDateTime.now());

        assertEquals("ORDER_CREATED", sub.getEventType());
        assertEquals("https://example.com/webhook", sub.getTargetUrl());
        assertEquals("secret", sub.getSecret());
        assertEquals("SUCCESS", sub.getLastDeliveryStatus());
    }

    @Test
    void equalsAndHashCodeCoverNullsAndDifferences() {
        LocalDateTime now = LocalDateTime.now();

        WebhookSubscription base = new WebhookSubscription(
                1L,
                "ORDER_CREATED",
                "https://example.com/webhook",
                "secret",
                true,
                0,
                "SUCCESS",
                now,
                now);

        WebhookSubscription same = new WebhookSubscription(
                1L,
                "ORDER_CREATED",
                "https://example.com/webhook",
                "secret",
                true,
                0,
                "SUCCESS",
                now,
                now);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-sub");

        WebhookSubscription diffUrl = new WebhookSubscription(
                1L,
                "ORDER_CREATED",
                "https://example.com/other",
                "secret",
                true,
                0,
                "SUCCESS",
                now,
                now);

        WebhookSubscription diffActive = new WebhookSubscription(
                1L,
                "ORDER_CREATED",
                "https://example.com/webhook",
                "secret",
                false,
                0,
                "SUCCESS",
                now,
                now);

        assertNotEquals(base, diffUrl);
        assertNotEquals(base, diffActive);
    }
}
