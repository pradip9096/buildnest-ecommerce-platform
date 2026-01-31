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
}
