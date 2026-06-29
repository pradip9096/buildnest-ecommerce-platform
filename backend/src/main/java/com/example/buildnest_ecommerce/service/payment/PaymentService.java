package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.model.entity.Payment;
import java.util.List;

public interface PaymentService {
    Payment initiatePayment(Long orderId, Double amount);
    Payment processPaymentCallback(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
    Payment getPaymentByOrderId(Long orderId);
    List<Payment> getPaymentsByUserId(Long userId);
    void refundPayment(Long paymentId);
    Payment getPaymentById(Long paymentId);

    /**
     * Processes an inbound Razorpay webhook event (PAY-01, #60).
     * Validates the webhook signature, handles payment.captured and payment.failed
     * events, and updates Payment and Order status accordingly.
     *
     * @param rawBody           raw JSON body exactly as received (for HMAC verification)
     * @param razorpaySignature value of the X-Razorpay-Signature header
     */
    void processWebhookEvent(String rawBody, String razorpaySignature);

    /**
     * Issues a full or partial refund for the payment associated with the given order (PAY-02, #61).
     * Validates that the refund amount does not exceed the original payment amount, calls the
     * Razorpay refund API, and updates Payment status to REFUNDED or PARTIALLY_REFUNDED.
     *
     * @param orderId internal order ID
     * @param amount  refund amount in rupees (must be &gt; 0 and ≤ original payment amount)
     * @param reason  optional human-readable refund reason for audit purposes
     * @return the updated Payment entity
     */
    Payment processRefund(Long orderId, Double amount, String reason);
}
