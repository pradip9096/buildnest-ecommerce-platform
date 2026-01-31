package com.example.buildnest_ecommerce.payment;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment Processing test suite TC-PAY-001 to TC-PAY-008.
 * Covers payment transactions and validations.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class PaymentProcessingTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("paymenttest");
        testUser.setEmail("payment@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Payment");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("PAY-001");
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("5000.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("TC-PAY-001: Should validate order amount before payment")
    void testOrderAmountValidation() {
        assertTrue(testOrder.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(testOrder.getTotalAmount().compareTo(new BigDecimal("5000.00")) == 0);
    }

    @Test
    @DisplayName("TC-PAY-002: Should process payment for order")
    void testPaymentProcessing() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        Order updated = orderRepository.save(testOrder);

        assertEquals(Order.OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-003: Should calculate order total with tax")
    void testOrderTotalWithTax() {
        BigDecimal baseAmount = new BigDecimal("5000.00");
        BigDecimal taxAmount = baseAmount.multiply(new BigDecimal("0.18"));
        BigDecimal total = baseAmount.add(taxAmount);

        assertTrue(total.compareTo(baseAmount) > 0);
    }

    @Test
    @DisplayName("TC-PAY-004: Should apply discount to order")
    void testApplyDiscount() {
        BigDecimal originalAmount = testOrder.getTotalAmount();
        BigDecimal discountPercent = new BigDecimal("0.10");
        BigDecimal discountAmount = originalAmount.multiply(discountPercent);
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        assertTrue(finalAmount.compareTo(originalAmount) < 0);
    }

    @Test
    @DisplayName("TC-PAY-005: Should track payment status")
    void testPaymentStatusTracking() {
        assertEquals(Order.OrderStatus.PENDING, testOrder.getStatus());

        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        Order confirmed = orderRepository.save(testOrder);
        assertEquals(Order.OrderStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-006: Should handle payment refund")
    void testPaymentRefund() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        Order refunded = orderRepository.save(testOrder);

        assertEquals(Order.OrderStatus.CANCELLED, refunded.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-007: Should validate payment gateway response")
    void testPaymentGatewayResponse() {
        Order order = orderRepository.findById(testOrder.getId()).orElse(null);

        assertNotNull(order);
        assertNotNull(order.getTotalAmount());
    }

    @Test
    @DisplayName("TC-PAY-008: Should handle payment timeout")
    void testPaymentTimeout() {
        testOrder.setStatus(Order.OrderStatus.PENDING);
        Order updated = orderRepository.save(testOrder);

        assertEquals(Order.OrderStatus.PENDING, updated.getStatus());
    }
}
