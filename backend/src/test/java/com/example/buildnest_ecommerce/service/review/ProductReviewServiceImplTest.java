package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.OrderItem;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductReview;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductReviewRepository;
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
@DisplayName("ProductReviewServiceImpl tests")
class ProductReviewServiceImplTest {

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductReviewServiceImpl reviewService;

    @Test
    @DisplayName("Should create review")
    void testCreateReview() {
        Product product = new Product();
        product.setId(1L);
        User user = new User();
        user.setId(2L);

        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(ProductReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductReview review = reviewService.createReview(1L, 2L, 5, "Great", true);
        assertEquals(5, review.getRating());
    }

    @Test
    @DisplayName("Should update review")
    void testUpdateReview() {
        ProductReview review = new ProductReview();
        review.setId(3L);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(ProductReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductReview updated = reviewService.updateReview(3L, 4, "Updated");
        assertEquals(4, updated.getRating());
        assertEquals("Updated", updated.getComment());
    }

    @Test
    @DisplayName("Should delete review by owner")
    void testDeleteReview() {
        User user = new User();
        user.setId(2L);
        ProductReview review = new ProductReview();
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
        ProductReview review = new ProductReview();
        review.setUser(user);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () -> reviewService.deleteReview(3L, 9L));
    }

    @Test
    @DisplayName("Should increment helpful count")
    void testMarkAsHelpful() {
        ProductReview review = new ProductReview();
        review.setHelpfulCount(0);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(ProductReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductReview updated = reviewService.markAsHelpful(3L);
        assertEquals(1, updated.getHelpfulCount());
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
        when(reviewRepository.getRatingDistribution(1L))
                .thenReturn(List.of(new Object[] { 5, 2L }, new Object[] { 4, 1L }));

        Map<Integer, Long> dist = reviewService.getRatingDistribution(1L);
        assertEquals(2L, dist.get(5));
        assertEquals(1L, dist.get(4));
        assertEquals(0L, dist.get(1));
    }

    @Test
    @DisplayName("Should detect user purchase")
    void testHasUserPurchasedProduct() {
        Product product = new Product();
        product.setId(10L);
        OrderItem item = new OrderItem();
        item.setProduct(product);
        Order order = new Order();
        order.setOrderItems(new java.util.HashSet<>(List.of(item)));

        when(orderRepository.findByUserId(2L)).thenReturn(List.of(order));

        assertTrue(reviewService.hasUserPurchasedProduct(2L, 10L));
    }

    @Test
    @DisplayName("Should throw when product not found")
    void testCreateReviewMissingProduct() {
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(1L, 2L, 5, "x", false));
    }

    @Test
    @DisplayName("Should throw when user not found during create")
    void testCreateReviewMissingUser() {
        Product product = new Product();
        product.setId(1L);
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(1L, 2L, 5, "x", false));
    }

    @Test
    @DisplayName("Should throw when user has already reviewed product")
    void testCreateReviewDuplicate() {
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> reviewService.createReview(1L, 2L, 5, "x", false));
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw when review not found on update")
    void testUpdateReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.updateReview(99L, 3, "new"));
    }

    @Test
    @DisplayName("Should throw when review not found on delete")
    void testDeleteReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(99L, 2L));
    }

    @Test
    @DisplayName("Should throw when review not found on markAsHelpful")
    void testMarkAsHelpfulNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.markAsHelpful(99L));
    }

    @Test
    @DisplayName("Should return empty distribution when no reviews exist")
    void testGetRatingDistributionEmpty() {
        when(reviewRepository.getRatingDistribution(1L)).thenReturn(Collections.emptyList());

        Map<Integer, Long> dist = reviewService.getRatingDistribution(1L);
        for (int i = 1; i <= 5; i++) {
            assertEquals(0L, dist.get(i), "Rating " + i + " should default to 0");
        }
    }

    @Test
    @DisplayName("Should return false when user has not purchased product")
    void testHasUserPurchasedProductFalse() {
        when(orderRepository.findByUserId(2L)).thenReturn(Collections.emptyList());

        assertFalse(reviewService.hasUserPurchasedProduct(2L, 10L));
    }

    @Test
    @DisplayName("Should return false when orders exist but product not in any of them")
    void testHasUserPurchasedProductWrongProduct() {
        Product otherProduct = new Product();
        otherProduct.setId(99L);
        OrderItem item = new OrderItem();
        item.setProduct(otherProduct);
        Order order = new Order();
        order.setOrderItems(new java.util.HashSet<>(List.of(item)));

        when(orderRepository.findByUserId(2L)).thenReturn(List.of(order));

        assertFalse(reviewService.hasUserPurchasedProduct(2L, 10L));
    }

    @Test
    @DisplayName("Should delegate getProductReviews to repository")
    void testGetProductReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByProductIdAndIsVisibleTrue(1L, pageable)).thenReturn(page);

        Page<ProductReview> result = reviewService.getProductReviews(1L, pageable);
        assertSame(page, result);
        verify(reviewRepository).findByProductIdAndIsVisibleTrue(1L, pageable);
    }

    @Test
    @DisplayName("Should delegate getUserReviews to repository")
    void testGetUserReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByUserId(2L, pageable)).thenReturn(page);

        Page<ProductReview> result = reviewService.getUserReviews(2L, pageable);
        assertSame(page, result);
    }

    @Test
    @DisplayName("Should delegate getTopHelpfulReviews to repository")
    void testGetTopHelpfulReviews() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProductReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findTopHelpfulReviews(1L, pageable)).thenReturn(page);

        Page<ProductReview> result = reviewService.getTopHelpfulReviews(1L, pageable);
        assertSame(page, result);
    }

    @Test
    @DisplayName("Should delegate getVerifiedPurchaseReviews to repository")
    void testGetVerifiedPurchaseReviews() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProductReview> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findVerifiedPurchaseReviews(1L, pageable)).thenReturn(page);

        Page<ProductReview> result = reviewService.getVerifiedPurchaseReviews(1L, pageable);
        assertSame(page, result);
    }
}
