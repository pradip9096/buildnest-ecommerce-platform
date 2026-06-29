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
}
