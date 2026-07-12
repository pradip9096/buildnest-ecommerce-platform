package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.analytics.UserEventService;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Product API - Version 2 (Current)
 *
 * Latest product endpoints with improved response wrapping and consistency.
 * All responses wrapped in ApiResponse&lt;T&gt; format.
 * When Elasticsearch is enabled, the /search endpoint delegates to
 * {@link ProductSearchService} for full-text relevance search (SRCH-01, #74).
 */
@RestController
@RequestMapping("/api/v2/products")
@Tag(name = "Products V2", description = "Current product management endpoints")
public class ProductControllerV2 {

        private final ProductService productService;
        private final Optional<ProductSearchService> productSearchService;
        private final Optional<UserEventService> userEventService;

        @Autowired
        public ProductControllerV2(ProductService productService,
                Optional<ProductSearchService> productSearchService,
                Optional<UserEventService> userEventService) {
                this.productService = productService;
                this.productSearchService = productSearchService;
                this.userEventService = userEventService;
        }

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
                recordProductView(id);
                return ResponseEntity.ok(
                                new ApiResponse(true, "Product retrieved successfully", product));
        }

        /**
         * Fire-and-forget page-view tracking (ANL-02, #65). Resolves the current
         * user from the security context directly rather than adding an
         * {@code Authentication} parameter, so anonymous views (this endpoint is
         * public) are recorded with a null userId instead of failing/erroring.
         */
        private void recordProductView(Long productId) {
                userEventService.ifPresent(service -> {
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        Long userId = null;
                        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                                userId = userDetails.getId();
                        }
                        service.recordProductView(userId, productId);
                });
        }

        @Operation(summary = "Search products with advanced filters",
                   description = "Full-text search via Elasticsearch when enabled; falls back to JPA otherwise.")
        @GetMapping("/search")
        public ResponseEntity<ApiResponse> searchProducts(
                        @Parameter(description = "Search query", example = "cement") @RequestParam(required = false) String query,
                        @Parameter(description = "Category ID", example = "5") @RequestParam(required = false) Long categoryId,
                        @Parameter(description = "Minimum price", example = "100") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price", example = "5000") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "In stock only", example = "true") @RequestParam(required = false) Boolean inStock,
                        @Parameter(description = "Tag name", example = "eco-friendly")
                        @RequestParam(required = false) String tag,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Sort by field", example = "price") @RequestParam(defaultValue = "id") String sortBy,
                        @Parameter(description = "Sort direction", example = "ASC") @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                if (productSearchService.isPresent()) {
                        Page<?> results = productSearchService.get()
                                        .search(query, categoryId, minPrice, maxPrice, inStock, tag, pageable);
                        return ResponseEntity.ok(new ApiResponse(true, "Products search completed", results));
                }

                Page<Product> results = productService.advancedSearch(
                                query, categoryId, minPrice, maxPrice, inStock, tag, pageable);
                return ResponseEntity.ok(new ApiResponse(true, "Products search completed", results));
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
