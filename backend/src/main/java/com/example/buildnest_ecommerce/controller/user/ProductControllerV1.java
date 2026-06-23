package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Product API - Version 1 (Legacy)
 * 
 * DEPRECATED: Use ProductControllerV2 instead
 * Sunset Date: 2026-12-31
 * Migration Guide: https://docs.buildnest.com/api/v2-migration
 * 
 * This version will be removed in v3.0.0
 * 
 * Section 6.1: API Versioning Sunset Management
 * Implements sunset date enforcement to prevent usage of deprecated APIs
 */
@Deprecated(since = "2.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products V1 (Deprecated)", description = "Legacy product endpoints - use v2 instead")
public class ProductControllerV1 {

        private final ProductService productService;

        // Section 6.1: API Versioning Sunset Management
        private static final LocalDate SUNSET_DATE = LocalDate.of(2026, 12, 31);
        private static final String MIGRATION_GUIDE_URL = "https://docs.buildnest.com/api/v2-migration";

        /**
         * Checks if the API has passed its sunset date on application startup.
         * Prevents deployment of expired API versions.
         */
        @PostConstruct
        public void checkSunsetDate() {
                LocalDate today = getToday();
                LocalDate sunsetDate = getSunsetDate();

                if (today.isAfter(sunsetDate)) {
                        String errorMessage = String.format(
                                        "API V1 has reached sunset date (%s). This controller must be removed. " +
                                                        "Migration guide: %s",
                                        sunsetDate, MIGRATION_GUIDE_URL);
                        log.error(errorMessage);
                        throw new IllegalStateException(errorMessage);
                }

                // Log warning if within 90 days of sunset
                long daysUntilSunset = java.time.temporal.ChronoUnit.DAYS.between(today, sunsetDate);
                if (daysUntilSunset <= 90) {
                        log.warn("API V1 will sunset in {} days ({}). Please migrate to V2. Guide: {}",
                                        daysUntilSunset, sunsetDate, MIGRATION_GUIDE_URL);
                }
        }

        protected LocalDate getToday() {
                return LocalDate.now();
        }

        protected LocalDate getSunsetDate() {
                return SUNSET_DATE;
        }

        @Operation(summary = "Get all products (V1 - Deprecated)", description = "Returns all products with pagination. Use v2 for better response format", deprecated = true)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
        })
        @GetMapping
        public ResponseEntity<Page<Product>> getAllProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<Product> products = productService.findAll(pageable);

                return ResponseEntity.ok()
                                .header("X-API-Deprecated", "true")
                                .header("X-API-Sunset", "2026-12-31")
                                .header("X-API-Migration-Guide", "https://docs.buildnest.com/api/v2-migration")
                                .body(products);
        }

        @Operation(summary = "Get product by ID (V1 - Deprecated)", description = "Retrieves a single product by ID. Use v2 for wrapped response", deprecated = true)
        @GetMapping("/{id}")
        public ResponseEntity<Product> getProduct(@PathVariable Long id) {
                Product product = productService.findById(id);

                return ResponseEntity.ok()
                                .header("X-API-Deprecated", "true")
                                .header("X-API-Sunset", "2026-12-31")
                                .body(product);
        }
}
