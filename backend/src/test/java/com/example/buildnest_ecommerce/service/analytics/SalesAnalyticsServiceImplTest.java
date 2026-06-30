package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SalesAnalyticsServiceImpl integration tests")
class SalesAnalyticsServiceImplTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private SalesAnalyticsServiceImpl service;

    private User user;

    @BeforeEach
    void setUp() {
        service = new SalesAnalyticsServiceImpl(orderRepository, userRepository);

        user = new User();
        user.setUsername("analytics_user");
        user.setEmail("analytics@example.com");
        user.setPassword("hashed");
        user.setFirstName("Ana");
        user.setLastName("Lytic");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
    }

    private Order savedOrder(OrderStatus status, BigDecimal amount, LocalDateTime createdAt) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-" + System.nanoTime());
        order.setStatus(status);
        order.setTotalAmount(amount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setIsDeleted(false);
        order.setCreatedAt(createdAt);
        return orderRepository.save(order);
    }

    // --- getDailyRevenue ---

    @Test
    @DisplayName("getDailyRevenue sums only DELIVERED orders for the given date")
    void getDailyRevenue_sumsDeliveredOrdersOnly() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"), today);
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("50.00"), today);
        savedOrder(OrderStatus.PENDING, new BigDecimal("200.00"), today);  // excluded

        BigDecimal revenue = service.getDailyRevenue(LocalDate.now());

        assertEquals(0, new BigDecimal("150.00").compareTo(revenue));
    }

    @Test
    @DisplayName("getDailyRevenue returns zero when no orders exist")
    void getDailyRevenue_returnsZeroWhenNoOrders() {
        BigDecimal revenue = service.getDailyRevenue(LocalDate.now());
        assertEquals(0, BigDecimal.ZERO.compareTo(revenue));
    }

    @Test
    @DisplayName("getDailyRevenue excludes orders from other dates")
    void getDailyRevenue_excludesOtherDates() {
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("300.00"),
                LocalDate.now().minusDays(2).atStartOfDay());

        BigDecimal revenue = service.getDailyRevenue(LocalDate.now());
        assertEquals(0, BigDecimal.ZERO.compareTo(revenue));
    }

    // --- getAverageOrderValue ---

    @Test
    @DisplayName("getAverageOrderValue computes mean of DELIVERED orders in range")
    void getAverageOrderValue_computesMeanOfDeliveredOrders() {
        LocalDateTime recent = LocalDate.now().minusDays(1).atStartOfDay();
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"), recent);
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("200.00"), recent);
        savedOrder(OrderStatus.PENDING, new BigDecimal("999.00"), recent);  // excluded

        BigDecimal avg = service.getAverageOrderValue(
                LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(0, new BigDecimal("150.00").compareTo(avg));
    }

    @Test
    @DisplayName("getAverageOrderValue returns zero when no qualifying orders exist")
    void getAverageOrderValue_returnsZeroWhenNoOrders() {
        BigDecimal avg = service.getAverageOrderValue(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertEquals(0, BigDecimal.ZERO.compareTo(avg));
    }

    // --- getCustomerLifetimeValue ---

    @Test
    @DisplayName("getCustomerLifetimeValue sums only DELIVERED orders for the user")
    void getCustomerLifetimeValue_sumsDeliveredForUser() {
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("500.00"), LocalDateTime.now());
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("250.00"), LocalDateTime.now());
        savedOrder(OrderStatus.CANCELLED, new BigDecimal("100.00"), LocalDateTime.now()); // excluded

        BigDecimal ltv = service.getCustomerLifetimeValue(user.getId());

        assertEquals(0, new BigDecimal("750.00").compareTo(ltv));
    }

    @Test
    @DisplayName("getCustomerLifetimeValue returns zero for user with no delivered orders")
    void getCustomerLifetimeValue_returnsZeroWithNoDelivered() {
        savedOrder(OrderStatus.PENDING, new BigDecimal("100.00"), LocalDateTime.now());

        BigDecimal ltv = service.getCustomerLifetimeValue(user.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(ltv));
    }

    // --- getConversionRate ---

    @Test
    @DisplayName("getConversionRate returns 20 percent when orders exist")
    void getConversionRate_returns20PercentProxy() {
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"),
                LocalDate.now().minusDays(1).atStartOfDay());

        Double rate = service.getConversionRate(
                LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(20.0, rate);
    }

    @Test
    @DisplayName("getConversionRate returns zero when no orders exist")
    void getConversionRate_returnsZeroWhenNoOrders() {
        Double rate = service.getConversionRate(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertEquals(0.0, rate);
    }

    // --- getCartAbandonmentRate ---

    @Test
    @DisplayName("getCartAbandonmentRate returns zero when no orders exist")
    void getCartAbandonmentRate_returnsZeroWhenNoOrders() {
        Double rate = service.getCartAbandonmentRate(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertEquals(0.0, rate);
    }

    @Test
    @DisplayName("getCartAbandonmentRate returns ~66.67 percent with completed orders")
    void getCartAbandonmentRate_returnsProxyRateWithOrders() {
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"),
                LocalDate.now().minusDays(1).atStartOfDay());
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"),
                LocalDate.now().minusDays(1).atStartOfDay());

        Double rate = service.getCartAbandonmentRate(
                LocalDate.now().minusDays(7), LocalDate.now());

        assertTrue(rate > 66.6 && rate < 66.8,
                "Expected ~66.67% abandonment proxy, got: " + rate);
    }

    // --- getDashboard ---

    @Test
    @DisplayName("getDashboard populates totalOrders and totalCustomers from repository counts")
    void getDashboard_populatesTotals() {
        savedOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"), LocalDateTime.now());
        savedOrder(OrderStatus.PENDING, new BigDecimal("50.00"), LocalDateTime.now());

        SalesDashboardDTO dashboard = service.getDashboard(
                LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(dashboard);
        assertEquals(2L, dashboard.getTotalOrders());
        assertEquals(1L, dashboard.getTotalCustomers());
        assertNotNull(dashboard.getRevenueTrend());
        assertEquals(8, dashboard.getRevenueTrend().size());  // 7 days + today = 8 points
    }

    @Test
    @DisplayName("getDashboard revenue metrics are non-negative")
    void getDashboard_revenueMetricsAreNonNegative() {
        SalesDashboardDTO dashboard = service.getDashboard(
                LocalDate.now().minusDays(30), LocalDate.now());

        assertTrue(dashboard.getDailyRevenue().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(dashboard.getWeeklyRevenue().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(dashboard.getMonthlyRevenue().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(dashboard.getYearlyRevenue().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("getDashboard start and end dates are reflected in the response")
    void getDashboard_reflectsRequestedDateRange() {
        LocalDate start = LocalDate.now().minusDays(14);
        LocalDate end = LocalDate.now();

        SalesDashboardDTO dashboard = service.getDashboard(start, end);

        assertEquals(start, dashboard.getStartDate());
        assertEquals(end, dashboard.getEndDate());
    }
}
