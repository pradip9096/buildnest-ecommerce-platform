package com.example.buildnest_ecommerce.service.analytics;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service interface for recording and querying user behaviour analytics
 * events (ANL-02, #65).
 */
public interface UserEventService {

    /**
     * Record a product page view. Fire-and-forget — never blocks the calling
     * request thread.
     *
     * @param userId    the viewing user's id, or {@code null} for an
     *                  unauthenticated/anonymous view
     * @param productId the viewed product's id
     */
    void recordProductView(Long userId, Long productId);

    /**
     * Record an add-to-cart action. Fire-and-forget.
     *
     * @param userId    the acting user's id
     * @param productId the added product's id
     * @param quantity  the quantity added
     */
    void recordAddToCart(Long userId, Long productId, Integer quantity);

    /**
     * Record the start of the checkout flow. Fire-and-forget.
     *
     * @param userId the checking-out user's id
     */
    void recordCheckoutStarted(Long userId);

    /**
     * Aggregate behaviour metrics for the given time window: page views per
     * product, cart abandonment rate, and conversion-funnel stage counts.
     *
     * @param start window start (inclusive)
     * @param end   window end (inclusive)
     * @return an aggregated metrics map; an empty-but-present shape if
     *         Elasticsearch is unavailable (graceful degradation, never
     *         throws)
     */
    Map<String, Object> getBehaviorMetrics(LocalDateTime start, LocalDateTime end);
}
