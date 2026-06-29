package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Admin endpoint to trigger a full Elasticsearch product re-index (SRCH-02, #75).
 *
 * The reindex operation is only available when Elasticsearch is enabled
 * ({@code elasticsearch.enabled=true}). When disabled, the endpoint returns 503.
 */
@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSearchController {

    private final Optional<ProductSearchService> productSearchService;

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse> reindex() {
        if (productSearchService.isEmpty()) {
            return ResponseEntity.status(503)
                    .body(new ApiResponse(false, "Elasticsearch is not enabled", null));
        }
        productSearchService.get().reindexAll();
        return ResponseEntity.ok(new ApiResponse(true, "Product re-index completed successfully", null));
    }
}
