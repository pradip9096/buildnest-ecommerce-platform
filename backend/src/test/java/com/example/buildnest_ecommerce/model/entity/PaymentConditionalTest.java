package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment - Conditional Branch Coverage Tests")
class PaymentConditionalTest {

    @Test
    @DisplayName("ID field - null vs non-null")
    void testIdField() {
        Payment payment = new Payment();
        payment.setId(null);
        assertNull(payment.getId());

        payment.setId(1L);
        assertEquals(1L, payment.getId());
    }

    @Test
    @DisplayName("OrderId field - null and various values")
    void testOrderIdField() {
        Payment payment = new Payment();

        payment.setOrderId(null);
        assertNull(payment.getOrderId());

        payment.setOrderId(0L);
        assertEquals(0L, payment.getOrderId());

        payment.setOrderId(100L);
        assertEquals(100L, payment.getOrderId());
    }

    @Test
    @DisplayName("Amount field - null and numeric variations")
    void testAmountField() {
        Payment payment = new Payment();

        payment.setAmount(null);
        assertNull(payment.getAmount());

        payment.setAmount(0.0);
        assertEquals(0.0, payment.getAmount());

        payment.setAmount(100.50);
        assertEquals(100.50, payment.getAmount());

        payment.setAmount(-50.0);
        assertEquals(-50.0, payment.getAmount());
    }

    @Test
    @DisplayName("RazorpayOrderId field - null vs populated")
    void testRazorpayOrderIdField() {
        Payment payment = new Payment();

        payment.setRazorpayOrderId(null);
        assertNull(payment.getRazorpayOrderId());

        payment.setRazorpayOrderId("order_1234567890");
        assertEquals("order_1234567890", payment.getRazorpayOrderId());
    }

    @Test
    @DisplayName("RazorpayPaymentId field - null vs populated")
    void testRazorpayPaymentIdField() {
        Payment payment = new Payment();

        payment.setRazorpayPaymentId(null);
        assertNull(payment.getRazorpayPaymentId());

        payment.setRazorpayPaymentId("pay_9876543210");
        assertEquals("pay_9876543210", payment.getRazorpayPaymentId());
    }

    @Test
    @DisplayName("Status field - PENDING/SUCCESS/FAILED/REFUNDED variations")
    void testStatusField() {
        Payment payment = new Payment();

        // Null branch
        payment.setStatus(null);
        assertNull(payment.getStatus());

        // PENDING branch
        payment.setStatus("PENDING");
        assertEquals("PENDING", payment.getStatus());

        // SUCCESS branch
        payment.setStatus("SUCCESS");
        assertEquals("SUCCESS", payment.getStatus());

        // FAILED branch
        payment.setStatus("FAILED");
        assertEquals("FAILED", payment.getStatus());

        // REFUNDED branch
        payment.setStatus("REFUNDED");
        assertEquals("REFUNDED", payment.getStatus());
    }

    @Test
    @DisplayName("CreatedAt field - null vs LocalDateTime")
    void testCreatedAtField() {
        Payment payment = new Payment();

        payment.setCreatedAt(null);
        assertNull(payment.getCreatedAt());

        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        assertEquals(now, payment.getCreatedAt());
    }

