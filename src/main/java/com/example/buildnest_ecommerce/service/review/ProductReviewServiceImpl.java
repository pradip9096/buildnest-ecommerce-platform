package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductReview;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductReviewRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product Review Service Implementation
 * Manages product reviews with transaction support and validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ProductReview createReview(Long productId, Long userId, Integer rating,
            String comment, boolean verifiedPurchase) {
        log.info("Creating review for product {} by user {}", productId, userId);

        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new IllegalStateException("User has already reviewed this product");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(rating)
                .comment(comment)
                .verifiedPurchase(verifiedPurchase)
                .helpfulCount(0)
                .isVisible(true)
                .build();

        ProductReview savedReview = reviewRepository.save(review);

        log.info("Review created with id {} for product {} (rating: {})",
                savedReview.getId(), productId, rating);

        return savedReview;
    }

    @Override
    @Transactional
    public ProductReview updateReview(Long reviewId, Integer rating, String comment) {
        log.info("Updating review {}", reviewId);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.setRating(rating);
        review.setComment(comment);

        ProductReview updatedReview = reviewRepository.save(review);

        log.info("Review {} updated with new rating: {}", reviewId, rating);

        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review {} by user {}", reviewId, userId);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Verify user owns the review
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("User is not authorized to delete this review");
        }

        reviewRepository.delete(review);

        log.info("Review {} deleted successfully", reviewId);
    }

    @Override
    public Page<ProductReview> getProductReviews(Long productId, Pageable pageable) {
        log.debug("Fetching reviews for product {}", productId);
        return reviewRepository.findByProductIdAndIsVisibleTrue(productId, pageable);
    }

    @Override
    public Page<ProductReview> getUserReviews(Long userId, Pageable pageable) {
        log.debug("Fetching reviews by user {}", userId);
        return reviewRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public ProductReview markAsHelpful(Long reviewId) {
        log.info("Marking review {} as helpful", reviewId);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.incrementHelpfulCount();
        ProductReview updatedReview = reviewRepository.save(review);

        log.info("Review {} helpful count incremented to {}", reviewId, updatedReview.getHelpfulCount());

        return updatedReview;
    }

    @Override
    public Double getAverageRating(Long productId) {
        log.debug("Calculating average rating for product {}", productId);
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    public Map<Integer, Long> getRatingDistribution(Long productId) {
        log.debug("Fetching rating distribution for product {}", productId);

        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);
        Map<Integer, Long> ratingMap = new HashMap<>();

        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(i, 0L);
        }

        // Fill in actual counts
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingMap.put(rating, count);
        }

        return ratingMap;
    }

    @Override
    public Page<ProductReview> getTopHelpfulReviews(Long productId, Pageable pageable) {
        log.debug("Fetching top helpful reviews for product {}", productId);
        return reviewRepository.findTopHelpfulReviews(productId, pageable);
    }

    @Override
    public Page<ProductReview> getVerifiedPurchaseReviews(Long productId, Pageable pageable) {
        log.debug("Fetching verified purchase reviews for product {}", productId);
        return reviewRepository.findVerifiedPurchaseReviews(productId, pageable);
    }

    @Override
    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        log.debug("Checking if user {} has purchased product {}", userId, productId);
        // Check if user has any completed orders containing this product
        return orderRepository.findByUserId(userId).stream()
                .anyMatch(order -> order.getOrderItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)));
    }
}
