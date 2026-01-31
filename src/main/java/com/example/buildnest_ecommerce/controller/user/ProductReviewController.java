package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.ReviewDTO;
import com.example.buildnest_ecommerce.model.entity.ProductReview;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.review.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Product Review Controller
 * Manages product reviews and ratings
 * Implements Section 6.1 - E-Commerce Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Reviews", description = "Endpoints for managing product reviews and ratings")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @Operation(summary = "Submit product review", description = "Create a new review for a product. Users can only review products they have purchased (verified purchase).", tags = {
            "Product Reviews" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review submitted successfully", content = @Content(schema = @Schema(implementation = ProductReview.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid review data or user has already reviewed this product"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> submitReview(
            @Parameter(description = "Product ID", example = "123", required = true) @PathVariable Long productId,
            @Parameter(description = "Review data", required = true) @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} submitting review for product {}", userDetails.getId(), productId);

            // Check if user purchased this product
            boolean verifiedPurchase = reviewService.hasUserPurchasedProduct(
                    userDetails.getId(), productId);

            ProductReview review = reviewService.createReview(
                    productId,
                    userDetails.getId(),
                    reviewDTO.getRating(),
                    reviewDTO.getComment(),
                    verifiedPurchase);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true,
                            verifiedPurchase ? "Review submitted (Verified Purchase)" : "Review submitted",
                            review));
        } catch (IllegalStateException e) {
            log.warn("Review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error submitting review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error submitting review: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get product reviews", description = "Retrieve paginated reviews for a product", tags = {
            "Product Reviews" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse> getProductReviews(
            @Parameter(description = "Product ID", example = "123", required = true) @PathVariable Long productId,
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        try {
            log.debug("Fetching reviews for product {}", productId);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<ProductReview> reviews = reviewService.getProductReviews(productId, pageable);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Reviews retrieved successfully", reviews));
        } catch (Exception e) {
            log.error("Error fetching reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching reviews", null));
        }
    }

    @Operation(summary = "Get product rating summary", description = "Get average rating and rating distribution for a product", tags = {
            "Product Reviews" })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getRatingSummary(
            @Parameter(description = "Product ID", example = "123", required = true) @PathVariable Long productId) {
        try {
            log.debug("Fetching rating summary for product {}", productId);
            Double avgRating = reviewService.getAverageRating(productId);
            Map<Integer, Long> distribution = reviewService.getRatingDistribution(productId);

            Map<String, Object> summary = Map.of(
                    "averageRating", avgRating,
                    "ratingDistribution", distribution,
                    "totalReviews", distribution.values().stream().mapToLong(Long::longValue).sum());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Rating summary retrieved", summary));
        } catch (Exception e) {
            log.error("Error fetching rating summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching rating summary", null));
        }
    }

    @Operation(summary = "Get top helpful reviews", description = "Get the most helpful reviews for a product (sorted by helpful count)", tags = {
            "Product Reviews" })
    @GetMapping("/top-helpful")
    public ResponseEntity<ApiResponse> getTopHelpfulReviews(
            @Parameter(description = "Product ID", example = "123", required = true) @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductReview> reviews = reviewService.getTopHelpfulReviews(productId, pageable);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Top helpful reviews retrieved", reviews));
        } catch (Exception e) {
            log.error("Error fetching top helpful reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching reviews", null));
        }
    }

    @Operation(summary = "Mark review as helpful", description = "Increment the helpful count for a review", tags = {
            "Product Reviews" })
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse> markAsHelpful(
            @PathVariable Long productId,
            @Parameter(description = "Review ID", example = "456", required = true) @PathVariable Long reviewId) {
        try {
            log.info("Marking review {} as helpful", reviewId);
            ProductReview review = reviewService.markAsHelpful(reviewId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Review marked as helpful", review.getHelpfulCount()));
        } catch (Exception e) {
            log.error("Error marking review as helpful", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error processing request", null));
        }
    }

    @Operation(summary = "Update own review", description = "Update an existing review submitted by the authenticated user", tags = {
            "Product Reviews" })
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> updateReview(
            @PathVariable Long productId,
            @Parameter(description = "Review ID", example = "456", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} updating review {}", userDetails.getId(), reviewId);
            ProductReview review = reviewService.updateReview(
                    reviewId,
                    reviewDTO.getRating(),
                    reviewDTO.getComment());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Review updated successfully", review));
        } catch (Exception e) {
            log.error("Error updating review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error updating review: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Delete own review", description = "Delete a review submitted by the authenticated user", tags = {
            "Product Reviews" })
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long productId,
            @Parameter(description = "Review ID", example = "456", required = true) @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} deleting review {}", userDetails.getId(), reviewId);
            reviewService.deleteReview(reviewId, userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Review deleted successfully", null));
        } catch (IllegalStateException e) {
            log.warn("Unauthorized delete attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error deleting review: " + e.getMessage(), null));
        }
    }
}
