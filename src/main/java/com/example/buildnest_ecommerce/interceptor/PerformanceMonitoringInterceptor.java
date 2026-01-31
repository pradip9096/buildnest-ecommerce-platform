package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.service.monitoring.PerformanceMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Performance Monitoring Interceptor
 * 
 * Intercepts all HTTP requests and measures response time.
 * Enables tracking of API performance against SLA requirements.
 * 
 * SYS-PERF-001: API response time < 500ms (95th percentile)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {

    private final PerformanceMonitoringService performanceMonitoringService;

    private static final String START_TIME_ATTRIBUTE = "startTime";

    /**
     * Pre-handle: Record request start time
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        return true;
    }

    /**
     * After completion: Calculate and record response time
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        try {
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long endTime = System.currentTimeMillis();
                long responseTimeMs = endTime - startTime;

                String endpoint = request.getMethod() + " " + request.getRequestURI();
                performanceMonitoringService.recordResponseTime(endpoint, responseTimeMs);

                // Log very slow requests
                if (responseTimeMs > 1000) {
                    log.warn("SLOW_API: {} completed in {}ms (status: {})",
                            endpoint, responseTimeMs, response.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("Error recording performance metrics: {}", e.getMessage());
        }
    }
}
