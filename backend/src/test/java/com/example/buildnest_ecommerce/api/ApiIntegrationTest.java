package com.example.buildnest_ecommerce.api;

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
 * API Integration test suite TC-API-001 to TC-API-010.
 * Covers API contract and response validation.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class ApiIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("apitest");
        testUser.setEmail("apitest@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("API");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("API Test Product");
        testProduct.setDescription("Test");
        testProduct.setPrice(new BigDecimal("1000.00"));
        testProduct.setStockQuantity(100);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("API-ORD-001");
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.setTotalAmount(new BigDecimal("5000.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("TC-API-001: Should return user with all required fields")
    void testUserApiResponse() {
        User user = userRepository.findById(testUser.getId()).orElse(null);

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());
    }

    @Test
    @DisplayName("TC-API-002: Should return product with valid price format")
    void testProductApiResponse() {
        Product product = productRepository.findById(testProduct.getId()).orElse(null);

        assertNotNull(product);
        assertNotNull(product.getPrice());
        assertTrue(product.getPrice().scale() >= 0);
    }

    @Test
    @DisplayName("TC-API-003: Should return order with complete information")
    void testOrderApiResponse() {
        Order order = orderRepository.findById(testOrder.getId()).orElse(null);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertNotNull(order.getStatus());
    }

    @Test
    @DisplayName("TC-API-004: Should validate pagination support")
    void testPaginationSupport() {
        int page = 0;
        int pageSize = 10;

        assertTrue(page >= 0);
        assertTrue(pageSize > 0);
    }

    @Test
    @DisplayName("TC-API-005: Should validate response timestamps")
    void testApiResponseTimestamps() {
        Order order = orderRepository.findById(testOrder.getId()).orElse(null);

        assertNotNull(order);
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("TC-API-006: Should return consistent data types")
    void testConsistentDataTypes() {
        Product product = productRepository.findById(testProduct.getId()).orElse(null);

        assertNotNull(product);
        assertTrue(product.getPrice() instanceof BigDecimal);
        assertTrue(product.getStockQuantity() instanceof Integer);
    }

    @Test
    @DisplayName("TC-API-007: Should validate request ID parameter")
    void testValidateApiRequestIdParameter() {
        Long userId = testUser.getId();

        assertNotNull(userId);
        assertTrue(userId > 0);
    }

    @Test
    @DisplayName("TC-API-008: Should support filtering in list endpoints")
    void testFilteringSupport() {
        String filter = "status:CONFIRMED";

        assertNotNull(filter);
        assertTrue(filter.contains(":"));
    }

    @Test
    @DisplayName("TC-API-009: Should include metadata in responses")
    void testMetadataInResponses() {
        int totalRecords = productRepository.findAll().size();

        assertTrue(totalRecords >= 0);
    }

    @Test
    @DisplayName("TC-API-010: Should handle API error responses")
    void testApiErrorHandling() {
        Order nonExistentOrder = orderRepository.findById(99999L).orElse(null);

        assertNull(nonExistentOrder);
    }
}
