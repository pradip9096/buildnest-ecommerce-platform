package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository layer integration tests for Order entity.
 * Tests JPA query methods and data persistence.
 */
@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("repotest-" + System.currentTimeMillis());
        testUser.setEmail("repotest-" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Repo");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-REPO-TEST-" + System.currentTimeMillis());
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("2000.00"));
        testOrder.setIsDeleted(false);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should persist and retrieve order by ID")
    void testSaveAndFindById() {
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());

        assertTrue(retrievedOrder.isPresent());
        assertEquals(savedOrder.getId(), retrievedOrder.get().getId());
        // Order number is now dynamically generated with timestamp
        assertTrue(retrievedOrder.get().getOrderNumber().startsWith("ORD-REPO-TEST-"));
        assertEquals(Order.OrderStatus.PENDING, retrievedOrder.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty optional when order not found")
    void testFindByIdNotFound() {
        Optional<Order> retrievedOrder = orderRepository.findById(99999L);

        assertFalse(retrievedOrder.isPresent());
    }

    @Test
    @DisplayName("Should update order successfully")
    void testUpdateOrder() {
        Order savedOrder = orderRepository.save(testOrder);
        savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        savedOrder.setTotalAmount(new BigDecimal("2500.00"));
        savedOrder.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(savedOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(updatedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(Order.OrderStatus.CONFIRMED, retrievedOrder.get().getStatus());
        assertEquals(new BigDecimal("2500.00"), retrievedOrder.get().getTotalAmount());
    }

    @Test
    @DisplayName("Should delete order (hard delete)")
    void testDeleteOrder() {
        Order savedOrder = orderRepository.save(testOrder);
        Long orderId = savedOrder.getId();
        entityManager.flush();

        orderRepository.deleteById(orderId);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(orderId);
        assertFalse(retrievedOrder.isPresent());
    }

    @Test
    @DisplayName("Should retrieve all orders")
    void testFindAll() {
        Order order1 = new Order();
        order1.setUser(testUser);
        order1.setOrderNumber("ORD-REPO-TEST-002");
        order1.setStatus(Order.OrderStatus.CONFIRMED);
        order1.setTotalAmount(new BigDecimal("1500.00"));
        order1.setIsDeleted(false);
        order1.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(testUser);
        order2.setOrderNumber("ORD-REPO-TEST-003");
        order2.setStatus(Order.OrderStatus.SHIPPED);
        order2.setTotalAmount(new BigDecimal("3000.00"));
        order2.setIsDeleted(false);
        order2.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order2);

        orderRepository.save(testOrder);
        entityManager.flush();

        List<Order> allOrders = orderRepository.findAll();

        assertTrue(allOrders.size() >= 3);
    }

    @Test
    @DisplayName("Should handle soft delete flag")
    void testSoftDeleteFlag() {
        Order savedOrder = orderRepository.save(testOrder);
        savedOrder.setIsDeleted(true);
        savedOrder.setDeletedAt(LocalDateTime.now());

        Order softDeletedOrder = orderRepository.save(savedOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(softDeletedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertTrue(retrievedOrder.get().getIsDeleted());
        assertNotNull(retrievedOrder.get().getDeletedAt());
    }

    @Test
    @DisplayName("Should preserve order number uniqueness constraint")
    void testOrderNumberUniqueness() {
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        assertNotNull(savedOrder.getId());

        Order duplicateOrder = new Order();
        duplicateOrder.setUser(testUser);
        // Use the same order number as testOrder to create a duplicate
        duplicateOrder.setOrderNumber(testOrder.getOrderNumber());
        duplicateOrder.setStatus(Order.OrderStatus.PENDING);
        duplicateOrder.setTotalAmount(new BigDecimal("1000.00"));
        duplicateOrder.setIsDeleted(false);
        duplicateOrder.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            orderRepository.save(duplicateOrder);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should maintain relationship with User entity")
    void testOrderUserRelationship() {
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertNotNull(retrievedOrder.get().getUser());
        assertEquals(testUser.getId(), retrievedOrder.get().getUser().getId());
        // Username is now dynamically generated with timestamp
        assertTrue(retrievedOrder.get().getUser().getUsername().startsWith("repotest-"));
    }

    @Test
    @DisplayName("Should persist timestamps correctly")
    void testOrderTimestamps() {
        LocalDateTime beforeSave = LocalDateTime.now();
        Order savedOrder = orderRepository.save(testOrder);
        LocalDateTime afterSave = LocalDateTime.now();
        entityManager.flush();

        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
        assertTrue(savedOrder.getCreatedAt().isAfter(beforeSave.minusSeconds(1)));
        assertTrue(savedOrder.getCreatedAt().isBefore(afterSave.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should handle order status enum correctly")
    void testOrderStatusEnum() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("ORD-REPO-TEST-STATUS");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setIsDeleted(false);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(Order.OrderStatus.PENDING, retrievedOrder.get().getStatus());
    }
}
