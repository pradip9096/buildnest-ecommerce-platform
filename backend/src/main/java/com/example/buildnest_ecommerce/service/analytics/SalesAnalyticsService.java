package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;

import java.time.LocalDate;

/**
 * Sales Analytics Service Interface
 * Provides business intelligence and analytics data
 */
public interface SalesAnalyticsService {

    /**
     * Get comprehensive sales dashboard data
     */
    SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate);

    /**
     * Get daily revenue
     */
    Double getDailyRevenue(LocalDate date);

    /**
     * Get conversion rate (orders/visitors)
     */
    Double getConversionRate(LocalDate startDate, LocalDate endDate);

    /**
     * Get cart abandonment rate
     */
    Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate);

    /**
     * Get customer lifetime value
     */
    Double getCustomerLifetimeValue(Long userId);

    /**
     * Get average order value
     */
    Double getAverageOrderValue(LocalDate startDate, LocalDate endDate);
}
