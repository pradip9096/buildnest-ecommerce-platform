package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.annotation.ApiSunset;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Section 6.1 - API Versioning Sunset Management
 * 
 * Interceptor that enforces API sunset dates and adds deprecation headers.
 * Automatically blocks requests to expired API versions.
 */
@Slf4j
@Component
public class ApiSunsetInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check for @ApiSunset annotation on method
        ApiSunset methodSunset = handlerMethod.getMethodAnnotation(ApiSunset.class);

        // Check for @ApiSunset annotation on controller class
        ApiSunset classSunset = handlerMethod.getBeanType().getAnnotation(ApiSunset.class);

        ApiSunset sunset = methodSunset != null ? methodSunset : classSunset;

        if (sunset != null) {
            return handleSunsetAnnotation(sunset, request, response);
        }

        return true;
    }

    private boolean handleSunsetAnnotation(ApiSunset sunset, HttpServletRequest request, HttpServletResponse response) {
        LocalDate sunsetDate = LocalDate.parse(sunset.date(), DATE_FORMATTER);
        LocalDate today = LocalDate.now();

        // Add deprecation headers
        response.setHeader("X-API-Deprecated", "true");
        response.setHeader("X-API-Sunset", sunset.date());
        response.setHeader("X-API-Version", sunset.version());

        if (!sunset.migrationGuide().isEmpty()) {
            response.setHeader("X-API-Migration-Guide", sunset.migrationGuide());
        }

        if (!sunset.replacedBy().isEmpty()) {
            response.setHeader("X-API-Replaced-By", sunset.replacedBy());
        }

        // Check if API has expired
        if (today.isAfter(sunsetDate)) {
            if (sunset.enforce()) {
                log.error("Request to expired API: {} - Sunset date: {}",
                        request.getRequestURI(), sunset.date());

                response.setStatus(HttpServletResponse.SC_GONE);
                response.setHeader("Content-Type", "application/json");

                String errorMessage = String.format(
                        "{\"error\":\"API_EXPIRED\",\"message\":\"This API version has been sunset as of %s. " +
                                "Please migrate to version %s. Migration guide: %s\"}",
                        sunset.date(),
                        sunset.replacedBy().isEmpty() ? "latest" : sunset.replacedBy(),
                        sunset.migrationGuide());

                try {
                    response.getWriter().write(errorMessage);
                } catch (Exception e) {
                    log.error("Error writing sunset response", e);
                }

                return false; // Block the request
            } else {
                log.warn("Request to expired API (enforcement disabled): {} - Sunset date: {}",
                        request.getRequestURI(), sunset.date());
            }
        }

        // Warn if approaching sunset
        long daysUntilSunset = java.time.temporal.ChronoUnit.DAYS.between(today, sunsetDate);
        if (daysUntilSunset <= sunset.warningDays() && daysUntilSunset > 0) {
            response.setHeader("X-API-Days-Until-Sunset", String.valueOf(daysUntilSunset));

            log.warn("Request to API approaching sunset: {} - {} days until {}",
                    request.getRequestURI(), daysUntilSunset, sunset.date());
        }

        return true;
    }
}
