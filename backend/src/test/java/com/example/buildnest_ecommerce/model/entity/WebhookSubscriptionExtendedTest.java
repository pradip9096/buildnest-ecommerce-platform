package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebhookSubscription Comprehensive Branch Coverage")
class WebhookSubscriptionExtendedTest {

    @Test
    @DisplayName("Test active field default value true")
    void testActiveDefaultTrue() {
        WebhookSubscription ws = new WebhookSubscription();
        assertTrue(ws.getActive());
    }

    @Test
    @DisplayName("Test failureCount field default value 0")
    void testFailureCountDefaultZero() {
        WebhookSubscription ws = new WebhookSubscription();
        assertEquals(0, ws.getFailureCount());
    }

    @Test
    @DisplayName("Test createdAt field default to now")
    void testCreatedAtDefaultNow() {
        LocalDateTime before = LocalDateTime.now();
        WebhookSubscription ws = new WebhookSubscription();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(ws.getCreatedAt());
        assertTrue(!ws.getCreatedAt().isBefore(before) || ws.getCreatedAt().isEqual(before));
        assertTrue(!ws.getCreatedAt().isAfter(after) || ws.getCreatedAt().isEqual(after));
    }

    @ParameterizedTest
    @CsvSource({
            "order.created, https://webhook.example.com/order",
            "order.updated, https://api.example.com/hooks/order",
            "payment.completed, https://payment.service/webhook",
            "user.registered, https://user.service/events"
    })
    @DisplayName("Test eventType and targetUrl combinations")
    void testEventTypeTargetUrlCombinations(String eventType, String targetUrl) {
        WebhookSubscription ws = new WebhookSubscription();
        ws.setEventType(eventType);
        ws.setTargetUrl(targetUrl);

        assertEquals(eventType, ws.getEventType());
        assertEquals(targetUrl, ws.getTargetUrl());
    }

