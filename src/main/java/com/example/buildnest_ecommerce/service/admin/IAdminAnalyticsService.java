package com.example.buildnest_ecommerce.service.admin;

import java.util.Map;

/**
 * Interface for Admin Analytics Service operations.
 * Defines contract for admin dashboard metrics and API error analysis.
 */
public interface IAdminAnalyticsService {

    /**
     * Retrieve API errors filtered by HTTP status code.
     * 
     * @param startDateStr Start date string
     * @param endDateStr   End date string
     * @return Map containing error statistics by status code
     */
    Map<String, Object> getApiErrorsByStatusCode(String startDateStr, String endDateStr);

    /**
     * Retrieve API errors aggregated by endpoint.
     * 
     * @param startDateStr Start date string
     * @param endDateStr   End date string
     * @return Map containing error statistics by endpoint
     */
    Map<String, Object> getApiErrorsByEndpoint(String startDateStr, String endDateStr);

    /**
     * Get API error rate over time.
     * 
     * @param startDateStr Start date string
     * @param endDateStr   End date string
     * @return Map containing error rate statistics
     */
    Map<String, Object> getApiErrorRate(String startDateStr, String endDateStr);

    /**
     * Get error details by correlation ID.
     * 
     * @param correlationId Correlation ID
     * @return Map containing error details
     */
    Map<String, Object> getErrorByCorrelationId(String correlationId);
}
