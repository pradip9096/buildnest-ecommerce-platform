package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEntityTest {

    private Payment build(Long id, Long orderId, Double amount, String rzpOrder, String rzpPayment, String status, LocalDateTime created, LocalDateTime updated) {
        Payment p = new Payment();
        p.setId(id); p.setOrderId(orderId); p.setAmount(amount);
        p.setRazorpayOrderId(rzpOrder); p.setRazorpayPaymentId(rzpPayment);
        p.setStatus(status); p.setCreatedAt(created); p.setUpdatedAt(updated);
        p.setRefundedAmount(0.0);
        return p;
    }

    @Test
    void accessorsAndEqualityWork() {
        LocalDateTime now = LocalDateTime.now();
        Payment base = build(1L, 2L, 25.5, "rp_order", "rp_payment", "SUCCESS", now, now);
        Payment same = build(1L, 2L, 25.5, "rp_order", "rp_payment", "SUCCESS", now, now);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-payment");

        Payment diffStatus = build(1L, 2L, 25.5, "rp_order", "rp_payment", "FAILED", now, now);
        Payment diffAmount = build(1L, 2L, 20.0, "rp_order", "rp_payment", "SUCCESS", now, now);

        assertNotEquals(base, diffStatus);
        assertNotEquals(base, diffAmount);
    }

    @Test
    void settersUpdateFields() {
        Payment payment = new Payment();
        payment.setId(5L);
        payment.setOrderId(10L);
        payment.setAmount(100.0);
        payment.setRazorpayOrderId("order");
        payment.setRazorpayPaymentId("payment");
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());

        assertEquals(5L, payment.getId());
        assertEquals(10L, payment.getOrderId());
        assertEquals(100.0, payment.getAmount());
        assertEquals("order", payment.getRazorpayOrderId());
        assertEquals("payment", payment.getRazorpayPaymentId());
        assertEquals("PENDING", payment.getStatus());
        assertNotNull(payment.getCreatedAt());
    }
}
