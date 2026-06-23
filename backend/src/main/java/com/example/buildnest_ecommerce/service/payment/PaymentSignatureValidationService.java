package com.example.buildnest_ecommerce.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Payment Signature Validation Service
 * 
 * Implements PCI DSS compliance for payment signature verification.
 * - Validates Razorpay payment signatures using HMAC-SHA256
 * - Prevents payment tampering and ensures authenticity
 * - Logs all validation attempts for audit trail
 * - Implements cryptographic best practices
 * 
 * SYS-PAY-006: Payment signature verification with PCI DSS compliance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSignatureValidationService {

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    /**
     * Validates Razorpay payment signature with comprehensive security checks
     * 
     * PCI DSS Requirement: Verify all payment transactions are authenticated
     * 
     * @param orderId   Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Signature received from payment gateway
     * @return true if signature is valid, false otherwise
     */
    public boolean validatePaymentSignature(String orderId, String paymentId, String signature) {
        // Input validation
        if (!validateInputs(orderId, paymentId, signature)) {
            log.warn("Invalid inputs received for signature validation");
            return false;
        }

        try {
            // Construct the message in correct format
            String message = orderId + "|" + paymentId;

            // Generate HMAC-SHA256 hash using Razorpay secret key
            String generatedSignature = generateHmacSHA256(message, razorpayKeySecret);

            // Perform constant-time comparison to prevent timing attacks
            boolean isValid = constantTimeEquals(generatedSignature, signature);

            if (isValid) {
                log.info("✓ Payment signature validated successfully for orderId: {} paymentId: {}", orderId,
                        paymentId);
                logAuditEvent("PAYMENT_SIGNATURE_VALIDATED", orderId, paymentId, true);
            } else {
                log.warn("✗ Payment signature validation failed for orderId: {} paymentId: {}", orderId, paymentId);
                logAuditEvent("PAYMENT_SIGNATURE_VALIDATION_FAILED", orderId, paymentId, false);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating payment signature: {}", e.getMessage(), e);
            logAuditEvent("PAYMENT_SIGNATURE_VALIDATION_ERROR", orderId, paymentId, false);
            return false;
        }
    }

    /**
     * Validates webhook signature for payment status updates
     * Ensures webhooks are from legitimate Razorpay servers
     * 
     * @param body              Webhook body content
     * @param webhookSecret     Webhook signing secret
     * @param receivedSignature Signature from webhook header
     * @return true if webhook is authentic
     */
    public boolean validateWebhookSignature(String body, String webhookSecret, String receivedSignature) {
        if (body == null || body.isEmpty() || webhookSecret == null) {
            log.warn("Invalid webhook inputs for signature validation");
            return false;
        }

        try {
            String generatedSignature = generateHmacSHA256(body, webhookSecret);
            boolean isValid = constantTimeEquals(generatedSignature, receivedSignature);

            if (isValid) {
                log.info("✓ Webhook signature validated successfully");
                logAuditEvent("WEBHOOK_SIGNATURE_VALIDATED", body.hashCode() + "", "", true);
            } else {
                log.warn("✗ Webhook signature validation failed - possible tampering attempt");
                logAuditEvent("WEBHOOK_SIGNATURE_FAILED", body.hashCode() + "", "", false);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generates HMAC-SHA256 hash
     * 
     * PCI DSS Requirement: Use strong cryptography (SHA-256 or higher)
     * 
     * @param message Message to hash
     * @param secret  Secret key for HMAC
     * @return Hex-encoded HMAC-SHA256 hash
     */
    private String generateHmacSHA256(String message, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Converts byte array to hexadecimal string
     * 
     * @param bytes Bytes to convert
     * @return Hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     * 
     * PCI DSS Security Best Practice: Prevent cryptographic attacks
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal (using constant-time comparison)
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }

    /**
     * Validates input parameters for signature verification
     * 
     * @param orderId   Order ID
     * @param paymentId Payment ID
     * @param signature Signature
     * @return true if all inputs are valid
     */
    private boolean validateInputs(String orderId, String paymentId, String signature) {
        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Invalid orderId provided for signature validation");
            return false;
        }

        if (paymentId == null || paymentId.trim().isEmpty()) {
            log.warn("Invalid paymentId provided for signature validation");
            return false;
        }

        if (signature == null || signature.trim().isEmpty()) {
            log.warn("Invalid signature provided for validation");
            return false;
        }

        // Signature should be 64 characters (256 bits in hex)
        if (signature.length() != 64) {
            log.warn("Invalid signature length: {} (expected 64)", signature.length());
            return false;
        }

        return true;
    }

    /**
     * Logs audit event for payment signature verification
     * Supports PCI DSS audit trail requirements
     * 
     * @param eventType Type of event (VALIDATED, FAILED, ERROR)
     * @param orderId   Order ID
     * @param paymentId Payment ID
     * @param success   Whether verification succeeded
     */
    private void logAuditEvent(String eventType, String orderId, String paymentId, boolean success) {
        try {
            // Format: TIMESTAMP | EVENT_TYPE | ORDER_ID | PAYMENT_ID | SUCCESS |
            // RAZORPAY_KEY_ID
            String auditLog = String.format(
                    "timestamp=%d | event=%s | orderId=%s | paymentId=%s | success=%s | keyId=%s",
                    System.currentTimeMillis(),
                    eventType,
                    sanitizeForLogging(orderId),
                    sanitizeForLogging(paymentId),
                    success,
                    razorpayKeyId);

            log.info("AUDIT_EVENT: {}", auditLog);
        } catch (Exception e) {
            log.error("Error logging audit event: {}", e.getMessage());
        }
    }

    /**
     * Sanitizes values for safe logging (prevents injection attacks)
     * 
     * @param value Value to sanitize
     * @return Sanitized value
     */
    private String sanitizeForLogging(String value) {
        if (value == null) {
            return "null";
        }
        // Remove any special characters that could break log format
        return value.replaceAll("[|\\n\\r]", "_");
    }

    /**
     * Gets cryptographic validation status information
     * Useful for health checks and monitoring
     * 
     * @return Status information
     */
    public String getValidationStatus() {
        return String.format(
                "PaymentSignatureValidation: ACTIVE | Algorithm: HmacSHA256 | KeyLength: %d",
                razorpayKeySecret.length());
    }
}
