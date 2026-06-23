package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.PaymentFailedEvent;
import com.example.buildnest_ecommerce.event.PaymentSuccessfulEvent;
import com.example.buildnest_ecommerce.repository.PaymentRepository;
import com.example.buildnest_ecommerce.integration.RazorpayClientAdapter;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Service Implementation
 *
 * Handles payment processing, Razorpay integration, and payment lifecycle
 * management.
 * Manages payment initiation, callback processing, and payment status tracking.
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RazorpayClientAdapter razorpayAdapter;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Initiates a payment by creating a Razorpay order.
     *
     * Creates a payment record with PENDING status and communicates with Razorpay
     * to generate an order ID for payment processing.
     *
     * @param orderId the ID of the order for which payment is initiated (required)
     * @param amount  the payment amount in rupees (required)
     * @return the created Payment entity with Razorpay order ID
     */
    @Override
    public Payment initiatePayment(Long orderId, Double amount) {
        log.info("Initiating payment for order: {}, amount: {}", orderId, amount);
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());

        try {
            // Call Razorpay to create order
            String razorpayOrderId = razorpayAdapter.createOrder(amount, orderId);
            payment.setRazorpayOrderId(razorpayOrderId);
            log.info("Razorpay order created with id: {}", razorpayOrderId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment parameters: {}", e.getMessage(), e);
            payment.setStatus("FAILED");
            throw new PaymentProcessingException("Invalid payment parameters: " + e.getMessage());
        } catch (ExternalServiceException e) {
            log.error("Razorpay service error: {}", e.getMessage(), e);
            payment.setStatus("FAILED");
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error creating Razorpay order: {}", e.getMessage(), e);
            payment.setStatus("FAILED");
            throw new PaymentProcessingException("Payment initiation failed: " + e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    /**
     * Processes payment callback from Razorpay.
     *
     * Verifies the payment signature, updates payment status to SUCCESS,
     * and publishes PaymentSuccessfulEvent or PaymentFailedEvent accordingly.
     *
     * @param razorpayOrderId   the Razorpay order ID (required)
     * @param razorpayPaymentId the Razorpay payment ID (required)
     * @param razorpaySignature the HMAC-SHA256 signature for verification
     *                          (required)
     * @return the updated Payment entity with SUCCESS status
     * @throws RuntimeException if signature is invalid or payment is not found
     */
    @Override
    public Payment processPaymentCallback(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        log.info("Processing payment callback for Razorpay order: {}", razorpayOrderId);
        Long relatedOrderId = null;
        try {
            // Verify signature
            boolean isValid = razorpayAdapter.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (!isValid) {
                log.warn("Invalid signature for payment: {}", razorpayPaymentId);
                throw new RuntimeException("Invalid payment signature");
            }

            // Find and update payment
            Payment payment = paymentRepository.findAll().stream()
                    .filter(p -> p.getRazorpayOrderId().equals(razorpayOrderId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            relatedOrderId = payment.getOrderId();

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);
            domainEventPublisher.publish(new PaymentSuccessfulEvent(this, saved.getId(), saved.getOrderId(),
                    java.math.BigDecimal.valueOf(saved.getAmount())));
            return saved;
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            domainEventPublisher.publish(new PaymentFailedEvent(this, relatedOrderId, e.getMessage()));
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    /**
     * Retrieves a payment by order ID.
     *
     * @param orderId the ID of the order (required)
     * @return the Payment entity associated with the order
     * @throws RuntimeException if payment is not found for the order
     */
    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order: {}", orderId);
        return paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        log.info("Fetching payments for user: {}", userId);
        // Note: This requires joining with Order table in actual implementation
        return paymentRepository.findAll();
    }

    @Override
    public void refundPayment(Long paymentId) {
        log.info("Refunding payment with id: {}", paymentId);
        Payment payment = getPaymentById(paymentId);

        try {
            razorpayAdapter.refundPayment(payment.getRazorpayPaymentId(), payment.getAmount());
            payment.setStatus("REFUNDED");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("Payment refunded successfully: {}", paymentId);
        } catch (Exception e) {
            log.error("Error refunding payment", e);
            throw new RuntimeException("Refund failed", e);
        }
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        log.info("Fetching payment with id: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
    }
}
