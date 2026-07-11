package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.elasticsearch.UserBehaviorEvent;
import com.example.buildnest_ecommerce.repository.elasticsearch.UserBehaviorEventRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Elasticsearch-backed implementation of {@link UserEventService} (ANL-02,
 * #65). Only registered when {@code elasticsearch.enabled=true} — this
 * service is genuinely Elasticsearch-only, unlike the classes fixed in #345.
 *
 * Domain-owned direct repository access (mirrors {@code ProductSearchServiceImpl}),
 * not routed through the shared {@code ElasticsearchIngestionService} — this
 * is a distinct domain (user behaviour, not audit/metrics), and its
 * aggregation queries don't fit a generic shared ingestion service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventServiceImpl implements UserEventService {

    private static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    private static final String ADD_TO_CART = "ADD_TO_CART";
    private static final String CHECKOUT_STARTED = "CHECKOUT_STARTED";

    private final UserBehaviorEventRepository repository;
    private final CircuitBreaker elasticsearchCircuitBreaker;

    @Override
    @Async
    public void recordProductView(Long userId, Long productId) {
        record(PRODUCT_VIEW, userId, productId, null);
    }

    @Override
    @Async
    public void recordAddToCart(Long userId, Long productId, Integer quantity) {
        Map<String, Object> metadata = quantity == null ? null : Map.of("quantity", quantity);
        record(ADD_TO_CART, userId, productId, metadata);
    }

    @Override
    @Async
    public void recordCheckoutStarted(Long userId) {
        record(CHECKOUT_STARTED, userId, null, null);
    }

    private void record(String eventType, Long userId, Long productId, Map<String, Object> metadata) {
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .productId(productId)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();

        try {
            elasticsearchCircuitBreaker.executeRunnable(() -> repository.save(event));
            log.debug("Recorded {} event for user {}", eventType, userId);
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, skipping {} event ingestion", eventType);
        } catch (Exception e) {
            log.error("Failed to record {} event", eventType, e);
        }
    }

    @Override
    public Map<String, Object> getBehaviorMetrics(LocalDateTime start, LocalDateTime end) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(() -> buildMetrics(start, end));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty behavior metrics");
            return emptyMetrics(start, end);
        } catch (Exception e) {
            log.error("Failed to compute behavior metrics", e);
            return emptyMetrics(start, end);
        }
    }

    private Map<String, Object> buildMetrics(LocalDateTime start, LocalDateTime end) {
        List<UserBehaviorEvent> views = repository.findByEventTypeAndTimestampBetween(PRODUCT_VIEW, start, end);
        List<UserBehaviorEvent> addToCarts = repository.findByEventTypeAndTimestampBetween(ADD_TO_CART, start, end);
        List<UserBehaviorEvent> checkoutsStarted = repository.findByEventTypeAndTimestampBetween(CHECKOUT_STARTED,
                start, end);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("period", Map.of("start", start, "end", end));
        metrics.put("pageViewsPerProduct", pageViewsPerProduct(views));
        metrics.put("cartAbandonmentRate", cartAbandonmentRate(addToCarts.size(), checkoutsStarted.size()));
        metrics.put("conversionFunnel", conversionFunnel(views.size(), addToCarts.size(), checkoutsStarted.size()));
        return metrics;
    }

    private Map<Long, Long> pageViewsPerProduct(List<UserBehaviorEvent> views) {
        return views.stream()
                .filter(e -> e.getProductId() != null)
                .collect(Collectors.groupingBy(UserBehaviorEvent::getProductId, Collectors.counting()));
    }

    /**
     * Cart abandonment rate: the share of add-to-cart actions that did not
     * proceed to a checkout-started event, within the same window. A
     * simplified, session-independent approximation — not a per-cart trace.
     */
    private double cartAbandonmentRate(int addToCartCount, int checkoutStartedCount) {
        if (addToCartCount == 0) {
            return 0.0;
        }
        int abandoned = Math.max(0, addToCartCount - checkoutStartedCount);
        return (abandoned * 100.0) / addToCartCount;
    }

    private Map<String, Object> conversionFunnel(int views, int addToCarts, int checkoutsStarted) {
        Map<String, Object> funnel = new HashMap<>();
        funnel.put("productViews", views);
        funnel.put("addToCart", addToCarts);
        funnel.put("checkoutStarted", checkoutsStarted);
        funnel.put("viewToCartRate", views == 0 ? 0.0 : (addToCarts * 100.0) / views);
        funnel.put("cartToCheckoutRate", addToCarts == 0 ? 0.0 : (checkoutsStarted * 100.0) / addToCarts);
        return funnel;
    }

    private Map<String, Object> emptyMetrics(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("period", Map.of("start", start, "end", end));
        metrics.put("pageViewsPerProduct", Map.of());
        metrics.put("cartAbandonmentRate", 0.0);
        metrics.put("conversionFunnel", Map.of("productViews", 0, "addToCart", 0, "checkoutStarted", 0,
                "viewToCartRate", 0.0, "cartToCheckoutRate", 0.0));
        return metrics;
    }
}
