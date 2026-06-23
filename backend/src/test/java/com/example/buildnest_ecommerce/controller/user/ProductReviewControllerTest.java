package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.ReviewDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.entity.ProductReview;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.review.ProductReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductReviewControllerTest {

    private CustomUserDetails userDetails() {
        return new CustomUserDetails(1L, "user", "u@example.com", "pass", Collections.emptyList(), true, true, true,
                true);
    }

    @Test
    void submitAndFetchReviews() {
        ProductReviewService service = mock(ProductReviewService.class);
        when(service.hasUserPurchasedProduct(1L, 10L)).thenReturn(true);
        when(service.createReview(eq(10L), eq(1L), eq(5), eq("Great"), eq(true))).thenReturn(new ProductReview());

        Page<ProductReview> page = new PageImpl<>(Collections.singletonList(new ProductReview()));
        when(service.getProductReviews(eq(10L), any())).thenReturn(page);
        when(service.getAverageRating(10L)).thenReturn(4.5);
        when(service.getRatingDistribution(10L)).thenReturn(Map.of(5, 2L));
        when(service.getTopHelpfulReviews(eq(10L), any())).thenReturn(page);
        when(service.markAsHelpful(99L)).thenReturn(new ProductReview());
        when(service.updateReview(eq(99L), eq(5), eq("Updated"))).thenReturn(new ProductReview());

        ProductReviewController controller = new ProductReviewController(service);

        ReviewDTO reviewDTO = new ReviewDTO(5, "Great");
        assertEquals(HttpStatus.CREATED, controller.submitReview(10L, reviewDTO, userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller
                        .getProductReviews(10L, 0, 10, "createdAt", org.springframework.data.domain.Sort.Direction.DESC)
                        .getStatusCode());
        assertEquals(HttpStatus.OK, controller.getRatingSummary(10L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getTopHelpfulReviews(10L, 0, 5).getStatusCode());
        assertEquals(HttpStatus.OK, controller.markAsHelpful(10L, 99L).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.updateReview(10L, 99L, new ReviewDTO(5, "Updated"), userDetails()).getStatusCode());
    }

    @Test
    void submitReviewUnverifiedPurchase() {
        ProductReviewService service = mock(ProductReviewService.class);
        when(service.hasUserPurchasedProduct(1L, 10L)).thenReturn(false);
        when(service.createReview(eq(10L), eq(1L), eq(4), eq("Okay"), eq(false))).thenReturn(new ProductReview());

        ProductReviewController controller = new ProductReviewController(service);

        ApiResponse response = controller.submitReview(10L, new ReviewDTO(4, "Okay"), userDetails()).getBody();
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Review submitted", response.getMessage());
    }

    @Test
    void handlesErrors() {
        ProductReviewService service = mock(ProductReviewService.class);
        when(service.hasUserPurchasedProduct(1L, 10L)).thenReturn(false);
        when(service.createReview(anyLong(), anyLong(), anyInt(), anyString(), anyBoolean()))
                .thenThrow(new IllegalStateException("duplicate"));

        ProductReviewController controller = new ProductReviewController(service);
        ResponseEntity<?> response = controller.submitReview(10L, new ReviewDTO(5, "Great"), userDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handlesReviewFetchErrors() {
        ProductReviewService service = mock(ProductReviewService.class);
        when(service.getProductReviews(eq(10L), any(Pageable.class)))
                .thenThrow(new RuntimeException("fail"));
        when(service.getAverageRating(eq(10L))).thenThrow(new RuntimeException("fail"));
        when(service.getTopHelpfulReviews(eq(10L), any(Pageable.class)))
                .thenThrow(new RuntimeException("fail"));

        ProductReviewController controller = new ProductReviewController(service);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getProductReviews(10L, 0, 10, "createdAt",
                        org.springframework.data.domain.Sort.Direction.DESC).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getRatingSummary(10L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getTopHelpfulReviews(10L, 0, 5).getStatusCode());
    }

    @Test
    void handlesReviewUpdateDeleteErrors() {
        ProductReviewService service = mock(ProductReviewService.class);
        when(service.markAsHelpful(5L)).thenThrow(new RuntimeException("fail"));
        when(service.updateReview(eq(5L), anyInt(), anyString())).thenThrow(new RuntimeException("fail"));
        doThrow(new IllegalStateException("forbidden")).when(service).deleteReview(5L, 1L);

        ProductReviewController controller = new ProductReviewController(service);
        assertEquals(HttpStatus.BAD_REQUEST, controller.markAsHelpful(10L, 5L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.updateReview(10L, 5L, new ReviewDTO(2, "Bad"), userDetails()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, controller.deleteReview(10L, 5L, userDetails()).getStatusCode());
    }

    @Test
    void deleteReviewHandlesGenericError() {
        ProductReviewService service = mock(ProductReviewService.class);
        doThrow(new RuntimeException("fail")).when(service).deleteReview(5L, 1L);

        ProductReviewController controller = new ProductReviewController(service);
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteReview(10L, 5L, userDetails()).getStatusCode());
    }
}
