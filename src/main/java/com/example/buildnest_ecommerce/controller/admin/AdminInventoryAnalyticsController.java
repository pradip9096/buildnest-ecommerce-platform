package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for inventory analytics and demand correlation (RQ-INV-ANA-01,
 * RQ-INV-ANA-02).
 * Restricted to ADMIN users only.
 */
@RestController
@RequestMapping("/api/admin/inventory-analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInventoryAnalyticsController {

    private final InventoryAnalyticsService analyticsService;

    /**
     * Get products with high demand but low inventory (RQ-INV-ANA-01,
     * RQ-INV-ANA-02).
     */
    @GetMapping("/high-demand-low-inventory")
    public ResponseEntity<ApiResponse> getHighDemandLowInventoryProducts(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Map<String, Object>> products = analyticsService.getHighDemandLowInventoryProducts(
                startDateTime,
                endDateTime);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "High-demand products identified: " + products.size(),
                products));
    }

    /**
     * Get seasonal demand patterns and recommendations (RQ-INV-ANA-02).
     */
    @GetMapping("/seasonal-patterns")
    public ResponseEntity<ApiResponse> getSeasonalDemandPatterns(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Map<String, Object>> patterns = analyticsService.getSeasonalDemandPatterns(
                startDateTime,
                endDateTime);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Seasonal patterns identified: " + patterns.size(),
                patterns));
    }

    /**
     * Get stock turnover analysis (RQ-INV-ANA-02).
     */
    @GetMapping("/stock-turnover")
    public ResponseEntity<ApiResponse> getStockTurnoverAnalysis(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Map<String, Object>> analysis = analyticsService.getStockTurnoverAnalysis(
                startDateTime,
                endDateTime);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Turnover analysis completed: " + analysis.size() + " products",
                analysis));
    }

    /**
     * Get predictive restocking plan (RQ-INV-ANA-01).
     */
    @GetMapping("/restocking-plan")
    public ResponseEntity<ApiResponse> getPredictiveRestockingPlan(
            @RequestParam(defaultValue = "30") Integer daysPeriod) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);

        Map<String, Object> plan = analyticsService.getPredictiveRestockingPlan(startDate);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Restocking plan generated",
                plan));
    }
}
