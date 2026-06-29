package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.integration.RazorpayClientAdapter;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.repository.PaymentRepository;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentServiceImpl#processWebhookEvent} (PAY-01, #60).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PaymentServiceImpl — processWebhookEvent")
class PaymentWebhookServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RazorpayClientAdapter razorpayAdapter;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private PaymentSignatureValidationService signatureValidationService;
    @Mock private OrderService orderService;

    private PaymentServiceImpl service;

    private static final String WEBHOOK_SECRET = "test_webhook_secret";

    private static final String CAPTURED_BODY = """
            {
              "event": "payment.captured",
              "payload": {
                "payment": {
                  "entity": {
                    "id": "pay_test123",
                    "order_id": "order_rzp_abc"
                  }
                }
              }
            }""";

    private static final String FAILED_BODY = """
            {
              "event": "payment.failed",
              "payload": {
                "payment": {
                  "entity": {
                    "id": "pay_test456",
                    "order_id": "order_rzp_abc"
                  }
                }
              }
            }""";

    private static final String UNKNOWN_EVENT_BODY = """
            {
              "event": "order.paid",
              "payload": {
                "payment": {
                  "entity": {
                    "id": "pay_other",
                    "order_id": "order_rzp_abc"
                  }
                }
              }
            }""";

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(
                paymentRepository, razorpayAdapter, domainEventPublisher,
                signatureValidationService, new ObjectMapper(), orderService);
        ReflectionTestUtils.setField(service, "razorpayWebhookSecret", WEBHOOK_SECRET);
    }

    private Payment pendingPayment(Long id, Long orderId, String rzpOrderId) {
        Payment p = new Payment();
        p.setId(id);
        p.setOrderId(orderId);
        p.setRazorpayOrderId(rzpOrderId);
        p.setAmount(999.0);
        p.setStatus("PENDING");
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    @Test
    @DisplayName("valid payment.captured event — sets SUCCESS and updates order to PAID")
    void capturedEvent_updatesPaymentAndOrder() {
        Payment payment = pendingPayment(1L, 10L, "order_rzp_abc");
        when(signatureValidationService.validateWebhookSignature(CAPTURED_BODY, WEBHOOK_SECRET, "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_rzp_abc")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWebhookEvent(CAPTURED_BODY, "sig");

        verify(paymentRepository).save(argThat(p -> "SUCCESS".equals(p.getStatus()) && "pay_test123".equals(p.getRazorpayPaymentId())));
        verify(orderService).updateOrderStatus(10L, "PAID");
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("valid payment.failed event — sets FAILED and updates order to PAYMENT_FAILED")
    void failedEvent_updatesPaymentAndOrder() {
        Payment payment = pendingPayment(2L, 20L, "order_rzp_abc");
        when(signatureValidationService.validateWebhookSignature(FAILED_BODY, WEBHOOK_SECRET, "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_rzp_abc")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWebhookEvent(FAILED_BODY, "sig");

        verify(paymentRepository).save(argThat(p -> "FAILED".equals(p.getStatus())));
        verify(orderService).updateOrderStatus(20L, "PAYMENT_FAILED");
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("invalid signature — throws PaymentProcessingException, no side effects")
    void invalidSignature_throwsAndNoSideEffects() {
        when(signatureValidationService.validateWebhookSignature(anyString(), anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.processWebhookEvent(CAPTURED_BODY, "bad_sig"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Invalid webhook signature");

        verifyNoInteractions(paymentRepository, orderService);
    }

    @Test
    @DisplayName("idempotency — already-SUCCESS payment is skipped without re-processing")
    void idempotency_alreadyProcessed_skipped() {
        Payment payment = pendingPayment(3L, 30L, "order_rzp_abc");
        payment.setStatus("SUCCESS");
        when(signatureValidationService.validateWebhookSignature(CAPTURED_BODY, WEBHOOK_SECRET, "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_rzp_abc")).thenReturn(Optional.of(payment));

        service.processWebhookEvent(CAPTURED_BODY, "sig");

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(orderService, domainEventPublisher);
    }

    @Test
    @DisplayName("payment not found for Razorpay order — silently ignored")
    void paymentNotFound_ignored() {
        when(signatureValidationService.validateWebhookSignature(CAPTURED_BODY, WEBHOOK_SECRET, "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId(anyString())).thenReturn(Optional.empty());

        service.processWebhookEvent(CAPTURED_BODY, "sig");

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(orderService, domainEventPublisher);
    }

    @Test
    @DisplayName("unknown event type — ignored without error")
    void unknownEvent_ignored() {
        Payment payment = pendingPayment(4L, 40L, "order_rzp_abc");
        when(signatureValidationService.validateWebhookSignature(UNKNOWN_EVENT_BODY, WEBHOOK_SECRET, "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_rzp_abc")).thenReturn(Optional.of(payment));

        service.processWebhookEvent(UNKNOWN_EVENT_BODY, "sig");

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(orderService, domainEventPublisher);
    }

    @Test
    @DisplayName("malformed JSON body — throws PaymentProcessingException")
    void malformedBody_throwsProcessingException() {
        when(signatureValidationService.validateWebhookSignature(anyString(), anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.processWebhookEvent("not-json", "sig"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Webhook processing failed");
    }
}
