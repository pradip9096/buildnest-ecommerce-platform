package com.example.buildnest_ecommerce.integration;

import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.Payment;
import com.razorpay.PaymentClient;
import com.razorpay.Refund;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RazorpayClientAdapter tests")
class RazorpayClientAdapterTest {

    private String hmacSha256(String message, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Test
    @DisplayName("Should create order using client")
    void testCreateOrder() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        RazorpayClient client = mock(RazorpayClient.class);
        OrderClient orderClient = mock(OrderClient.class);
        ReflectionTestUtils.setField(client, "orders", orderClient);

        Order order = mock(Order.class);
        when(orderClient.create(any(JSONObject.class))).thenReturn(order);
        when(order.get("id")).thenReturn("order_123");

        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        String id = adapter.createOrder(100.0, 1L);
        assertEquals("order_123", id);
    }

    @Test
    @DisplayName("Should verify signature")
    void testVerifySignature() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        String signature = hmacSha256("order|pay", "secret");
        assertTrue(adapter.verifySignature("order", "pay", signature));
        assertFalse(adapter.verifySignature("order", "pay", signature + "x"));
    }

    @Test
    @DisplayName("Should fetch payment details")
    void testFetchPaymentDetails() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        RazorpayClient client = mock(RazorpayClient.class);
        PaymentClient paymentClient = mock(PaymentClient.class);
        ReflectionTestUtils.setField(client, "payments", paymentClient);

        Payment payment = mock(Payment.class);
        when(paymentClient.fetch("pay_1")).thenReturn(payment);

        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        assertEquals(payment, adapter.fetchPaymentDetails("pay_1"));
    }

    @Test
    @DisplayName("Should process refund without error")
    void testRefundPayment() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        PaymentClient paymentClient = mock(PaymentClient.class);
        Refund refundResponse = mock(Refund.class);
        when(paymentClient.refund(any(String.class), any(JSONObject.class))).thenReturn(refundResponse);

        RazorpayClient client = mock(RazorpayClient.class);
        ReflectionTestUtils.setField(client, "payments", paymentClient);
        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        assertDoesNotThrow(() -> adapter.refundPayment("pay_1", 50.0));
        verify(paymentClient).refund(eq("pay_1"), any(JSONObject.class));
    }

    @Test
    @DisplayName("Should return false when signature verification fails")
    void testVerifySignatureFailure() {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", null);

        assertFalse(adapter.verifySignature("order", "pay", "sig"));
    }

    @Test
    @DisplayName("createOrder — client throws → wraps in RuntimeException")
    void testCreateOrder_clientThrows_throwsRuntimeException() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        RazorpayClient client = mock(RazorpayClient.class);
        OrderClient orderClient = mock(OrderClient.class);
        ReflectionTestUtils.setField(client, "orders", orderClient);
        when(orderClient.create(any(JSONObject.class))).thenThrow(new RuntimeException("API error"));
        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adapter.createOrder(100.0, 1L));
        assertTrue(ex.getMessage().contains("Failed to create Razorpay order"));
    }

    @Test
    @DisplayName("refundPayment — client throws → wraps in PaymentProcessingException")
    void testRefundPayment_clientThrows_throwsPaymentProcessingException() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        RazorpayClient client = mock(RazorpayClient.class);
        PaymentClient paymentClient = mock(PaymentClient.class);
        ReflectionTestUtils.setField(client, "payments", paymentClient);
        when(paymentClient.refund(any(String.class), any(JSONObject.class)))
                .thenThrow(new RuntimeException("refund API error"));
        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        assertThrows(PaymentProcessingException.class,
                () -> adapter.refundPayment("pay_1", 50.0));
    }

    @Test
    @DisplayName("fetchPaymentDetails — client throws → wraps in RuntimeException")
    void testFetchPaymentDetails_clientThrows_throwsRuntimeException() throws Exception {
        RazorpayClientAdapter adapter = new RazorpayClientAdapter();
        ReflectionTestUtils.setField(adapter, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(adapter, "razorpayKeySecret", "secret");

        RazorpayClient client = mock(RazorpayClient.class);
        PaymentClient paymentClient = mock(PaymentClient.class);
        ReflectionTestUtils.setField(client, "payments", paymentClient);
        when(paymentClient.fetch(any(String.class))).thenThrow(new RuntimeException("fetch error"));
        ReflectionTestUtils.setField(adapter, "razorpayClient", client);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adapter.fetchPaymentDetails("pay_1"));
        assertTrue(ex.getMessage().contains("Failed to fetch payment details"));
    }

}
