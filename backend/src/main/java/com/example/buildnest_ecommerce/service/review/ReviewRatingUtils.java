package com.example.buildnest_ecommerce.service.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared rating-distribution map-building logic, extracted from
 * {@code ProductReviewServiceImpl}/{@code SellerReviewServiceImpl} — both
 * built an identical 1-5 zero-initialized map from a
 * {@code [rating, count]} row list (#558, resolving a real SonarCloud
 * new-code duplication gate failure between the two sibling services).
 */
public final class ReviewRatingUtils {

    private ReviewRatingUtils() {
    }

    public static Map<Integer, Long> buildDistribution(
            List<Object[]> rows) {
        Map<Integer, Long> ratingMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(i, 0L);
        }
        for (Object[] row : rows) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingMap.put(rating, count);
        }
        return ratingMap;
    }
}