    @Test
    @DisplayName("Test secret field null and non-null")
    void testSecretField() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setSecret(null);
        assertNull(ws1.getSecret());

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setSecret("secret-key-123");
        assertEquals("secret-key-123", ws2.getSecret());
    }

    @Test
    @DisplayName("Test active field variations")
    void testActiveFieldVariations() {
        Boolean[] activeValues = { true, false, null };

        for (Boolean active : activeValues) {
            WebhookSubscription ws = new WebhookSubscription();
            ws.setActive(active);

            if (active != null) {
                assertEquals(active, ws.getActive());
            }
        }
    }

    @Test
    @DisplayName("Test failureCount field variations")
    void testFailureCountVariations() {
        Integer[] counts = { 0, 1, 2, 3, 10, 100, null };

        for (Integer count : counts) {
            WebhookSubscription ws = new WebhookSubscription();
            ws.setFailureCount(count);

            if (count != null) {
                assertEquals(count, ws.getFailureCount());
            }
        }
    }

    @Test
    @DisplayName("Test lastDeliveryStatus field variations")
    void testLastDeliveryStatusVariations() {
        String[] statuses = { null, "SUCCESS", "FAILED", "RETRY", "TIMEOUT" };

        for (String status : statuses) {
            WebhookSubscription ws = new WebhookSubscription();
            ws.setLastDeliveryStatus(status);
            assertEquals(status, ws.getLastDeliveryStatus());
        }
    }

    @Test
    @DisplayName("Test updatedAt field variations")
    void testUpdatedAtVariations() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setUpdatedAt(null);
        assertNull(ws1.getUpdatedAt());

        LocalDateTime now = LocalDateTime.now();
        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setUpdatedAt(now);
        assertEquals(now, ws2.getUpdatedAt());
    }

    @Test
    @DisplayName("Test allArgs constructor")
    void testAllArgsConstructor() {
        LocalDateTime created = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2023, 1, 15, 15, 30);

        WebhookSubscription ws = new WebhookSubscription(
                1L, "order.created", "https://webhook.example.com", "secret",
                true, 0, "SUCCESS", created, updated);

        assertEquals(1L, ws.getId());
        assertEquals("order.created", ws.getEventType());
        assertEquals("https://webhook.example.com", ws.getTargetUrl());
        assertEquals("secret", ws.getSecret());
        assertTrue(ws.getActive());
        assertEquals(0, ws.getFailureCount());
        assertEquals("SUCCESS", ws.getLastDeliveryStatus());
        assertEquals(created, ws.getCreatedAt());
        assertEquals(updated, ws.getUpdatedAt());
    }

    @Test
    @DisplayName("Test equals with same values")
    void testEqualsWithSameValues() {
        LocalDateTime now = LocalDateTime.now();

        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setId(1L);
        ws1.setEventType("order.created");
        ws1.setTargetUrl("https://webhook.com");
        ws1.setCreatedAt(now);

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setId(1L);
        ws2.setEventType("order.created");
        ws2.setTargetUrl("https://webhook.com");
        ws2.setCreatedAt(now);

        assertEquals(ws1, ws2);
    }

    @Test
    @DisplayName("Test equals with different values")
    void testNotEqualsWithDifferentValues() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setId(1L);

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setId(2L);

        assertNotEquals(ws1, ws2);
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void testHashCodeConsistency() {
        WebhookSubscription ws = new WebhookSubscription();
        ws.setId(1L);
        ws.setEventType("order.created");

        int hash1 = ws.hashCode();
        int hash2 = ws.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        WebhookSubscription ws = new WebhookSubscription();
        ws.setId(1L);
        ws.setEventType("order.created");
        ws.setTargetUrl("https://webhook.com");

        String str = ws.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    @Test
    @DisplayName("Test setter chaining simulation - multiple sets")
    void testMultipleSets() {
        WebhookSubscription ws = new WebhookSubscription();

        ws.setId(1L);
        ws.setEventType("order.created");
        ws.setTargetUrl("https://webhook.com");
        ws.setSecret("secret123");
        ws.setActive(true);
        ws.setFailureCount(0);
        ws.setLastDeliveryStatus("SUCCESS");

        assertEquals(1L, ws.getId());
        assertEquals("order.created", ws.getEventType());
        assertEquals("https://webhook.com", ws.getTargetUrl());
        assertEquals("secret123", ws.getSecret());
        assertTrue(ws.getActive());
        assertEquals(0, ws.getFailureCount());
        assertEquals("SUCCESS", ws.getLastDeliveryStatus());
    }

    @Test
    @DisplayName("Test loop iteration with 5 webhook subscriptions")
    void testLoopWith5Subscriptions() {
        List<WebhookSubscription> subscriptions = new ArrayList<>();
        String[] eventTypes = { "order.created", "order.updated", "payment.completed", "user.registered",
                "product.updated" };

        for (int i = 0; i < 5; i++) {
            WebhookSubscription ws = new WebhookSubscription();
            ws.setId((long) (i + 1));
            ws.setEventType(eventTypes[i]);
            ws.setTargetUrl("https://webhook.example.com/" + i);
            subscriptions.add(ws);
        }

        assertEquals(5, subscriptions.size());
        for (int i = 0; i < 5; i++) {
            assertEquals((long) (i + 1), subscriptions.get(i).getId());
            assertEquals(eventTypes[i], subscriptions.get(i).getEventType());
        }
    }

    @Test
    @DisplayName("Test default vs explicit active values")
    void testDefaultVsExplicitActive() {
        WebhookSubscription ws1 = new WebhookSubscription();
        assertTrue(ws1.getActive()); // default true

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setActive(false);
        assertFalse(ws2.getActive()); // explicitly false

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setActive(true);
        assertTrue(ws3.getActive()); // explicitly true
    }

    @Test
    @DisplayName("Test default vs explicit failureCount")
    void testDefaultVsExplicitFailureCount() {
        WebhookSubscription ws1 = new WebhookSubscription();
        assertEquals(0, ws1.getFailureCount()); // default 0

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setFailureCount(5);
        assertEquals(5, ws2.getFailureCount()); // incremented

        WebhookSubscription ws3 = new WebhookSubscription();
        ws3.setFailureCount(0);
        assertEquals(0, ws3.getFailureCount()); // reset to 0
    }

    @Test
    @DisplayName("Test noArgs constructor uses defaults")
    void testNoArgsConstructorDefaults() {
        WebhookSubscription ws = new WebhookSubscription();

        assertTrue(ws.getActive()); // default true
        assertEquals(0, ws.getFailureCount()); // default 0
        assertNotNull(ws.getCreatedAt()); // default now
    }

    @Test
    @DisplayName("Test event type case sensitivity")
    void testEventTypeCaseSensitivity() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setEventType("ORDER.CREATED");

        WebhookSubscription ws2 = new WebhookSubscription();
        ws2.setEventType("order.created");

        assertNotEquals(ws1.getEventType(), ws2.getEventType());
    }

    @Test
    @DisplayName("Test targetUrl length variations")
    void testTargetUrlLength() {
        WebhookSubscription ws1 = new WebhookSubscription();
        ws1.setTargetUrl("https://webhook.com");

        WebhookSubscription ws2 = new WebhookSubscription();
        String longUrl = "https://webhook.example.com/path/to/very/long/url/that/exceeds/normal/length?"
                + "param1=value1&param2=value2&param3=value3&param4=value4";
        ws2.setTargetUrl(longUrl);

        assertTrue(ws1.getTargetUrl().length() < ws2.getTargetUrl().length());
    }
}
