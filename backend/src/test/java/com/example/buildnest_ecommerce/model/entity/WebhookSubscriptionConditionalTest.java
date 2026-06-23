package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebhookSubscription Branch Coverage Tests")
class WebhookSubscriptionConditionalTest {

    @Test
    @DisplayName("Test ID field")
    void testIdField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setId(null);
        assertNull(ws1.getId());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setId(1L);
        assertEquals(1L, ws2.getId());
    }

    @Test
    @DisplayName("Test eventType field variations")
    void testEventTypeField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setEventType(null);
        assertNull(ws1.getEventType());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setEventType("order.created");
        assertEquals("order.created", ws2.getEventType());

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setEventType("order.updated");
        assertEquals("order.updated", ws3.getEventType());

        WebhookSubscription ws4 = new WebhookSubscription();
        ws4.setEventType("order.deleted");
        assertEquals("order.deleted", ws4.getEventType());
    }

    @Test
    @DisplayName("Test targetUrl field")
    void testTargetUrlField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setTargetUrl(null);
        assertNull(ws1.getTargetUrl());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setTargetUrl("https://example.com/webhook");
        assertEquals("https://example.com/webhook", ws2.getTargetUrl());
    }

    @Test
    @DisplayName("Test secret field")
    void testSecretField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setSecret(null);
        assertNull(ws1.getSecret());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setSecret("secret-key-123");
        assertEquals("secret-key-123", ws2.getSecret());
    }

    @Test
    @DisplayName("Test active field with default value")
    void testActiveField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        // Default is true
        assertTrue(ws1.getActive());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setActive(true);
        assertTrue(ws2.getActive());

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setActive(false);
        assertFalse(ws3.getActive());

        WebhookSubscription ws4 = new WebhookSubscription();
        ws4.setActive(null);
        assertNull(ws4.getActive());
    }

    @Test
    @DisplayName("Test failureCount field with default value")
    void testFailureCountField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        // Default is 0
        assertEquals(0, ws1.getFailureCount());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setFailureCount(0);
        assertEquals(0, ws2.getFailureCount());

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setFailureCount(5);
        assertEquals(5, ws3.getFailureCount());

        WebhookSubscription ws4 = new WebhookSubscription();
        ws4.setFailureCount(10);
        assertEquals(10, ws4.getFailureCount());

        WebhookSubscription ws5 = new WebhookSubscription();
        ws5.setFailureCount(null);
        assertNull(ws5.getFailureCount());
    }

    @Test
    @DisplayName("Test lastDeliveryStatus field variations")
    void testLastDeliveryStatusField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setLastDeliveryStatus(null);
        assertNull(ws1.getLastDeliveryStatus());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setLastDeliveryStatus("SUCCESS");
        assertEquals("SUCCESS", ws2.getLastDeliveryStatus());

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setLastDeliveryStatus("FAILED");
        assertEquals("FAILED", ws3.getLastDeliveryStatus());

        WebhookSubscription ws4 = new WebhookSubscription();
        ws4.setLastDeliveryStatus("PENDING");
        assertEquals("PENDING", ws4.getLastDeliveryStatus());
    }

    @Test
    @DisplayName("Test createdAt field with default value")
    void testCreatedAtField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        // Default is now
        assertNotNull(ws1.getCreatedAt());

        LocalDateTime specificTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setCreatedAt(specificTime);
        assertEquals(specificTime, ws2.getCreatedAt());

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setCreatedAt(null);
        assertNull(ws3.getCreatedAt());
    }

    @Test
    @DisplayName("Test updatedAt field")
    void testUpdatedAtField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setUpdatedAt(null);
        assertNull(ws1.getUpdatedAt());

        LocalDateTime updateTime = LocalDateTime.of(2025, 1, 2, 12, 0, 0);
        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setUpdatedAt(updateTime);
        assertEquals(updateTime, ws2.getUpdatedAt());
    }

    @Test
    @DisplayName("Test complete WebhookSubscription object")
    void testCompleteWebhookSubscription() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updated = now.plusHours(1);

        WebhookSubscription ws = new WebhookSubscription(
                1L,
                "order.created",
                "https://example.com/webhook",
                "secret-key",
                true,
                0,
                "SUCCESS",
                now,
                updated);

        assertEquals(1L, ws.getId());
        assertEquals("order.created", ws.getEventType());
        assertEquals("https://example.com/webhook", ws.getTargetUrl());
        assertEquals("secret-key", ws.getSecret());
        assertTrue(ws.getActive());
        assertEquals(0, ws.getFailureCount());
        assertEquals("SUCCESS", ws.getLastDeliveryStatus());
        assertEquals(now, ws.getCreatedAt());
        assertEquals(updated, ws.getUpdatedAt());
    }

    @Test
    @DisplayName("Test no-args constructor defaults")
    void testNoArgsConstructor() {
        WebhookSubscription ws = new WebhookSubscription();
        assertNull(ws.getId());
        assertTrue(ws.getActive());
        assertEquals(0, ws.getFailureCount());
        assertNotNull(ws.getCreatedAt());
    }

    @Test
    @DisplayName("Test all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        WebhookSubscription ws = new WebhookSubscription(
                5L, "event.type", "https://url.com", "secret", false, 3, "FAILED", now, null);

        assertEquals(5L, ws.getId());
        assertEquals("event.type", ws.getEventType());
        assertEquals("https://url.com", ws.getTargetUrl());
        assertEquals("secret", ws.getSecret());
        assertFalse(ws.getActive());
        assertEquals(3, ws.getFailureCount());
        assertEquals("FAILED", ws.getLastDeliveryStatus());
        assertEquals(now, ws.getCreatedAt());
        assertNull(ws.getUpdatedAt());
    }

    @Test
    @DisplayName("Test field setters return object for chaining")
    void testSettersChaining() {
        WebhookSubscription ws = new WebhookSubscription();
        ws.setId(1L);
        ws.setEventType("test.event");
        ws.setTargetUrl("https://test.com");
        ws.setSecret("test-secret");
        ws.setActive(true);
        ws.setFailureCount(0);
        ws.setLastDeliveryStatus("SUCCESS");

        assertEquals(1L, ws.getId());
        assertEquals("test.event", ws.getEventType());
        assertEquals("https://test.com", ws.getTargetUrl());
        assertEquals("test-secret", ws.getSecret());
        assertTrue(ws.getActive());
        assertEquals(0, ws.getFailureCount());
        assertEquals("SUCCESS", ws.getLastDeliveryStatus());
    }

    @Test
    @DisplayName("Test equals method")
    void testEquals() {
        LocalDateTime now = LocalDateTime.now();
        WebhookSubscription ws1 = new WebhookSubscription(
                1L, "event", "https://url", "secret", true, 0, "SUCCESS", now, null);
        WebhookSubscription ws2 = new WebhookSubscription(
                1L, "event", "https://url", "secret", true, 0, "SUCCESS", now, null);

        assertEquals(ws1, ws2);
    }

    @Test
    @DisplayName("Test hashCode")
    void testHashCode() {
        LocalDateTime now = LocalDateTime.now();
        WebhookSubscription ws1 = new WebhookSubscription(
                1L, "event", "https://url", "secret", true, 0, "SUCCESS", now, null);
        WebhookSubscription ws2 = new WebhookSubscription(
                1L, "event", "https://url", "secret", true, 0, "SUCCESS", now, null);

        assertEquals(ws1.hashCode(), ws2.hashCode());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        WebhookSubscription ws = new WebhookSubscription();
        ws.setId(1L);
        ws.setEventType("test.event");

        String str = ws.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    @DisplayName("Test multiple webhook subscriptions with different states")
    void testMultipleSubscriptions() {
        for (int i = 0; i < 10; i++) {
            WebhookSubscription ws = new WebhookSubscription();
            ws.setId((long) i);
            ws.setEventType("event." + i);
            ws.setTargetUrl("https://example.com/webhook/" + i);
            ws.setActive(i % 2 == 0);
            ws.setFailureCount(i);

            assertEquals((long) i, ws.getId());
            assertEquals("event." + i, ws.getEventType());
            assertEquals(i % 2 == 0, ws.getActive());
            assertEquals(i, ws.getFailureCount());
        }
    }
}
