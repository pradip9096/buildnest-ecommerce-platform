package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesAnalyticsServiceImpl tests")
class SalesAnalyticsServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SalesAnalyticsServiceImpl analyticsService;

    @Test
    @DisplayName("Should calculate dashboard metrics")
    void testGetDashboard() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("100.00"));
        delivered.setCreatedAt(LocalDateTime.now().minusDays(1));

        Order pending = new Order();
        pending.setStatus(Order.OrderStatus.PENDING);
        pending.setTotalAmount(new BigDecimal("50.00"));
        pending.setCreatedAt(LocalDateTime.now());

        User user = new User();
        user.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(orderRepository.findAll()).thenReturn(List.of(delivered, pending));
        when(orderRepository.count()).thenReturn(2L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.count()).thenReturn(1L);

        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        SalesDashboardDTO dashboard = analyticsService.getDashboard(start, end);

        assertNotNull(dashboard);
        assertEquals(2L, dashboard.getTotalOrders());
        assertEquals(1L, dashboard.getTotalCustomers());
        assertTrue(dashboard.getDailyRevenue().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Should calculate conversion rate")
    void testConversionRate() {
        when(orderRepository.findAll()).thenReturn(List.of());

        Double rate = analyticsService.getConversionRate(LocalDate.now().minusDays(3), LocalDate.now());
        assertEquals(0.0, rate);
    }

    @Test
    @DisplayName("Should calculate conversion rate with orders")
    void testConversionRateWithOrders() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setCreatedAt(LocalDateTime.now().minusDays(1));

        Order delivered2 = new Order();
        delivered2.setStatus(Order.OrderStatus.DELIVERED);
        delivered2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(orderRepository.findAll()).thenReturn(List.of(delivered, delivered2));

        Double rate = analyticsService.getConversionRate(LocalDate.now().minusDays(3), LocalDate.now());
        assertEquals(20.0, rate);
    }

    @Test
    @DisplayName("Should calculate cart abandonment rate with zero orders")
    void testCartAbandonmentRateNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        Double rate = analyticsService.getCartAbandonmentRate(LocalDate.now().minusDays(3), LocalDate.now());
        assertEquals(0.0, rate);
    }

    @Test
    @DisplayName("Should calculate cart abandonment rate with orders")
    void testCartAbandonmentRateWithOrders() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setCreatedAt(LocalDateTime.now().minusDays(1));

        Order delivered2 = new Order();
        delivered2.setStatus(Order.OrderStatus.DELIVERED);
        delivered2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(orderRepository.findAll()).thenReturn(List.of(delivered, delivered2));

        Double rate = analyticsService.getCartAbandonmentRate(LocalDate.now().minusDays(3), LocalDate.now());
        assertTrue(rate > 66.6 && rate < 66.7);
    }

    @Test
    @DisplayName("Should calculate customer lifetime value")
    void testCustomerLifetimeValue() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("100.00"));

        Order pending = new Order();
        pending.setStatus(Order.OrderStatus.PENDING);
        pending.setTotalAmount(new BigDecimal("50.00"));

        when(orderRepository.findByUserId(1L)).thenReturn(List.of(delivered, pending));

        Double value = analyticsService.getCustomerLifetimeValue(1L);
        assertEquals(100.00, value);
    }

    @Test
    @DisplayName("Should calculate average order value")
    void testAverageOrderValue() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("120.00"));
        delivered.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findAll()).thenReturn(List.of(delivered));

        Double avg = analyticsService.getAverageOrderValue(LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(120.00, avg);
    }

    @Test
    @DisplayName("Should return zero average order value when no delivered orders")
    void testAverageOrderValueNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        Double avg = analyticsService.getAverageOrderValue(LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(0.0, avg);
    }

    @Test
    @DisplayName("Should calculate daily revenue")
    void testGetDailyRevenue() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("75.00"));
        delivered.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findAll()).thenReturn(List.of(delivered));

        Double revenue = analyticsService.getDailyRevenue(LocalDate.now());
        assertEquals(75.00, revenue);
    }

    @Test
    @DisplayName("Should build revenue trend for date range")
    void testGetRevenueTrend() {
        Order delivered = new Order();
        delivered.setStatus(Order.OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("50.00"));
        delivered.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(orderRepository.findAll()).thenReturn(List.of(delivered));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();
        SalesDashboardDTO dashboard = analyticsService.getDashboard(start, end);

        assertNotNull(dashboard.getRevenueTrend());
        assertEquals(2, dashboard.getRevenueTrend().size());
    }
}
