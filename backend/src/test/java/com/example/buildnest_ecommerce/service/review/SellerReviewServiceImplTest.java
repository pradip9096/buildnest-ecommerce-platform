package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.OrderItem;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.SellerReview;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.SellerReviewRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerReviewServiceImpl tests")
class SellerReviewServiceImplTest {

    @Mock
    private SellerReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SellerReviewServiceImpl reviewService;

    @Test
    @DisplayName("Should create review")
    void testCreateReview() {
        User seller = new User();
        seller.setId(1L);
        User user = new User();
        user.setId(2L);

        when(reviewRepository.existsBySellerIdAndUserId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(SellerReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerReview review =
                reviewService.createReview(1L, 2L, 5, "Great", true);
        assertEquals(5, review.getRating());
    }

    @Test
    @DisplayName("Should throw when user has already reviewed seller")
    void testCreateReviewDuplicate() {
        when(reviewRepository.existsBySellerIdAndUserId(1L, 2L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> reviewService.createReview(1L, 2L, 5, "x", false));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw when seller not found")
    void testCreateReviewMissingSeller() {
        when(reviewRepository.existsBySellerIdAndUserId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(1L, 2L, 5, "x", false));
    }

    @Test
    @DisplayName("Should throw when reviewing user not found")
    void testCreateReviewMissingUser() {
        User seller = new User();
        seller.setId(1L);
        when(reviewRepository.existsBySellerIdAndUserId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(1L, 2L, 5, "x", false));
    }

    @Test
    @DisplayName("Should update review by owner")
    void testUpdateReview() {
        User user = new User();
        user.setId(2L);
        SellerReview review = new SellerReview();
        review.setId(3L);
        review.setUser(user);

        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(SellerReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerReview updated =
                reviewService.updateReview(3L, 2L, 4, "Updated");
        assertEquals(4, updated.getRating());
        assertEquals("Updated", updated.getComment());
    }

    @Test
    @DisplayName("Should throw on update by non-owner")
    void testUpdateReviewUnauthorized() {
        User user = new User();
        user.setId(2L);
        SellerReview review = new SellerReview();
        review.setUser(user);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class,
                () -> reviewService.updateReview(3L, 9L, 4, "x"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when review not found on update")
    void testUpdateReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.updateReview(99L, 2L, 3, "new"));
    }

    @Test
    @DisplayName("Should delete review by owner")
    void testDeleteReview() {
        User user = new User();
        user.setId(2L);
        SellerReview review = new SellerReview();
        review.setUser(user);

        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(3L, 2L);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("Should throw on delete by non-owner")
    void testDeleteReviewUnauthorized() {
        User user = new User();
        user.setId(2L);
        SellerReview review = new SellerReview();
        review.setUser(user);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class,
                () -> reviewService.deleteReview(3L, 9L));
    }

    @Test
    @DisplayName("Should throw when review not found on delete")
    void testDeleteReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteReview(99L, 2L));
    }

    @Test
    @DisplayName("Should return average rating with fallback")
    void testGetAverageRating() {
        when(reviewRepository.calculateAverageRating(1L)).thenReturn(null);
        assertEquals(0.0, reviewService.getAverageRating(1L));

        when(reviewRepository.calculateAverageRating(1L)).thenReturn(4.5);
        assertEquals(4.5, reviewService.getAverageRating(1L));
    }

    @Test
    @DisplayName("Should return rating distribution")
    void testGetRatingDistribution() {
        when(reviewRepository.getRatingDistribution(1L)).thenReturn(
                List.of(new Object[] { 5, 2L }, new Object[] { 4, 1L }));

        Map<Integer, Long> dist = reviewService.getRatingDistribution(1L);
        assertEquals(2L, dist.get(5));
        assertEquals(1L, dist.get(4));
        assertEquals(0L, dist.get(1));
    }

    @Test
    @DisplayName("Should return empty distribution when no reviews exist")
    void testGetRatingDistributionEmpty() {
        when(reviewRepository.getRatingDistribution(1L))
                .thenReturn(Collections.emptyList());

        Map<Integer, Long> dist = reviewService.getRatingDistribution(1L);
        for (int i = 1; i <= 5; i++) {
            assertEquals(0L, dist.get(i), "Rating " + i + " should be 0");
        }
    }

    @Test
    @DisplayName("Should detect user purchase from seller")
    void testHasUserPurchasedFromSeller() {
        User seller = new User();
        seller.setId(10L);
        Product product = new Product();
        product.setSeller(seller);
        OrderItem item = new OrderItem();
        item.setProduct(product);
        Order order = new Order();
        order.setOrderItems(new java.util.HashSet<>(List.of(item)));

        when(orderRepository.findByUserId(2L)).thenReturn(List.of(order));

        assertTrue(reviewService.hasUserPurchasedFromSeller(2L, 10L));
    }

    @Test
    @DisplayName("Should return false when user has not purchased from seller")
    void testHasUserPurchasedFromSellerFalse() {
        when(orderRepository.findByUserId(2L))
                .thenReturn(Collections.emptyList());

        assertFalse(reviewService.hasUserPurchasedFromSeller(2L, 10L));
    }

    @Test
    @DisplayName("Should delegate getSellerReviews to repository")
    void testGetSellerReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SellerReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findBySellerIdAndIsVisibleTrue(1L, pageable))
                .thenReturn(page);

        Page<SellerReview> result =
                reviewService.getSellerReviews(1L, pageable);
        assertSame(page, result);
    }

    @Test
    @DisplayName("Should delegate getUserReviews to repository")
    void testGetUserReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SellerReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByUserId(2L, pageable)).thenReturn(page);

        Page<SellerReview> result =
                reviewService.getUserReviews(2L, pageable);
        assertSame(page, result);
    }
}
