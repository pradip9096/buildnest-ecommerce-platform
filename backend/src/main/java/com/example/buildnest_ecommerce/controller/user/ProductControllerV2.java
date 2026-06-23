package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Product API - Version 2 (Current)
 * 
 * Latest product endpoints with improved response wrapping and consistency.
 * All responses wrapped in ApiResponse&lt;T&gt; format.
 * 
 * Section 2.5.2: API Versioning - Version 2 Current Implementation
 * Section 2.2.3: Consistent response wrapping
 */
@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@Tag(name = "Products V2", description = "Current product management endpoints")
public class ProductControllerV2 {

        private final ProductService productService;

        @Operation(summary = "Get all products with pagination", description = "Returns paginated list of products with full details wrapped in ApiResponse")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
        })
        @GetMapping
        public ResponseEntity<ApiResponse> getAllProducts(
                        @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,

                        @Parameter(description = "Sort by field", example = "id") @RequestParam(defaultValue = "id") String sortBy,

                        @Parameter(description = "Sort direction", example = "DESC") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                Page<Product> products = productService.findAll(pageable);

                return ResponseEntity.ok(
                                new ApiResponse(true, "Products retrieved successfully", products));
        }

        @Operation(summary = "Get product by ID", description = "Retrieves detailed information for a specific product")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse> getProduct(
                        @Parameter(description = "Product ID", example = "123") @PathVariable Long id) {
                Product product = productService.findById(id);
                return ResponseEntity.ok(
                                new ApiResponse(true, "Product retrieved successfully", product));
        }

        @Operation(summary = "Search products with advanced filters", description = "Search products by name, category, price range, and availability")
        @GetMapping("/search")
        public ResponseEntity<ApiResponse> searchProducts(
                        @Parameter(description = "Search query", example = "cement") @RequestParam(required = false) String query,

                        @Parameter(description = "Category ID", example = "5") @RequestParam(required = false) Long categoryId,

                        @Parameter(description = "Minimum price", example = "100") @RequestParam(required = false) BigDecimal minPrice,

                        @Parameter(description = "Maximum price", example = "5000") @RequestParam(required = false) BigDecimal maxPrice,

                        @Parameter(description = "In stock only", example = "true") @RequestParam(required = false) Boolean inStock,

                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,

                        @Parameter(description = "Sort by field", example = "price") @RequestParam(defaultValue = "id") String sortBy,

                        @Parameter(description = "Sort direction", example = "ASC") @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                Page<Product> results = productService.advancedSearch(
                                query, categoryId, minPrice, maxPrice, inStock, pageable);

                return ResponseEntity.ok(
                                new ApiResponse(true, "Products search completed", results));
        }

        @Operation(summary = "Get products by category", description = "Retrieves all products in a specific category")
        @GetMapping("/category/{categoryId}")
        public ResponseEntity<ApiResponse> getProductsByCategory(
                        @Parameter(description = "Category ID", example = "5") @PathVariable Long categoryId,

                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size (max 100)", example = "20") @RequestParam(defaultValue = "20") int size) {
                // 2.4 MEDIUM - Pagination Best Practices: Enforce max page size
                final int MAX_PAGE_SIZE = 100;
                size = Math.min(size, MAX_PAGE_SIZE);

                Pageable pageable = PageRequest.of(page, size);
                Page<Product> products = productService.findByCategory(categoryId, pageable);

                return ResponseEntity.ok(
                                new ApiResponse(true, "Category products retrieved successfully", products));
        }
}