    @Test
    @DisplayName("UpdatedAt field - null vs LocalDateTime")
    void testUpdatedAtField() {
        Payment payment = new Payment();

        payment.setUpdatedAt(null);
        assertNull(payment.getUpdatedAt());

        LocalDateTime updated = LocalDateTime.now();
        payment.setUpdatedAt(updated);
        assertEquals(updated, payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Complete payment object with all fields")
    void testCompletePayment() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updated = now.plusMinutes(5);

        Payment payment = new Payment(
                1L,
                100L,
                500.75,
                "order_abc123",
                "pay_xyz789",
                "SUCCESS",
                now,
                updated);

        assertEquals(1L, payment.getId());
        assertEquals(100L, payment.getOrderId());
        assertEquals(500.75, payment.getAmount());
        assertEquals("order_abc123", payment.getRazorpayOrderId());
        assertEquals("pay_xyz789", payment.getRazorpayPaymentId());
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(now, payment.getCreatedAt());
        assertEquals(updated, payment.getUpdatedAt());
    }

    @Test
    @DisplayName("NoArgsConstructor - default state")
    void testNoArgsConstructor() {
        Payment payment = new Payment();
        assertNull(payment.getId());
        assertNull(payment.getOrderId());
        assertNull(payment.getAmount());
    }

    @Test
    @DisplayName("AllArgsConstructor - 8 parameter constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(1L, 50L, 250.0, "order", "pay", "PENDING", now, null);

        assertEquals(1L, payment.getId());
        assertEquals(50L, payment.getOrderId());
    }

    @Test
    @DisplayName("Lombok equals() with matching payments")
    void testEquals() {
        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setOrderId(100L);

        Payment payment2 = new Payment();
        payment2.setId(1L);
        payment2.setOrderId(100L);

        assertEquals(payment1, payment2);
    }

    @Test
    @DisplayName("Lombok hashCode() consistency")
    void testHashCode() {
        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setOrderId(100L);

        Payment payment2 = new Payment();
        payment2.setId(1L);
        payment2.setOrderId(100L);

        assertEquals(payment1.hashCode(), payment2.hashCode());
    }

    @Test
    @DisplayName("Lombok toString()")
    void testToString() {
        Payment payment = new Payment();
        payment.setId(1L);

        String str = payment.toString();
        assertNotNull(str);
        assertTrue(str.contains("Payment"));
    }

    @Test
    @DisplayName("Multiple payments with different statuses")
    void testMultiplePaymentsWithDifferentStatuses() {
        String[] statuses = { "PENDING", "SUCCESS", "FAILED", "REFUNDED" };
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            Payment payment = new Payment();
            payment.setId((long) i);
            payment.setOrderId((long) (i + 100));
            payment.setAmount(100.0 * (i + 1));
            payment.setStatus(statuses[i % 4]); // Cycles through all statuses
            payment.setCreatedAt(now.minusMinutes(i));

            assertNotNull(payment.getStatus());
            assertTrue(statuses[i % 4].equals(payment.getStatus())); // Branch coverage for status
        }
    }

    @Test
    @DisplayName("Payment lifecycle - creation to completion")
    void testPaymentLifecycle() {
        Payment payment = new Payment();
        LocalDateTime now = LocalDateTime.now();

        // Initial state
        payment.setId(1L);
        payment.setOrderId(100L);
        payment.setAmount(500.0);
        payment.setStatus("PENDING");
        payment.setCreatedAt(now);
        assertEquals("PENDING", payment.getStatus());

        // Processing state
        payment.setRazorpayOrderId("order_123");
        payment.setRazorpayPaymentId("pay_456");

        // Completed state
        payment.setStatus("SUCCESS");
        LocalDateTime updated = now.plusMinutes(1);
        payment.setUpdatedAt(updated);
        assertEquals("SUCCESS", payment.getStatus());
        assertNotNull(payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Null handling across all fields")
    void testNullHandlingAcrossFields() {
        Payment payment = new Payment();

        assertNull(payment.getId());
        assertNull(payment.getOrderId());
        assertNull(payment.getAmount());
        assertNull(payment.getRazorpayOrderId());
        assertNull(payment.getRazorpayPaymentId());
        assertNull(payment.getStatus());
        assertNull(payment.getCreatedAt());
        assertNull(payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Razorpay ID field maximum length")
    void testRazorpayIdFieldMaxLength() {
        Payment payment = new Payment();

        // Max 255 chars as per column definition
        StringBuilder longId = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            longId.append("a");
        }

        payment.setRazorpayOrderId(longId.toString());
        assertEquals(255, payment.getRazorpayOrderId().length());
    }

    @Test
    @DisplayName("Amount variations - zero, positive, negative")
    void testAmountVariations() {
        Payment payment = new Payment();

        // Zero amount
        payment.setAmount(0.0);
        assertEquals(0.0, payment.getAmount());

        // Positive amounts
        payment.setAmount(0.01);
        assertEquals(0.01, payment.getAmount());

        payment.setAmount(999999.99);
        assertEquals(999999.99, payment.getAmount());

        // Negative amounts (refunds)
        payment.setAmount(-100.0);
        assertTrue(payment.getAmount() < 0);
    }

    @Test
    @DisplayName("Payment order relationships")
    void testPaymentOrderRelationships() {
        LocalDateTime now = LocalDateTime.now();

        for (int orderId = 1; orderId <= 5; orderId++) {
            for (int paymentIdx = 0; paymentIdx < 3; paymentIdx++) {
                Payment payment = new Payment();
                payment.setId((long) (orderId * 100 + paymentIdx));
                payment.setOrderId((long) orderId);
                payment.setAmount(100.0 + paymentIdx * 10);
                payment.setStatus("SUCCESS");
                payment.setCreatedAt(now.minusMinutes(paymentIdx));

                assertEquals((long) orderId, payment.getOrderId());
            }
        }
    }
}
