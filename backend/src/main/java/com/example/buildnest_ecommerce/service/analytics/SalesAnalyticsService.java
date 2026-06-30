package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SalesAnalyticsService {

    SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate);

    BigDecimal getDailyRevenue(LocalDate date);

    /** Conversion rate as a percentage (0–100). Approximate: no visitor-tracking data available. */
    Double getConversionRate(LocalDate startDate, LocalDate endDate);

    /** Cart abandonment rate as a percentage (0–100). Approximate: no cart-event tracking data available. */
    Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate);

    BigDecimal getCustomerLifetimeValue(Long userId);

    BigDecimal getAverageOrderValue(LocalDate startDate, LocalDate endDate);
}
