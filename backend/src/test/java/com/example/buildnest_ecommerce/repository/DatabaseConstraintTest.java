package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.config.TestClockConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database constraint validation tests for entity integrity.
 * Tests TC-DB-CONS-001 through TC-DB-CONS-006.
 * Addresses Mitigation Strategy #5: Validate entity constraints and DB schema.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestClockConfig.class })
class DatabaseConstraintTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    /**
     * TC-DB-CONS-001: Order must have a non-null user relationship
     */
    @Test
    void testOrderRequiresUser() {
        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setCreatedAt(LocalDateTime.now(clock));
        order.setUser(null); // Violates nullable = false constraint

        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.saveAndFlush(order);
        }, "Order should fail to save without a user");
    }

    /**
     * TC-DB-CONS-002: Order must have a unique order number
     */
    @Test
    void testOrderNumberMustBeUnique() {
        // Create first order with user
        User user = new User();
        user.setEmail("test-" + System.currentTimeMillis() + "@example.com");
        user.setUsername("testuser-" + System.currentTimeMillis());
        user.setPassword("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now(clock));
        user = userRepository.saveAndFlush(user);

        String orderNumber = "ORD-UNIQUE-" + System.currentTimeMillis();

        Order order1 = new Order();
        order1.setUser(user);
        order1.setOrderNumber(orderNumber);
        order1.setTotalAmount(BigDecimal.valueOf(100.00));
        order1.setCreatedAt(LocalDateTime.now(clock));
        orderRepository.saveAndFlush(order1);

        // Attempt to create second order with same order number
        Order order2 = new Order();
        order2.setUser(user);
        order2.setOrderNumber(orderNumber); // Duplicate
        order2.setTotalAmount(BigDecimal.valueOf(200.00));
        order2.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.saveAndFlush(order2);
        }, "Order should fail to save with duplicate order number");
    }

    /**
     * TC-DB-CONS-003: Order must have a non-null total amount
     */
    @Test
    void testOrderRequiresTotalAmount() {
        User user = new User();
        user.setEmail("test-" + System.currentTimeMillis() + "@example.com");
        user.setUsername("testuser-" + System.currentTimeMillis());
        user.setPassword("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now(clock));
        user = userRepository.saveAndFlush(user);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(null); // Violates nullable = false
        order.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.saveAndFlush(order);
        }, "Order should fail to save without total amount");
    }

    /**
     * TC-DB-CONS-004: Payment must have a non-null order ID
     */
    @Test
    void testPaymentRequiresOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(null); // Violates nullable = false
        payment.setAmount(100.00);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(payment);
        }, "Payment should fail to save without order ID");
    }

    /**
     * TC-DB-CONS-005: Payment must have a non-null amount
     */
    @Test
    void testPaymentRequiresAmount() {
        Payment payment = new Payment();
        payment.setOrderId(12345L);
        payment.setAmount(null); // Violates nullable = false
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(payment);
        }, "Payment should fail to save without amount");
    }

    /**
     * TC-DB-CONS-006: Payment status must respect length constraint
     */
    @Test
    void testPaymentStatusLengthConstraint() {
        Payment payment = new Payment();
        payment.setOrderId(12345L);
        payment.setAmount(100.00);
        payment.setStatus("A".repeat(51)); // Exceeds 50 character limit
        payment.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(payment);
        }, "Payment should fail to save with status exceeding 50 characters");
    }
}
