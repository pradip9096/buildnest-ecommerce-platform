package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.ReviewDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.entity.SellerReview;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.review.SellerReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SellerReviewControllerTest {

    private CustomUserDetails userDetails() {
        return new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
    }

    @Test
    void submitAndFetchReviews() {
        SellerReviewService service = mock(SellerReviewService.class);
        when(service.hasUserPurchasedFromSeller(1L, 10L)).thenReturn(true);
        when(service.createReview(eq(10L), eq(1L), eq(5), eq("Great"),
                eq(true))).thenReturn(new SellerReview());

        Page<SellerReview> page = new PageImpl<>(
                Collections.singletonList(new SellerReview()));
        when(service.getSellerReviews(eq(10L), any())).thenReturn(page);
        when(service.getAverageRating(10L)).thenReturn(4.5);
        when(service.getRatingDistribution(10L)).thenReturn(Map.of(5, 2L));
        when(service.updateReview(eq(99L), eq(1L), eq(5), eq("Updated")))
                .thenReturn(new SellerReview());

        SellerReviewController controller = new SellerReviewController(service);

        ReviewDTO reviewDTO = new ReviewDTO(5, "Great");
        assertEquals(HttpStatus.CREATED,
                controller.submitReview(10L, reviewDTO, userDetails())
                        .getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.getSellerReviews(10L, 0, 10, "createdAt",
                        org.springframework.data.domain.Sort.Direction.DESC)
                        .getStatusCode());
        assertEquals(HttpStatus.OK, controller.getRatingSummary(10L)
                .getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.updateReview(10L, 99L, new ReviewDTO(5, "Updated"),
                        userDetails()).getStatusCode());
    }

    @Test
    void submitReviewUnverifiedPurchase() {
        SellerReviewService service = mock(SellerReviewService.class);
        when(service.hasUserPurchasedFromSeller(1L, 10L)).thenReturn(false);
        when(service.createReview(eq(10L), eq(1L), eq(4), eq("Okay"),
                eq(false))).thenReturn(new SellerReview());

        SellerReviewController controller = new SellerReviewController(service);

        ApiResponse response = controller
                .submitReview(10L, new ReviewDTO(4, "Okay"), userDetails())
                .getBody();
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Review submitted", response.getMessage());
    }

    @Test
    void handlesSubmitErrors() {
        SellerReviewService service = mock(SellerReviewService.class);
        when(service.hasUserPurchasedFromSeller(1L, 10L)).thenReturn(false);
        when(service.createReview(anyLong(), anyLong(), anyInt(), anyString(),
                anyBoolean()))
                .thenThrow(new IllegalStateException("duplicate"));

        SellerReviewController controller = new SellerReviewController(service);
        ResponseEntity<?> response = controller.submitReview(10L,
                new ReviewDTO(5, "Great"), userDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handlesFetchErrors() {
        SellerReviewService service = mock(SellerReviewService.class);
        when(service.getSellerReviews(eq(10L), any()))
                .thenThrow(new RuntimeException("fail"));
        when(service.getAverageRating(eq(10L)))
                .thenThrow(new RuntimeException("fail"));

        SellerReviewController controller = new SellerReviewController(service);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getSellerReviews(10L, 0, 10, "createdAt",
                        org.springframework.data.domain.Sort.Direction.DESC)
                        .getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getRatingSummary(10L).getStatusCode());
    }

    @Test
    void handlesUpdateDeleteErrors() {
        SellerReviewService service = mock(SellerReviewService.class);
        when(service.updateReview(eq(5L), eq(1L), anyInt(), anyString()))
                .thenThrow(new RuntimeException("fail"));
        doThrow(new IllegalStateException("forbidden")).when(service)
                .deleteReview(5L, 1L);

        SellerReviewController controller = new SellerReviewController(service);
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.updateReview(10L, 5L, new ReviewDTO(2, "Bad"),
                        userDetails()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                controller.deleteReview(10L, 5L, userDetails())
                        .getStatusCode());
    }

    @Test
    void deleteReviewHandlesGenericError() {
        SellerReviewService service = mock(SellerReviewService.class);
        doThrow(new RuntimeException("fail")).when(service)
                .deleteReview(5L, 1L);

        SellerReviewController controller = new SellerReviewController(service);
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.deleteReview(10L, 5L, userDetails())
                        .getStatusCode());
    }
}
