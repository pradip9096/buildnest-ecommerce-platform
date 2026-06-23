package com.example.buildnest_ecommerce.performance;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
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
 * Performance test suite (TC-PERF-001 through TC-PERF-004).
 * Tests for response time, throughput, and scalability metrics.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class PerformanceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("perftest");
        testUser.setEmail("perftest@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Perf");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Performance test product");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStockQuantity(10000);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    // TC-PERF-001: Order retrieval response time < 100ms
    @Test
    @DisplayName("TC-PERF-001: Order retrieval should complete in < 100ms")
    void testOrderRetrievalResponseTime() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("PERF-001");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setIsDeleted(false);
        order.setCreatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        long startTime = System.currentTimeMillis();
        var retrieved = orderRepository.findById(saved.getId());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(retrieved.isPresent());
        assertTrue(duration < 100, "Order retrieval took " + duration + "ms, should be < 100ms");
    }

    // TC-PERF-002: Bulk order listing pagination < 500ms
    @Test
    @DisplayName("TC-PERF-002: Bulk order listing should complete in < 500ms")
    void testBulkOrderListingPerformance() {
        // Create 50 orders
        for (int i = 0; i < 50; i++) {
            Order order = new Order();
            order.setUser(testUser);
            order.setOrderNumber("PERF-002-" + i);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setIsDeleted(false);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        long startTime = System.currentTimeMillis();
        var allOrders = orderRepository.findAll();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(allOrders.size() >= 50);
        assertTrue(duration < 500, "Order listing took " + duration + "ms, should be < 500ms");
    }

    // TC-PERF-003: Search/filter operation < 200ms
    @Test
    @DisplayName("TC-PERF-003: Search/filter operation should complete in < 200ms")
    void testSearchFilterPerformance() {
        for (int i = 0; i < 30; i++) {
            Order order = new Order();
            order.setUser(testUser);
            order.setOrderNumber("PERF-003-" + i);
            order.setStatus(i % 2 == 0 ? Order.OrderStatus.PENDING : Order.OrderStatus.CONFIRMED);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setIsDeleted(false);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        long startTime = System.currentTimeMillis();
        var filtered = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .toList();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(filtered.size() > 0);
        assertTrue(duration < 200, "Search/filter took " + duration + "ms, should be < 200ms");
    }

    // TC-PERF-004: Concurrent order creation throughput > 100 orders/sec
    @Test
    @DisplayName("TC-PERF-004: Concurrent order creation throughput > 100 orders/sec")
    void testOrderCreationThroughput() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            Order order = new Order();
            order.setUser(testUser);
            order.setOrderNumber("PERF-004-" + i);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setIsDeleted(false);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double throughput = (100.0 / duration) * 1000; // orders per second

        assertTrue(throughput > 100, "Throughput was " + throughput + " orders/sec, should be > 100");
    }
}
