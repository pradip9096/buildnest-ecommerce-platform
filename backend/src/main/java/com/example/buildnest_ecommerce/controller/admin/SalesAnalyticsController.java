package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.analytics.SalesAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Sales Analytics Controller
 * Provides business intelligence and sales analytics endpoints
 * Implements Section 6.2 - Business Intelligence Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@RestController
@RequestMapping("/api/admin/analytics/sales")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Sales Analytics", description = "Business intelligence and sales analytics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class SalesAnalyticsController {

    private final SalesAnalyticsService analyticsService;

    @Operation(summary = "Get sales dashboard", description = "Retrieve comprehensive sales analytics dashboard with revenue, orders, and trend data", tags = {
            "Sales Analytics" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully", content = @Content(schema = @Schema(implementation = SalesDashboardDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-12-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Default to last 30 days if not specified
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            log.info("Admin requesting sales dashboard for period {} to {}", startDate, endDate);
            SalesDashboardDTO dashboard = analyticsService.getDashboard(startDate, endDate);

            return ResponseEntity.ok(
                    new ApiResponse(true, "Dashboard data retrieved successfully", dashboard));
        } catch (Exception e) {
            log.error("Error generating sales dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error generating dashboard: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get daily revenue", description = "Get revenue for a specific date", tags = {
            "Sales Analytics" })
    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse> getDailyRevenue(
            @Parameter(description = "Date (YYYY-MM-DD)", example = "2024-01-15") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.debug("Fetching revenue for date: {}", date);
            Double revenue = analyticsService.getDailyRevenue(date);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Revenue retrieved", revenue));
        } catch (Exception e) {
            log.error("Error fetching daily revenue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching revenue", null));
        }
    }

    @Operation(summary = "Get conversion rate", description = "Calculate conversion rate (orders/visitors) for a date range", tags = {
            "Sales Analytics" })
    @GetMapping("/conversion-rate")
    public ResponseEntity<ApiResponse> getConversionRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("Calculating conversion rate for {} to {}", startDate, endDate);
            Double rate = analyticsService.getConversionRate(startDate, endDate);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Conversion rate calculated", rate));
        } catch (Exception e) {
            log.error("Error calculating conversion rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error calculating rate", null));
        }
    }

    @Operation(summary = "Get cart abandonment rate", description = "Calculate cart abandonment rate for a date range", tags = {
            "Sales Analytics" })
    @GetMapping("/cart-abandonment-rate")
    public ResponseEntity<ApiResponse> getCartAbandonmentRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("Calculating cart abandonment rate for {} to {}", startDate, endDate);
            Double rate = analyticsService.getCartAbandonmentRate(startDate, endDate);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Cart abandonment rate calculated", rate));
        } catch (Exception e) {
            log.error("Error calculating cart abandonment rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error calculating rate", null));
        }
    }

    @Operation(summary = "Get average order value", description = "Calculate average order value for a date range", tags = {
            "Sales Analytics" })
    @GetMapping("/average-order-value")
    public ResponseEntity<ApiResponse> getAverageOrderValue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("Calculating average order value for {} to {}", startDate, endDate);
            Double aov = analyticsService.getAverageOrderValue(startDate, endDate);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Average order value calculated", aov));
        } catch (Exception e) {
            log.error("Error calculating average order value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error calculating value", null));
        }
    }

    @Operation(summary = "Get customer lifetime value", description = "Calculate total lifetime value for a specific customer", tags = {
            "Sales Analytics" })
    @GetMapping("/customer-lifetime-value/{userId}")
    public ResponseEntity<ApiResponse> getCustomerLifetimeValue(
            @Parameter(description = "User ID", example = "123", required = true) @PathVariable Long userId) {
        try {
            log.debug("Calculating lifetime value for user {}", userId);
            Double ltv = analyticsService.getCustomerLifetimeValue(userId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Customer lifetime value calculated", ltv));
        } catch (Exception e) {
            log.error("Error calculating customer lifetime value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error calculating value", null));
        }
    }
}
