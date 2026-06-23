package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for inventory reporting and analytics (RQ-INV-REP-01,
 * RQ-INV-REP-02, RQ-INV-REP-03).
 * Restricted to ADMIN users only.
 */
@RestController
@RequestMapping("/api/admin/inventory-reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInventoryReportController {

    private final InventoryReportService reportService;

    /**
     * Get all products currently below threshold (RQ-INV-REP-01).
     */
    @GetMapping("/below-threshold")
    public ResponseEntity<ApiResponse> getProductsBelowThreshold() {
        List<Map<String, Object>> products = reportService.getProductsBelowThreshold();
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Low stock products retrieved: " + products.size(),
                products));
    }

    /**
     * Get threshold breaches in a date range (RQ-INV-REP-02).
     */
    @GetMapping("/breaches")
    public ResponseEntity<ApiResponse> getThresholdBreachesInRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Map<String, Object>> breaches = reportService.getThresholdBreachesInRange(
                startDateTime,
                endDateTime);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Breaches retrieved: " + breaches.size(),
                breaches));
    }

    /**
     * Get products with frequent threshold breaches (RQ-INV-REP-03).
     */
    @GetMapping("/frequent-problems")
    public ResponseEntity<ApiResponse> getFrequentlyLowStockProducts(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Map<String, Object>> problems = reportService.getFrequentlyLowStockProducts(
                startDateTime,
                endDateTime);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Problem products retrieved: " + problems.size(),
                problems));
    }

    /**
     * Get detailed inventory report for a product (RQ-INV-REP-01).
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getProductInventoryReport(@PathVariable Long productId) {
        Map<String, Object> report = reportService.getProductInventoryReport(productId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Product report retrieved",
                report));
    }

    /**
     * Get system-wide inventory summary (RQ-INV-REP-01).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getInventorySummary() {
        Map<String, Object> summary = reportService.getInventorySummary();
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Inventory summary retrieved",
                summary));
    }
}
