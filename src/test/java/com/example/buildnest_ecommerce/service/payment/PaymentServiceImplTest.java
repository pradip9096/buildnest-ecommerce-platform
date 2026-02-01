package com.example.buildnest_ecommerce.service.payment;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.exception.ExternalServiceException;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.integration.RazorpayClientAdapter;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayClientAdapter razorpayAdapter;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should initiate payment successfully")
    void testInitiatePayment() {
        when(razorpayAdapter.createOrder(100.0, 1L)).thenReturn("rp-order");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.initiatePayment(1L, 100.0);
        assertEquals("PENDING", payment.getStatus());
        assertEquals("rp-order", payment.getRazorpayOrderId());
    }

    @Test
    @DisplayName("Should throw on invalid payment params")
    void testInitiatePaymentInvalid() {
        when(razorpayAdapter.createOrder(anyDouble(), any(Long.class)))
                .thenThrow(new IllegalArgumentException("bad"));

        assertThrows(PaymentProcessingException.class, () -> paymentService.initiatePayment(1L, 0.0));
    }

    @Test
    @DisplayName("Should propagate external service exception")
    void testInitiatePaymentExternalFailure() {
        when(razorpayAdapter.createOrder(anyDouble(), any(Long.class)))
                .thenThrow(new ExternalServiceException("Razorpay", "down"));

        assertThrows(ExternalServiceException.class, () -> paymentService.initiatePayment(1L, 100.0));
    }

    @Test
    @DisplayName("Should process payment callback successfully")
    void testProcessPaymentCallback() {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setOrderId(5L);
        payment.setRazorpayOrderId("rp-order");
        payment.setAmount(100.0);

        when(razorpayAdapter.verifySignature("rp-order", "rp-pay", "sig")).thenReturn(true);
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.processPaymentCallback("rp-order", "rp-pay", "sig");
        assertEquals("SUCCESS", result.getStatus());
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should fail callback on invalid signature")
    void testProcessPaymentCallbackInvalidSignature() {
        when(razorpayAdapter.verifySignature("rp-order", "rp-pay", "sig")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> paymentService.processPaymentCallback("rp-order", "rp-pay", "sig"));
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should get payment by order id")
    void testGetPaymentByOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        assertEquals(payment, paymentService.getPaymentByOrderId(1L));
    }

    @Test
    @DisplayName("Should refund payment")
    void testRefundPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setRazorpayPaymentId("rp-pay");
        payment.setAmount(50.0);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.refundPayment(1L);

        verify(razorpayAdapter).refundPayment("rp-pay", 50.0);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw when payment not found")
    void testGetPaymentByIdNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(99L));
    }
}
