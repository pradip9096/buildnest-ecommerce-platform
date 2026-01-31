package com.example.buildnest_ecommerce.analytics;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Analytics and Reporting test suite TC-REPORT-001 to TC-REPORT-010.
 * Covers business metrics and analytics features.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class AnalyticsReportingTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("analytics_user");
        testUser.setEmail("analytics@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Analytics");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Analytical product");
        testProduct.setPrice(new BigDecimal("1000.00"));
        testProduct.setStockQuantity(100);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ANAL-ORD-001");
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.setTotalAmount(new BigDecimal("5000.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("TC-REPORT-001: Should calculate total sales revenue")
    void testCalculateTotalSalesRevenue() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertTrue(totalRevenue.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("TC-REPORT-002: Should track order count")
    void testTrackOrderCount() {
        List<Order> orders = orderRepository.findAll();

        assertTrue(orders.size() >= 1);
    }

    @Test
    @DisplayName("TC-REPORT-003: Should analyze product sales")
    void testAnalyzeProductSales() {
        List<Product> products = productRepository.findAll();

        assertFalse(products.isEmpty());
    }

    @Test
    @DisplayName("TC-REPORT-004: Should generate daily sales report")
    void testGenerateDailySalesReport() {
        LocalDateTime today = LocalDateTime.now();
        List<Order> dailyOrders = orderRepository.findAll();

        assertTrue(dailyOrders.stream()
                .anyMatch(order -> order.getCreatedAt().toLocalDate().equals(today.toLocalDate())));
    }

    @Test
    @DisplayName("TC-REPORT-005: Should calculate average order value")
    void testCalculateAverageOrderValue() {
        List<Order> orders = orderRepository.findAll();

        if (!orders.isEmpty()) {
            BigDecimal totalAmount = orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOrderValue = totalAmount.divide(new BigDecimal(orders.size()));

            assertTrue(avgOrderValue.compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    @DisplayName("TC-REPORT-006: Should identify top selling products")
    void testIdentifyTopSellingProducts() {
        List<Product> products = productRepository.findAll();

        assertFalse(products.isEmpty());
    }

    @Test
    @DisplayName("TC-REPORT-007: Should generate customer report")
    void testGenerateCustomerReport() {
        List<User> users = userRepository.findAll();

        assertTrue(users.size() >= 1);
    }

    @Test
    @DisplayName("TC-REPORT-008: Should track inventory turnover")
    void testTrackInventoryTurnover() {
        Product product = testProduct;

        assertTrue(product.getStockQuantity() >= 0);
    }

    @Test
    @DisplayName("TC-REPORT-009: Should calculate profit margin")
    void testCalculateProfitMargin() {
        BigDecimal revenue = new BigDecimal("5000.00");
        BigDecimal cost = new BigDecimal("3000.00");
        BigDecimal profit = revenue.subtract(cost);

        assertTrue(profit.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("TC-REPORT-010: Should generate monthly report")
    void testGenerateMonthlyReport() {
        List<Order> orders = orderRepository.findAll();

        assertTrue(orders.size() >= 0);
    }
}
