package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sales Dashboard DTO
 * Comprehensive sales analytics data transfer object
 * Implements Section 6.2 - Business Intelligence Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDashboardDTO {

    // Revenue Metrics
    private BigDecimal dailyRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;

    // Order Metrics
    private Long dailyOrders;
    private Long weeklyOrders;
    private Long monthlyOrders;
    private Long totalOrders;

    // Customer Metrics
    private Double averageOrderValue;
    private Long totalCustomers;
    private Long newCustomersThisMonth;
    private Double customerRetentionRate;

    // Product Metrics
    private List<TopProductDTO> topSellingProducts;
    private Map<String, BigDecimal> revenueByCategory;

    // Conversion Metrics
    private Double cartAbandonmentRate;
    private Double conversionRate;

    // Trend Data
    private List<RevenueTrendPoint> revenueTrend;

    // Time Period
    private LocalDate startDate;
    private LocalDate endDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private Long unitsSold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTrendPoint {
        private LocalDate date;
        private BigDecimal revenue;
        private Long orderCount;
    }
}
