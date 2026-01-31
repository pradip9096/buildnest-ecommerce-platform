package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setOrderId(1001L);
        testPayment.setAmount(250.00);
        testPayment.setRazorpayOrderId("order_test123");
        testPayment.setRazorpayPaymentId("pay_test456");
        testPayment.setStatus("PENDING");
        testPayment.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testPayment);
        entityManager.flush();
    }

    @Test
    @DisplayName("TC-PAY-REPO-001: Find payment by order ID")
    void testFindByOrderId() {
        // Arrange
        Long orderId = testPayment.getOrderId();

        // Act
        List<Payment> payments = paymentRepository.findAll();
        Optional<Payment> found = payments.stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst();

        // Assert
        assertTrue(found.isPresent());
        assertEquals(orderId, found.get().getOrderId());
        assertEquals("order_test123", found.get().getRazorpayOrderId());
    }

    @Test
    @DisplayName("TC-PAY-REPO-002: Find failed payments")
    void testFindFailedPayments() {
        // Arrange
        Payment failedPayment1 = new Payment();
        failedPayment1.setOrderId(1002L);
        failedPayment1.setAmount(100.00);
        failedPayment1.setStatus("FAILED");
        failedPayment1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(failedPayment1);

        Payment failedPayment2 = new Payment();
        failedPayment2.setOrderId(1003L);
        failedPayment2.setAmount(150.00);
        failedPayment2.setStatus("FAILED");
        failedPayment2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(failedPayment2);

        Payment successPayment = new Payment();
        successPayment.setOrderId(1004L);
        successPayment.setAmount(200.00);
        successPayment.setStatus("SUCCESS");
        successPayment.setCreatedAt(LocalDateTime.now());
        entityManager.persist(successPayment);

        entityManager.flush();

        // Act
        List<Payment> allPayments = paymentRepository.findAll();
        long failedCount = allPayments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();

        // Assert
        assertEquals(2, failedCount);
    }

    @Test
    @DisplayName("TC-PAY-REPO-003: Payment status transitions")
    void testPaymentStatusTransitions() {
        // Arrange
        Long paymentId = testPayment.getId();

        // Act - Transition from PENDING to SUCCESS
        testPayment.setStatus("SUCCESS");
        testPayment.setUpdatedAt(LocalDateTime.now());
        testPayment.setRazorpayPaymentId("pay_success789");
        paymentRepository.save(testPayment);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Payment> updated = paymentRepository.findById(paymentId);
        assertTrue(updated.isPresent());
        assertEquals("SUCCESS", updated.get().getStatus());
        assertEquals("pay_success789", updated.get().getRazorpayPaymentId());
        assertNotNull(updated.get().getUpdatedAt());
    }

    @Test
    @DisplayName("TC-PAY-REPO-004: Refund tracking")
    void testRefundTracking() {
        // Arrange
        Payment successPayment = new Payment();
        successPayment.setOrderId(2001L);
        successPayment.setAmount(500.00);
        successPayment.setRazorpayOrderId("order_refund123");
        successPayment.setRazorpayPaymentId("pay_refund456");
        successPayment.setStatus("SUCCESS");
        successPayment.setCreatedAt(LocalDateTime.now());
        entityManager.persist(successPayment);
        entityManager.flush();

        Long paymentId = successPayment.getId();

        // Act - Refund the payment
        successPayment.setStatus("REFUNDED");
        successPayment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(successPayment);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Payment> refunded = paymentRepository.findById(paymentId);
        assertTrue(refunded.isPresent());
        assertEquals("REFUNDED", refunded.get().getStatus());
        assertEquals(500.00, refunded.get().getAmount());
    }

    @Test
    @DisplayName("TC-PAY-REPO-005: Payment audit trail")
    void testPaymentAuditTrail() {
        // Arrange
        Payment payment1 = new Payment();
        payment1.setOrderId(3001L);
        payment1.setAmount(100.00);
        payment1.setStatus("SUCCESS");
        payment1.setCreatedAt(LocalDateTime.now().minusDays(2));
        entityManager.persist(payment1);

        Payment payment2 = new Payment();
        payment2.setOrderId(3002L);
        payment2.setAmount(200.00);
        payment2.setStatus("SUCCESS");
        payment2.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(payment2);

        Payment payment3 = new Payment();
        payment3.setOrderId(3003L);
        payment3.setAmount(300.00);
        payment3.setStatus("SUCCESS");
        payment3.setCreatedAt(LocalDateTime.now());
        entityManager.persist(payment3);

        entityManager.flush();

        // Act
        List<Payment> allPayments = paymentRepository.findAll();

        // Assert - Verify audit trail exists
        assertEquals(4, allPayments.size()); // Including testPayment
        assertTrue(allPayments.stream().allMatch(p -> p.getCreatedAt() != null));
    }

    @Test
    @DisplayName("TC-PAY-REPO-006: Payment amount validation")
    void testPaymentAmountValidation() {
        // Arrange
        Payment zeroAmountPayment = new Payment();
        zeroAmountPayment.setOrderId(4001L);
        zeroAmountPayment.setAmount(0.0);
        zeroAmountPayment.setStatus("PENDING");
        zeroAmountPayment.setCreatedAt(LocalDateTime.now());

        // Act - Save payment with zero amount
        Payment saved = paymentRepository.save(zeroAmountPayment);
        entityManager.flush();

        // Assert - Zero amount should be allowed (business logic should validate)
        assertNotNull(saved.getId());
        assertEquals(0.0, saved.getAmount());

        // Test negative amount
        Payment negativeAmountPayment = new Payment();
        negativeAmountPayment.setOrderId(4002L);
        negativeAmountPayment.setAmount(-100.00);
        negativeAmountPayment.setStatus("PENDING");
        negativeAmountPayment.setCreatedAt(LocalDateTime.now());

        // Act & Assert - Negative amount should be allowed at DB level
        // (business validation should prevent this)
        assertDoesNotThrow(() -> {
            Payment savedNegative = paymentRepository.save(negativeAmountPayment);
            entityManager.flush();
            assertNotNull(savedNegative.getId());
        });
    }
}
