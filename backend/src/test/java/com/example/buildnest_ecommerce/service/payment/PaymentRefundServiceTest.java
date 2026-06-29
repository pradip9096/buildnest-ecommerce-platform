package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentServiceImpl#processRefund} (PAY-02, #61).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl — processRefund")
class PaymentRefundServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RazorpayClientAdapter razorpayAdapter;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private PaymentSignatureValidationService signatureValidationService;
    @Mock private OrderService orderService;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(
                paymentRepository, razorpayAdapter, domainEventPublisher,
                signatureValidationService, new ObjectMapper(), orderService);
        ReflectionTestUtils.setField(service, "razorpayWebhookSecret", "test_secret");
    }

    private Payment successPayment(Long id, Long orderId, double amount) {
        Payment p = new Payment();
        p.setId(id);
        p.setOrderId(orderId);
        p.setAmount(amount);
        p.setRefundedAmount(0.0);
        p.setRazorpayPaymentId("pay_rzp_" + id);
        p.setStatus("SUCCESS");
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    @Test
    @DisplayName("full refund — status becomes REFUNDED")
    void fullRefund_setsStatusRefunded() {
        Payment payment = successPayment(1L, 10L, 500.0);
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(razorpayAdapter).refundPayment(anyString(), anyDouble());

        Payment result = service.processRefund(10L, 500.0, "Customer request");

        assertThat(result.getStatus()).isEqualTo("REFUNDED");
        assertThat(result.getRefundedAmount()).isEqualTo(500.0);
        assertThat(result.getRefundReason()).isEqualTo("Customer request");
        assertThat(result.getRefundInitiatedAt()).isNotNull();
        verify(razorpayAdapter).refundPayment("pay_rzp_1", 500.0);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("partial refund — status becomes PARTIALLY_REFUNDED")
    void partialRefund_setsStatusPartiallyRefunded() {
        Payment payment = successPayment(2L, 20L, 1000.0);
        when(paymentRepository.findByOrderId(20L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(razorpayAdapter).refundPayment(anyString(), anyDouble());

        Payment result = service.processRefund(20L, 300.0, "Partial return");

        assertThat(result.getStatus()).isEqualTo("PARTIALLY_REFUNDED");
        assertThat(result.getRefundedAmount()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("amount exceeds original — throws PaymentProcessingException")
    void amountExceedsOriginal_throws() {
        Payment payment = successPayment(3L, 30L, 200.0);
        when(paymentRepository.findByOrderId(30L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.processRefund(30L, 250.0, "reason"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("exceeds refundable balance");

        verifyNoInteractions(razorpayAdapter);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("payment not in SUCCESS status — throws PaymentProcessingException")
    void nonSuccessPayment_throws() {
        Payment payment = successPayment(4L, 40L, 500.0);
        payment.setStatus("PENDING");
        when(paymentRepository.findByOrderId(40L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.processRefund(40L, 100.0, null))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("SUCCESS status");

        verifyNoInteractions(razorpayAdapter);
    }

    @Test
    @DisplayName("no payment found for order — throws ResourceNotFoundException")
    void paymentNotFound_throws() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processRefund(99L, 100.0, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("gateway call fails — throws PaymentProcessingException")
    void gatewayFails_throws() {
        Payment payment = successPayment(5L, 50L, 500.0);
        when(paymentRepository.findByOrderId(50L)).thenReturn(Optional.of(payment));
        doThrow(new PaymentProcessingException("gateway error"))
                .when(razorpayAdapter).refundPayment(anyString(), anyDouble());

        assertThatThrownBy(() -> service.processRefund(50L, 200.0, null))
                .isInstanceOf(PaymentProcessingException.class);

        verify(paymentRepository, never()).save(any());
    }
}
