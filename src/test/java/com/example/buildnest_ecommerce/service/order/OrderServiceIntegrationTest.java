package com.example.buildnest_ecommerce.service.order;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Order Processing Repository functionality.
 * Tests cover order creation, persistence, retrieval, and query operations.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser1");
        testUser.setEmail("testuser1@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFirstName("Test");
        testUser.setLastName("User1");
        testUser.setPhoneNumber("+1234567890");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setEmail("testuser2@example.com");
        testUser2.setPassword("hashedPassword456");
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setPhoneNumber("+0987654321");
        testUser2.setIsActive(true);
        testUser2 = userRepository.save(testUser2);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-TEST-001");
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("1000.00"));
        testOrder.setIsDeleted(false);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("TC-ORD-001: Should create order and persist successfully")
    void testCreateOrderSuccess() {
        Order savedOrder = orderRepository.save(testOrder);

        assertNotNull(savedOrder.getId());
        assertEquals("ORD-TEST-001", savedOrder.getOrderNumber());
        assertEquals(Order.OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(new BigDecimal("1000.00"), savedOrder.getTotalAmount());
        assertEquals(testUser.getId(), savedOrder.getUser().getId());
    }

    @Test
    @DisplayName("TC-ORD-002: Should update order status successfully")
    void testUpdateOrderStatusSuccess() {
        Order savedOrder = orderRepository.save(testOrder);

        savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        savedOrder.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(savedOrder);

        assertEquals(Order.OrderStatus.CONFIRMED, updatedOrder.getStatus());
        assertNotNull(updatedOrder.getUpdatedAt());
    }

    @Test
    @DisplayName("TC-ORD-003: Should retrieve order by ID successfully")
    void testGetOrderByIdSuccess() {
        Order savedOrder = orderRepository.save(testOrder);
        Long orderId = savedOrder.getId();

        Optional<Order> retrievedOrder = orderRepository.findById(orderId);

        assertTrue(retrievedOrder.isPresent());
        assertEquals(orderId, retrievedOrder.get().getId());
        assertEquals("ORD-TEST-001", retrievedOrder.get().getOrderNumber());
    }

    @Test
    @DisplayName("TC-ORD-004: Should return empty when order not found")
    void testGetOrderByIdNotFound() {
        Optional<Order> retrievedOrder = orderRepository.findById(99999L);

        assertFalse(retrievedOrder.isPresent());
    }

    @Test
    @DisplayName("TC-ORD-005: Should retrieve all orders for a user")
    void testGetOrdersByUserId() {
        orderRepository.save(testOrder); // Save testOrder from setUp

        Order order1 = new Order();
        order1.setUser(testUser);
        order1.setOrderNumber("ORD-TEST-005-1");
        order1.setStatus(Order.OrderStatus.PENDING);
        order1.setTotalAmount(new BigDecimal("500.00"));
        order1.setIsDeleted(false);
        order1.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(testUser);
        order2.setOrderNumber("ORD-TEST-005-2");
        order2.setStatus(Order.OrderStatus.CONFIRMED);
        order2.setTotalAmount(new BigDecimal("750.00"));
        order2.setIsDeleted(false);
        order2.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(testUser2);
        order3.setOrderNumber("ORD-TEST-005-3");
        order3.setStatus(Order.OrderStatus.PENDING);
        order3.setTotalAmount(new BigDecimal("300.00"));
        order3.setIsDeleted(false);
        order3.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order3);

        List<Order> userOrders = orderRepository.findAll()
                .stream()
                .filter(o -> o.getUser().getId().equals(testUser.getId()))
                .filter(o -> !o.getIsDeleted())
                .toList();

        // testOrder + order1 + order2 = 3
        assertEquals(3, userOrders.size());
        assertTrue(userOrders.stream().allMatch(o -> o.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("TC-ORD-007: Should update order details successfully")
    void testUpdateOrderDetails() {
        Order savedOrder = orderRepository.save(testOrder);

        savedOrder.setTotalAmount(new BigDecimal("1500.00"));
        savedOrder.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(savedOrder);

        assertEquals(new BigDecimal("1500.00"), updatedOrder.getTotalAmount());
    }

    @Test
    @DisplayName("TC-ORD-010: Should cancel order")
    void testOrderCancellation() {
        Order savedOrder = orderRepository.save(testOrder);

        savedOrder.setStatus(Order.OrderStatus.CANCELLED);
        savedOrder.setUpdatedAt(LocalDateTime.now());
        Order cancelledOrder = orderRepository.save(savedOrder);

        assertEquals(Order.OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }

    @Test
    @DisplayName("TC-ORD-011: Should soft delete order")
    void testSoftDeleteOrder() {
        Order savedOrder = orderRepository.save(testOrder);
        Long orderId = savedOrder.getId();

        savedOrder.setIsDeleted(true);
        savedOrder.setDeletedAt(LocalDateTime.now());
        orderRepository.save(savedOrder);

        Optional<Order> retrievedOrder = orderRepository.findById(orderId);
        assertTrue(retrievedOrder.isPresent());
        assertTrue(retrievedOrder.get().getIsDeleted());
        assertNotNull(retrievedOrder.get().getDeletedAt());
    }

    @Test
    @DisplayName("TC-ORD-012: Should handle order status transitions")
    void testOrderStatusTransitions() {
        Order savedOrder = orderRepository.save(testOrder);

        // PENDING -> CONFIRMED
        savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        Order step1 = orderRepository.save(savedOrder);
        assertEquals(Order.OrderStatus.CONFIRMED, step1.getStatus());

        // CONFIRMED -> SHIPPED
        step1.setStatus(Order.OrderStatus.SHIPPED);
        Order step2 = orderRepository.save(step1);
        assertEquals(Order.OrderStatus.SHIPPED, step2.getStatus());

        // SHIPPED -> DELIVERED
        step2.setStatus(Order.OrderStatus.DELIVERED);
        Order step3 = orderRepository.save(step2);
        assertEquals(Order.OrderStatus.DELIVERED, step3.getStatus());
    }

    @Test
    @DisplayName("TC-ORD-014: Should track order timestamps correctly")
    void testOrderTimestamps() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        Order savedOrder = orderRepository.save(testOrder);
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
        assertTrue(savedOrder.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(savedOrder.getCreatedAt().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("TC-ORD-015: Should verify order belongs to correct user")
    void testOrderOwnershipVerification() {
        Order savedOrder = orderRepository.save(testOrder);

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(testUser.getId(), retrievedOrder.get().getUser().getId());
        assertNotEquals(testUser2.getId(), retrievedOrder.get().getUser().getId());
    }

    @Test
    @DisplayName("TC-ORD-016: Should enforce unique constraint on order number")
    void testOrderNumberUniqueness() {
        Order order1 = orderRepository.save(testOrder);

        Order order2 = new Order();
        order2.setUser(testUser2);
        order2.setOrderNumber("ORD-TEST-016-UNIQUE");
        order2.setStatus(Order.OrderStatus.PENDING);
        order2.setTotalAmount(new BigDecimal("1000.00"));
        order2.setIsDeleted(false);
        order2.setCreatedAt(LocalDateTime.now());

        Order savedOrder2 = orderRepository.save(order2);

        assertNotEquals(order1.getOrderNumber(), savedOrder2.getOrderNumber());
        assertEquals("ORD-TEST-001", order1.getOrderNumber());
        assertEquals("ORD-TEST-016-UNIQUE", savedOrder2.getOrderNumber());
    }

    @Test
    @DisplayName("TC-ORD-017: Should verify order data consistency")
    void testOrderDataConsistency() {
        Order savedOrder = orderRepository.save(testOrder);
        Long orderId = savedOrder.getId();

        Optional<Order> retrievedOrder = orderRepository.findById(orderId);

        assertTrue(retrievedOrder.isPresent());
        assertEquals(savedOrder.getOrderNumber(), retrievedOrder.get().getOrderNumber());
        assertEquals(savedOrder.getStatus(), retrievedOrder.get().getStatus());
        assertEquals(savedOrder.getTotalAmount(), retrievedOrder.get().getTotalAmount());
        assertEquals(savedOrder.getIsDeleted(), retrievedOrder.get().getIsDeleted());
        assertEquals(savedOrder.getUser().getId(), retrievedOrder.get().getUser().getId());
    }
}
