package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesAnalyticsServiceImpl implements SalesAnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales dashboard for period {} to {}", startDate, endDate);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate tomorrow = today.plusDays(1);

        return SalesDashboardDTO.builder()
                .dailyRevenue(revenueForRange(today, tomorrow))
                .weeklyRevenue(revenueForRange(weekStart, tomorrow))
                .monthlyRevenue(revenueForRange(monthStart, tomorrow))
                .yearlyRevenue(revenueForRange(yearStart, tomorrow))
                .dailyOrders(orderRepository.countOrdersBetween(today.atStartOfDay(), tomorrow.atStartOfDay()))
                .weeklyOrders(orderRepository.countOrdersBetween(weekStart.atStartOfDay(), tomorrow.atStartOfDay()))
                .monthlyOrders(orderRepository.countOrdersBetween(monthStart.atStartOfDay(), tomorrow.atStartOfDay()))
                .totalOrders(orderRepository.count())
                .averageOrderValue(getAverageOrderValue(startDate, endDate))
                .totalCustomers(userRepository.count())
                .newCustomersThisMonth(userRepository.countNewUsersBetween(
                        monthStart.atStartOfDay(), tomorrow.atStartOfDay()))
                .customerRetentionRate(calculateRetentionRate())
                .topSellingProducts(getTopSellingProducts(startDate, endDate, 10))
                .revenueByCategory(getRevenueByCategory(startDate, endDate))
                .cartAbandonmentRate(getCartAbandonmentRate(startDate, endDate))
                .conversionRate(getConversionRate(startDate, endDate))
                .revenueTrend(getRevenueTrend(startDate, endDate))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public BigDecimal getDailyRevenue(LocalDate date) {
        return revenueForRange(date, date.plusDays(1));
    }

    @Override
    public Double getConversionRate(LocalDate startDate, LocalDate endDate) {
        // No visitor-tracking data exists; use order count as a proxy (20% assumed rate).
        long orders = orderRepository.countOrdersBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        if (orders == 0) {
            return 0.0;
        }
        long estimatedVisitors = orders * 5;
        return (double) orders / estimatedVisitors * 100;
    }

    @Override
    public Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate) {
        // Cart entity has no creation timestamp; use completed-order count as a proxy.
        // Assumes a 2:1 abandonment-to-completion ratio until cart-event tracking is added.
        long completedOrders = orderRepository.countOrdersBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        if (completedOrders == 0) {
            return 0.0;
        }
        long abandonedCarts = completedOrders * 2;
        long totalCarts = completedOrders + abandonedCarts;
        return (double) abandonedCarts / totalCarts * 100;
    }

    @Override
    public BigDecimal getCustomerLifetimeValue(Long userId) {
        return orderRepository.sumRevenueByUser(userId, OrderStatus.DELIVERED);
    }

    @Override
    public BigDecimal getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
        BigDecimal avg = orderRepository.avgOrderValueBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(),
                OrderStatus.DELIVERED);
        return avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    // --- private helpers ---

    private BigDecimal revenueForRange(LocalDate from, LocalDate toExclusive) {
        return orderRepository.sumRevenueBetween(
                from.atStartOfDay(), toExclusive.atStartOfDay(), OrderStatus.DELIVERED);
    }

    private Double calculateRetentionRate() {
        long total = orderRepository.countDistinctCustomersByStatus(OrderStatus.DELIVERED);
        if (total == 0) {
            return 0.0;
        }
        long returning = orderRepository.countReturningCustomers(OrderStatus.DELIVERED);
        return BigDecimal.valueOf(returning)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<SalesDashboardDTO.TopProductDTO> getTopSellingProducts(
            LocalDate startDate, LocalDate endDate, int limit) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = orderRepository.findTopSellingProducts(
                start, end, OrderStatus.DELIVERED, PageRequest.of(0, limit));

        List<SalesDashboardDTO.TopProductDTO> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(SalesDashboardDTO.TopProductDTO.builder()
                    .productId((Long) row[0])
                    .productName((String) row[1])
                    .unitsSold(((Number) row[2]).longValue())
                    .revenue((BigDecimal) row[3])
                    .build());
        }
        return result;
    }

    private Map<String, BigDecimal> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = orderRepository.sumRevenueGroupedByCategory(
                start, end, OrderStatus.DELIVERED);

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String categoryName = row[0] != null ? (String) row[0] : "Uncategorised";
            result.put(categoryName, (BigDecimal) row[1]);
        }
        return result;
    }

    private List<SalesDashboardDTO.RevenueTrendPoint> getRevenueTrend(
            LocalDate startDate, LocalDate endDate) {

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        List<SalesDashboardDTO.RevenueTrendPoint> trend = new ArrayList<>((int) days + 1);

        for (long i = 0; i <= days; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            BigDecimal revenue = orderRepository.sumRevenueBetween(dayStart, dayEnd, OrderStatus.DELIVERED);
            Long orders = orderRepository.countOrdersBetween(dayStart, dayEnd);

            trend.add(SalesDashboardDTO.RevenueTrendPoint.builder()
                    .date(date)
                    .revenue(revenue)
                    .orderCount(orders)
                    .build());
        }
        return trend;
    }
}
