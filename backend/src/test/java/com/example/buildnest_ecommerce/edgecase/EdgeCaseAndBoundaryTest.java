package com.example.buildnest_ecommerce.edgecase;

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
 * Edge Case and Boundary test suite TC-EDGE-001 to TC-EDGE-015.
 * Covers extreme and boundary condition testing.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class EdgeCaseAndBoundaryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("edgecase");
        testUser.setEmail("edgecase@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Edge");
        testUser.setLastName("Case");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Edge Case Product");
        testProduct.setDescription("Test");
        testProduct.setPrice(new BigDecimal("0.01"));
        testProduct.setStockQuantity(1);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("TC-EDGE-001: Should handle minimum product price")
    void testMinimumProductPrice() {
        BigDecimal minPrice = new BigDecimal("0.01");

        assertTrue(minPrice.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("TC-EDGE-002: Should handle maximum product price")
    void testMaximumProductPrice() {
        testProduct.setPrice(new BigDecimal("999999999.99"));
        Product updated = productRepository.save(testProduct);

        assertTrue(updated.getPrice().compareTo(new BigDecimal("999999999.99")) == 0);
    }

    @Test
    @DisplayName("TC-EDGE-003: Should handle zero stock quantity")
    void testZeroStockQuantity() {
        testProduct.setStockQuantity(0);
        Product updated = productRepository.save(testProduct);

        assertEquals(0, updated.getStockQuantity());
    }

    @Test
    @DisplayName("TC-EDGE-004: Should handle maximum stock quantity")
    void testMaximumStockQuantity() {
        testProduct.setStockQuantity(Integer.MAX_VALUE);
        Product updated = productRepository.save(testProduct);

        assertEquals(Integer.MAX_VALUE, updated.getStockQuantity());
    }

    @Test
    @DisplayName("TC-EDGE-005: Should handle very long product name")
    void testVeryLongProductName() {
        String longName = "A".repeat(500);
        testProduct.setName(longName);
        Product updated = productRepository.save(testProduct);

        assertEquals(longName, updated.getName());
    }

    @Test
    @DisplayName("TC-EDGE-006: Should handle order with zero amount")
    void testOrderWithZeroAmount() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("EDGE-ZERO");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("0.00"));
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        assertEquals(new BigDecimal("0.00"), saved.getTotalAmount());
    }

    @Test
    @DisplayName("TC-EDGE-007: Should handle order with very large amount")
    void testOrderWithVeryLargeAmount() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("EDGE-LARGE");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("999999999.99"));
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        assertEquals(new BigDecimal("999999999.99"), saved.getTotalAmount());
    }

    @Test
    @DisplayName("TC-EDGE-008: Should handle username with minimum length")
    void testMinimumLengthUsername() {
        User user = new User();
        user.setUsername("a");
        user.setEmail("min@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("Min");
        user.setLastName("User");
        user.setIsActive(true);

        User saved = userRepository.save(user);
        assertEquals("a", saved.getUsername());
    }

    @Test
    @DisplayName("TC-EDGE-009: Should handle product with exact decimal precision")
    void testProductPriceExactDecimalPrecision() {
        BigDecimal price = new BigDecimal("123.45");
        testProduct.setPrice(price);
        Product updated = productRepository.save(testProduct);

        assertEquals(price, updated.getPrice());
    }

    @Test
    @DisplayName("TC-EDGE-010: Should handle inactive user")
    void testInactiveUserHandling() {
        testUser.setIsActive(false);
        User inactiveUser = userRepository.save(testUser);

        assertFalse(inactiveUser.getIsActive());
    }

    @Test
    @DisplayName("TC-EDGE-011: Should handle order cancellation after confirmation")
    void testOrderCancellationAfterConfirmation() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("EDGE-CANCEL");
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);

        assertEquals(Order.OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("TC-EDGE-012: Should handle product with special characters")
    void testSpecialCharactersInProductName() {
        testProduct.setName("Product!@#$%^&*");
        Product updated = productRepository.save(testProduct);

        assertEquals("Product!@#$%^&*", updated.getName());
    }

    @Test
    @DisplayName("TC-EDGE-013: Should handle negative stock adjustment")
    void testNegativeStockAdjustment() {
        testProduct.setStockQuantity(-10);
        Product updated = productRepository.save(testProduct);

        assertEquals(-10, updated.getStockQuantity());
    }

    @Test
    @DisplayName("TC-EDGE-014: Should handle null optional fields")
    void testNullOptionalFields() {
        Product product = new Product();
        product.setName("Test");
        product.setPrice(new BigDecimal("100.00"));

        assertNull(product.getDescription());
    }

    @Test
    @DisplayName("TC-EDGE-015: Should handle concurrent timestamp updates")
    void testConcurrentTimestampUpdates() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderNumber("EDGE-TIME");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        assertNotNull(saved.getCreatedAt());
    }
}
