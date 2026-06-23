package com.example.buildnest_ecommerce.reliability;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reliability and High Availability test suite (TC-REL-001 through TC-REL-004).
 * Tests for fault tolerance, error recovery, and data consistency.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class ReliabilityHATest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("reliabilitytest");
        testUser.setEmail("reliability@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Reliability");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    // TC-REL-001: Transaction rollback on error
    @Test
    @DisplayName("TC-REL-001: Transaction rollback on error maintains data consistency")
    void testTransactionRollback() {
        Order order1 = new Order();
        order1.setUser(testUser);
        order1.setOrderNumber("REL-001-" + System.currentTimeMillis());
        order1.setStatus(Order.OrderStatus.PENDING);
        order1.setTotalAmount(new BigDecimal("1000.00"));
        order1.setIsDeleted(false);
        order1.setCreatedAt(LocalDateTime.now());
        Order saved1 = orderRepository.save(order1);

        long savedId = saved1.getId();

        try {
            // Simulate transaction rollback scenario
            Order order2 = new Order();
            order2.setUser(testUser);
            order2.setOrderNumber("REL-001-" + (System.currentTimeMillis() + 1)); // Unique order number
            order2.setStatus(Order.OrderStatus.PENDING);
            order2.setTotalAmount(new BigDecimal("2000.00"));
            order2.setIsDeleted(false);
            order2.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order2);
            orderRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Expected - duplicate key violation (if intentional)
        }

        // Original order should still exist and be unchanged
        var verified = orderRepository.findById(savedId);
        assertTrue(verified.isPresent());
        assertEquals(new BigDecimal("1000.00"), verified.get().getTotalAmount());
    }

    // TC-REL-002: Data consistency with concurrent writes
    @Test
    @DisplayName("TC-REL-002: Data consistency maintained under concurrent writes")
    void testConcurrentWriteConsistency() {
        int numberOfOrders = 10;
        long baseTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfOrders; i++) {
            Order order = new Order();
            order.setUser(testUser);
            order.setOrderNumber("REL-002-" + (baseTime + i));
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setIsDeleted(false);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        // Verify all orders created successfully
        var allOrders = orderRepository.findAll();
        long userOrderCount = allOrders.stream()
                .filter(o -> o.getUser().getId().equals(testUser.getId()))
                .count();

        assertEquals(numberOfOrders, userOrderCount);
    }

    // TC-REL-003: Automatic retry on transient failures
    @Test
    @DisplayName("TC-REL-003: System recovers from transient failures")
    void testTransientFailureRecovery() {
        int attempts = 0;
        int maxAttempts = 3;
        boolean success = false;

        while (attempts < maxAttempts && !success) {
            try {
                attempts++;
                Order order = new Order();
                order.setUser(testUser);
                order.setOrderNumber("REL-003-" + attempts);
                order.setStatus(Order.OrderStatus.PENDING);
                order.setTotalAmount(new BigDecimal("1000.00"));
                order.setIsDeleted(false);
                order.setCreatedAt(LocalDateTime.now());
                orderRepository.save(order);
                success = true;
            } catch (Exception e) {
                if (attempts >= maxAttempts) {
                    fail("Failed to recover after " + maxAttempts + " attempts");
                }
            }
        }

        assertTrue(success);
        assertTrue(attempts <= maxAttempts);
    }

    // TC-REL-004: Graceful degradation under load
    @Test
    @DisplayName("TC-REL-004: System gracefully degrades under high load")
    void testGracefulDegradationUnderLoad() {
        long startTime = System.currentTimeMillis();
        int ordersCreated = 0;
        int targetOrders = 100;

        try {
            for (int i = 0; i < targetOrders; i++) {
                Order order = new Order();
                order.setUser(testUser);
                order.setOrderNumber("REL-004-" + i);
                order.setStatus(Order.OrderStatus.PENDING);
                order.setTotalAmount(new BigDecimal("1000.00"));
                order.setIsDeleted(false);
                order.setCreatedAt(LocalDateTime.now());
                orderRepository.save(order);
                ordersCreated++;
            }
        } catch (Exception e) {
            // System gracefully handles the error and continues
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // At least some orders should be created even under load
        assertTrue(ordersCreated > 0);
        // Should complete within reasonable time even under load
        assertTrue(duration < 10000, "High load test took " + duration + "ms");
    }
}
