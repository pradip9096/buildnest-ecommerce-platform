package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.ReviewDTO;
import com.example.buildnest_ecommerce.model.entity.SellerReview;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.review.SellerReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Seller Review Controller (FR-SEL-07, #558) — buyer-to-seller ratings,
 * mirrors {@code ProductReviewController}'s shape scoped by sellerId
 * (User.id) instead of productId.
 */
@RestController
@RequestMapping("/api/sellers/{sellerId}/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seller Reviews",
        description = "Endpoints for buyer-to-seller ratings and reviews")
public class SellerReviewController {

    private final SellerReviewService reviewService;

    @Operation(summary = "Submit seller review", tags = { "Seller Reviews" })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> submitReview(
            @PathVariable Long sellerId,
            @Parameter(description = "Review data", required = true)
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} submitting review for seller {}",
                    userDetails.getId(), sellerId);

            boolean verifiedPurchase = reviewService
                    .hasUserPurchasedFromSeller(userDetails.getId(),
                            sellerId);

            SellerReview review = reviewService.createReview(
                    sellerId,
                    userDetails.getId(),
                    reviewDTO.getRating(),
                    reviewDTO.getComment(),
                    verifiedPurchase);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true,
                            verifiedPurchase
                                    ? "Review submitted (Verified Purchase)"
                                    : "Review submitted",
                            review));
        } catch (IllegalStateException e) {
            log.warn("Seller review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error submitting seller review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error submitting review: " + e.getMessage(),
                            null));
        }
    }

    @Operation(summary = "Get seller reviews", tags = { "Seller Reviews" })
    @GetMapping
    public ResponseEntity<ApiResponse> getSellerReviews(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        try {
            log.debug("Fetching reviews for seller {}", sellerId);
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(direction, sortBy));
            Page<SellerReview> reviews = reviewService
                    .getSellerReviews(sellerId, pageable);
            return ResponseEntity.ok(new ApiResponse(true,
                    "Reviews retrieved successfully", reviews));
        } catch (Exception e) {
            log.error("Error fetching seller reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error fetching reviews", null));
        }
    }

    @Operation(summary = "Get seller rating summary",
            tags = { "Seller Reviews" })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getRatingSummary(
            @PathVariable Long sellerId) {
        try {
            log.debug("Fetching rating summary for seller {}", sellerId);
            Double avgRating = reviewService.getAverageRating(sellerId);
            Map<Integer, Long> distribution =
                    reviewService.getRatingDistribution(sellerId);

            Map<String, Object> summary = Map.of(
                    "averageRating", avgRating,
                    "ratingDistribution", distribution,
                    "totalReviews", distribution.values().stream()
                            .mapToLong(Long::longValue).sum());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Rating summary retrieved",
                            summary));
        } catch (Exception e) {
            log.error("Error fetching seller rating summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error fetching rating summary", null));
        }
    }

    @Operation(summary = "Update own seller review",
            tags = { "Seller Reviews" })
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> updateReview(
            @PathVariable Long sellerId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} updating seller review {}",
                    userDetails.getId(), reviewId);
            SellerReview review = reviewService.updateReview(
                    reviewId,
                    userDetails.getId(),
                    reviewDTO.getRating(),
                    reviewDTO.getComment());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Review updated successfully",
                            review));
        } catch (IllegalStateException e) {
            log.warn("Unauthorized update attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating seller review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,
                            "Error updating review: " + e.getMessage(),
                            null));
        }
    }

    @Operation(summary = "Delete own seller review",
            tags = { "Seller Reviews" })
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long sellerId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("User {} deleting seller review {}",
                    userDetails.getId(), reviewId);
            reviewService.deleteReview(reviewId, userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Review deleted successfully",
                            null));
        } catch (IllegalStateException e) {
            log.warn("Unauthorized delete attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting seller review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,
                            "Error deleting review: " + e.getMessage(),
                            null));
        }
    }
}
