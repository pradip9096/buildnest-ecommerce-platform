package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.PaymentFailedEvent;
import com.example.buildnest_ecommerce.event.PaymentSuccessfulEvent;
import com.example.buildnest_ecommerce.repository.PaymentRepository;
import com.example.buildnest_ecommerce.integration.RazorpayClientAdapter;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ExternalServiceException;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClientAdapter razorpayAdapter;
    private final DomainEventPublisher domainEventPublisher;
    private final PaymentSignatureValidationService paymentSignatureValidationService;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            RazorpayClientAdapter razorpayAdapter,
            DomainEventPublisher domainEventPublisher,
            PaymentSignatureValidationService paymentSignatureValidationService,
            ObjectMapper objectMapper,
            @Lazy OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.razorpayAdapter = razorpayAdapter;
        this.domainEventPublisher = domainEventPublisher;
        this.paymentSignatureValidationService = paymentSignatureValidationService;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @Override
    public Payment initiatePayment(Long orderId, Double amount) {
        log.info("Initiating payment for order: {}, amount: {}", orderId, amount);
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());

        try {
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

    @Override
    public Payment processPaymentCallback(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        log.info("Processing payment callback for Razorpay order: {}", razorpayOrderId);
        Long relatedOrderId = null;
        try {
            boolean isValid = razorpayAdapter.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (!isValid) {
                log.warn("Invalid signature for payment: {}", razorpayPaymentId);
                throw new RuntimeException("Invalid payment signature");
            }

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

    /**
     * Processes inbound Razorpay webhook events (PAY-01, #60).
     *
     * Validates the HMAC-SHA256 signature, handles payment.captured and
     * payment.failed events with idempotency protection, and updates both
     * Payment and Order status accordingly.
     */
    @Override
    public void processWebhookEvent(String rawBody, String razorpaySignature) {
        log.info("Processing Razorpay webhook event");

        boolean signatureValid = paymentSignatureValidationService.validateWebhookSignature(
                rawBody, razorpayWebhookSecret, razorpaySignature);
        if (!signatureValid) {
            log.warn("Razorpay webhook signature validation failed — rejecting event");
            throw new PaymentProcessingException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText();
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String razorpayPaymentId = paymentEntity.path("id").asText(null);
            String razorpayOrderId = paymentEntity.path("order_id").asText(null);

            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                log.warn("Webhook event '{}' has no order_id — ignoring", event);
                return;
            }

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
            if (payment == null) {
                log.info("No local payment record for Razorpay order {} — ignoring webhook", razorpayOrderId);
                return;
            }

            // Idempotency: skip already-processed events
            if (!"PENDING".equals(payment.getStatus())) {
                log.info("Payment {} already in status '{}' — skipping duplicate webhook",
                        payment.getId(), payment.getStatus());
                return;
            }

            switch (event) {
                case "payment.captured" -> {
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setStatus("SUCCESS");
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    orderService.updateOrderStatus(payment.getOrderId(), "PAID");
                    domainEventPublisher.publish(new PaymentSuccessfulEvent(
                            this, payment.getId(), payment.getOrderId(),
                            java.math.BigDecimal.valueOf(payment.getAmount())));
                    log.info("Payment {} captured — order {} set to PAID", payment.getId(), payment.getOrderId());
                }
                case "payment.failed" -> {
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setStatus("FAILED");
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    orderService.updateOrderStatus(payment.getOrderId(), "PAYMENT_FAILED");
                    domainEventPublisher.publish(new PaymentFailedEvent(
                            this, payment.getOrderId(), "Payment failed via webhook"));
                    log.info("Payment {} failed — order {} set to PAYMENT_FAILED",
                            payment.getId(), payment.getOrderId());
                }
                default -> log.info("Unhandled Razorpay webhook event '{}' — ignoring", event);
            }
        } catch (PaymentProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing or processing Razorpay webhook body", e);
            throw new PaymentProcessingException("Webhook processing failed: " + e.getMessage());
        }
    }

    @Override
    public Payment processRefund(Long orderId, Double amount, String reason) {
        log.info("Processing refund for order: {}, amount: {}", orderId, amount);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new PaymentProcessingException(
                    "Refund is only allowed for payments in SUCCESS status; current status: " + payment.getStatus());
        }

        double alreadyRefunded = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : 0.0;
        double maxRefundable = payment.getAmount() - alreadyRefunded;
        if (amount > maxRefundable + 0.001) {
            throw new PaymentProcessingException(String.format(
                    "Refund amount %.2f exceeds refundable balance %.2f", amount, maxRefundable));
        }

        try {
            razorpayAdapter.refundPayment(payment.getRazorpayPaymentId(), amount);
        } catch (PaymentProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Razorpay refund call failed for order {}", orderId, e);
            throw new PaymentProcessingException("Refund gateway call failed: " + e.getMessage());
        }

        double newRefundedAmount = alreadyRefunded + amount;
        boolean fullyRefunded = newRefundedAmount >= payment.getAmount() - 0.001;
        payment.setRefundedAmount(newRefundedAmount);
        payment.setRefundReason(reason);
        payment.setRefundInitiatedAt(LocalDateTime.now());
        payment.setStatus(fullyRefunded ? "REFUNDED" : "PARTIALLY_REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        log.info("Refund processed for order {}: amount={}, status={}", orderId, amount, payment.getStatus());
        return payment;
    }
}
