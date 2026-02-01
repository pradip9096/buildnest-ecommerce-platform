package com.example.buildnest_ecommerce.service.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentSignatureValidationService tests")
class PaymentSignatureValidationServiceTest {

    private String hmacSha256(String message, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    @DisplayName("Should validate payment signature when correct")
    void testValidatePaymentSignature() throws Exception {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String orderId = "order_1";
        String paymentId = "pay_1";
        String signature = hmacSha256(orderId + "|" + paymentId, "secret");

        assertTrue(service.validatePaymentSignature(orderId, paymentId, signature));
        assertFalse(service.validatePaymentSignature(orderId, paymentId, signature.substring(0, 63)));
    }

    @Test
    @DisplayName("Should reject incorrect payment signature of valid length")
    void testValidatePaymentSignatureIncorrectSignature() throws Exception {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String orderId = "order_2";
        String paymentId = "pay_2";
        String signature = hmacSha256(orderId + "|" + paymentId, "secret");
        String wrongSignature = signature.substring(0, 63) + (signature.endsWith("a") ? "b" : "a");

        assertFalse(service.validatePaymentSignature(orderId, paymentId, wrongSignature));
    }

    @Test
    @DisplayName("Should validate webhook signature when correct")
    void testValidateWebhookSignature() throws Exception {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String body = "payload";
        String signature = hmacSha256(body, "whSecret");

        assertTrue(service.validateWebhookSignature(body, "whSecret", signature));
        assertFalse(service.validateWebhookSignature(body, "whSecret", signature.substring(0, 60)));
    }

    @Test
    @DisplayName("Should reject incorrect webhook signature of valid length")
    void testValidateWebhookSignatureIncorrectSignature() throws Exception {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String body = "payload";
        String signature = hmacSha256(body, "whSecret");
        String wrongSignature = signature.substring(0, 63) + (signature.endsWith("a") ? "b" : "a");

        assertFalse(service.validateWebhookSignature(body, "whSecret", wrongSignature));
    }

    @Test
    @DisplayName("Should reject invalid webhook inputs")
    void testValidateWebhookSignatureInvalidInputs() {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        assertFalse(service.validateWebhookSignature(null, "whSecret", "sig"));
        assertFalse(service.validateWebhookSignature("", "whSecret", "sig"));
        assertFalse(service.validateWebhookSignature("payload", null, "sig"));
    }

    @Test
    @DisplayName("Should return status with key length")
    void testGetValidationStatus() {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String status = service.getValidationStatus();
        assertTrue(status.contains("KeyLength: 6"));
    }

    @Test
    @DisplayName("Should reject invalid inputs")
    void testValidateInputsFailures() {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        assertFalse(service.validatePaymentSignature(null, "p", "sig"));
        assertFalse(service.validatePaymentSignature("o", "", "sig"));
        assertFalse(service.validatePaymentSignature("o", "p", ""));
    }

    @Test
    @DisplayName("Should return false when signature validation throws exception")
    void testValidatePaymentSignatureHandlesException() {
        PaymentSignatureValidationService service = new PaymentSignatureValidationService();
        ReflectionTestUtils.setField(service, "razorpayKeySecret", null);
        ReflectionTestUtils.setField(service, "razorpayKeyId", "keyId");

        String signature = "a".repeat(64);

        assertFalse(service.validatePaymentSignature("order_3", "pay_3", signature));
    }
}
