package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.SellerReview;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.SellerReviewRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Seller Review Service Implementation (FR-SEL-07, #558) — mirrors
 * {@code ProductReviewServiceImpl}'s transaction/validation shape, scoped
 * by sellerId (User.id) instead of productId. Unlike its precedent,
 * {@code updateReview} also enforces ownership — {@code ProductReview}'s
 * own update path has no such check (a latent IDOR, filed separately
 * rather than fixed here), and this repo has a documented history of that
 * exact gap (missing ownership check on a mutating endpoint).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SellerReviewServiceImpl implements SellerReviewService {

    private static final String REVIEW_NOT_FOUND_MSG =
            "Seller review not found with id: ";

    private final SellerReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * {@code review.user} is a lazy {@code @ManyToOne} — mirrors
     * {@code ProductReviewServiceImpl}'s identical fix for the same
     * post-transaction serialization failure (#441).
     */
    private static void initializeUser(SellerReview review) {
        Hibernate.initialize(review.getUser());
    }

    @Override
    @Transactional
    public SellerReview createReview(Long sellerId, Long userId,
            Integer rating, String comment, boolean verifiedPurchase) {
        log.info("Creating seller review for seller {} by user {}",
                sellerId, userId);

        if (reviewRepository.existsBySellerIdAndUserId(sellerId, userId)) {
            throw new IllegalStateException(
                    "User has already reviewed this seller");
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller not found with id: " + sellerId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        SellerReview review = SellerReview.builder()
                .seller(seller)
                .user(user)
                .rating(rating)
                .comment(comment)
                .verifiedPurchase(verifiedPurchase)
                .isVisible(true)
                .build();

        SellerReview savedReview = reviewRepository.save(review);
        initializeUser(savedReview);

        log.info("Seller review created with id {} for seller {}",
                savedReview.getId(), sellerId);

        return savedReview;
    }

    @Override
    @Transactional
    public SellerReview updateReview(Long reviewId, Long userId,
            Integer rating, String comment) {
        log.info("Updating seller review {} by user {}", reviewId, userId);

        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        REVIEW_NOT_FOUND_MSG + reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "User is not authorized to update this review");
        }

        review.setRating(rating);
        review.setComment(comment);

        SellerReview updatedReview = reviewRepository.save(review);
        initializeUser(updatedReview);

        log.info("Seller review {} updated with new rating: {}",
                reviewId, rating);

        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting seller review {} by user {}", reviewId, userId);

        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        REVIEW_NOT_FOUND_MSG + reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "User is not authorized to delete this review");
        }

        reviewRepository.delete(review);

        log.info("Seller review {} deleted successfully", reviewId);
    }

    @Override
    public Page<SellerReview> getSellerReviews(Long sellerId,
            Pageable pageable) {
        log.debug("Fetching reviews for seller {}", sellerId);
        Page<SellerReview> reviews = reviewRepository
                .findBySellerIdAndIsVisibleTrue(sellerId, pageable);
        reviews.forEach(SellerReviewServiceImpl::initializeUser);
        return reviews;
    }

    @Override
    public Page<SellerReview> getUserReviews(Long userId, Pageable pageable) {
        log.debug("Fetching reviews by user {}", userId);
        Page<SellerReview> reviews = reviewRepository
                .findByUserId(userId, pageable);
        reviews.forEach(SellerReviewServiceImpl::initializeUser);
        return reviews;
    }

    @Override
    public Double getAverageRating(Long sellerId) {
        log.debug("Calculating average rating for seller {}", sellerId);
        Double avgRating = reviewRepository.calculateAverageRating(sellerId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    public Map<Integer, Long> getRatingDistribution(Long sellerId) {
        log.debug("Fetching rating distribution for seller {}", sellerId);
        return ReviewRatingUtils.buildDistribution(
                reviewRepository.getRatingDistribution(sellerId));
    }

    @Override
    public boolean hasUserPurchasedFromSeller(Long userId, Long sellerId) {
        log.debug("Checking if user {} purchased from seller {}", userId,
                sellerId);
        return orderRepository.findByUserId(userId).stream()
                .anyMatch(order -> order.getOrderItems().stream()
                        .anyMatch(item -> item.getProduct().getSeller()
                                .getId().equals(sellerId)));
    }
}
