package com.example.buildnest_ecommerce.service.analytics;

import com.example.buildnest_ecommerce.model.elasticsearch.UserBehaviorEvent;
import com.example.buildnest_ecommerce.repository.elasticsearch.UserBehaviorEventRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventServiceImpl tests (ANL-02, #65)")
class UserEventServiceImplTest {

    @Mock
    private UserBehaviorEventRepository repository;

    @Captor
    private ArgumentCaptor<UserBehaviorEvent> eventCaptor;

    private UserEventServiceImpl service;
    private CircuitBreaker closedCircuitBreaker;

    @BeforeEach
    void setUp() {
        closedCircuitBreaker = CircuitBreaker.ofDefaults("user-event-circuit-breaker-test");
        service = new UserEventServiceImpl(repository, closedCircuitBreaker);
    }

    private UserEventServiceImpl withOpenCircuitBreaker() {
        CircuitBreaker openCb = CircuitBreaker.of("open-cb-test",
                CircuitBreakerConfig.custom()
                        .minimumNumberOfCalls(1)
                        .failureRateThreshold(1)
                        .build());
        openCb.transitionToOpenState();
        return new UserEventServiceImpl(repository, openCb);
    }

    @Test
    @DisplayName("recordProductView saves a PRODUCT_VIEW event with the given user and product")
    void recordProductViewSavesEvent() {
        service.recordProductView(7L, 42L);

        verify(repository).save(eventCaptor.capture());
        UserBehaviorEvent saved = eventCaptor.getValue();
        assertEquals("PRODUCT_VIEW", saved.getEventType());
        assertEquals(7L, saved.getUserId());
        assertEquals(42L, saved.getProductId());
        assertNotNull(saved.getId());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    @DisplayName("recordProductView tolerates a null userId for anonymous views")
    void recordProductViewToleratesAnonymousUser() {
        service.recordProductView(null, 42L);

        verify(repository).save(eventCaptor.capture());
        assertNull(eventCaptor.getValue().getUserId());
        assertEquals(42L, eventCaptor.getValue().getProductId());
    }

    @Test
    @DisplayName("recordAddToCart saves an ADD_TO_CART event with quantity metadata")
    void recordAddToCartSavesEventWithQuantity() {
        service.recordAddToCart(7L, 42L, 3);

        verify(repository).save(eventCaptor.capture());
        UserBehaviorEvent saved = eventCaptor.getValue();
        assertEquals("ADD_TO_CART", saved.getEventType());
        assertEquals(3, saved.getMetadata().get("quantity"));
    }

    @Test
    @DisplayName("recordCheckoutStarted saves a CHECKOUT_STARTED event with no productId")
    void recordCheckoutStartedSavesEvent() {
        service.recordCheckoutStarted(7L);

        verify(repository).save(eventCaptor.capture());
        UserBehaviorEvent saved = eventCaptor.getValue();
        assertEquals("CHECKOUT_STARTED", saved.getEventType());
        assertEquals(7L, saved.getUserId());
        assertNull(saved.getProductId());
    }

    @Test
    @DisplayName("record methods do not throw when the circuit breaker is open — graceful degradation")
    void recordMethodsDegradeGracefullyWhenCircuitBreakerOpen() {
        UserEventServiceImpl openService = withOpenCircuitBreaker();

        assertDoesNotThrow(() -> openService.recordProductView(1L, 2L));
        assertDoesNotThrow(() -> openService.recordAddToCart(1L, 2L, 1));
        assertDoesNotThrow(() -> openService.recordCheckoutStarted(1L));
        verifyNoInteractions(repository);
    }

    private UserBehaviorEvent event(String type, Long productId) {
        return UserBehaviorEvent.builder()
                .id("id")
                .eventType(type)
                .productId(productId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getBehaviorMetrics computes page views per product from PRODUCT_VIEW events")
    void getBehaviorMetricsComputesPageViewsPerProduct() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(repository.findByEventTypeAndTimestampBetween("PRODUCT_VIEW", start, end))
                .thenReturn(List.of(event("PRODUCT_VIEW", 1L), event("PRODUCT_VIEW", 1L), event("PRODUCT_VIEW", 2L)));
        when(repository.findByEventTypeAndTimestampBetween("ADD_TO_CART", start, end)).thenReturn(List.of());
        when(repository.findByEventTypeAndTimestampBetween("CHECKOUT_STARTED", start, end)).thenReturn(List.of());

        Map<String, Object> metrics = service.getBehaviorMetrics(start, end);

        @SuppressWarnings("unchecked")
        Map<Long, Long> pageViews = (Map<Long, Long>) metrics.get("pageViewsPerProduct");
        assertEquals(2L, pageViews.get(1L));
        assertEquals(1L, pageViews.get(2L));
    }

    @Test
    @DisplayName("getBehaviorMetrics computes cart abandonment rate as (addToCart - checkoutStarted) / addToCart")
    void getBehaviorMetricsComputesCartAbandonmentRate() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(repository.findByEventTypeAndTimestampBetween("PRODUCT_VIEW", start, end)).thenReturn(List.of());
        when(repository.findByEventTypeAndTimestampBetween("ADD_TO_CART", start, end))
                .thenReturn(List.of(event("ADD_TO_CART", 1L), event("ADD_TO_CART", 2L),
                        event("ADD_TO_CART", 3L), event("ADD_TO_CART", 4L)));
        when(repository.findByEventTypeAndTimestampBetween("CHECKOUT_STARTED", start, end))
                .thenReturn(List.of(event("CHECKOUT_STARTED", null)));

        Map<String, Object> metrics = service.getBehaviorMetrics(start, end);

        assertEquals(75.0, (double) metrics.get("cartAbandonmentRate"));
    }

    @Test
    @DisplayName("getBehaviorMetrics reports 0% abandonment when there were no add-to-cart events (no division by zero)")
    void getBehaviorMetricsAbandonmentRateZeroWhenNoAddToCart() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(repository.findByEventTypeAndTimestampBetween(anyString(), eq(start), eq(end))).thenReturn(List.of());

        Map<String, Object> metrics = service.getBehaviorMetrics(start, end);

        assertEquals(0.0, (double) metrics.get("cartAbandonmentRate"));
    }

    @Test
    @DisplayName("getBehaviorMetrics computes conversion funnel stage counts and rates")
    void getBehaviorMetricsComputesConversionFunnel() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(repository.findByEventTypeAndTimestampBetween("PRODUCT_VIEW", start, end))
                .thenReturn(List.of(event("PRODUCT_VIEW", 1L), event("PRODUCT_VIEW", 1L),
                        event("PRODUCT_VIEW", 1L), event("PRODUCT_VIEW", 1L)));
        when(repository.findByEventTypeAndTimestampBetween("ADD_TO_CART", start, end))
                .thenReturn(List.of(event("ADD_TO_CART", 1L), event("ADD_TO_CART", 1L)));
        when(repository.findByEventTypeAndTimestampBetween("CHECKOUT_STARTED", start, end))
                .thenReturn(List.of(event("CHECKOUT_STARTED", null)));

        Map<String, Object> metrics = service.getBehaviorMetrics(start, end);

        @SuppressWarnings("unchecked")
        Map<String, Object> funnel = (Map<String, Object>) metrics.get("conversionFunnel");
        assertEquals(4, funnel.get("productViews"));
        assertEquals(2, funnel.get("addToCart"));
        assertEquals(1, funnel.get("checkoutStarted"));
        assertEquals(50.0, (double) funnel.get("viewToCartRate"));
        assertEquals(50.0, (double) funnel.get("cartToCheckoutRate"));
    }

    @Test
    @DisplayName("getBehaviorMetrics returns an empty-but-present shape when the circuit breaker is open")
    void getBehaviorMetricsDegradesGracefullyWhenCircuitBreakerOpen() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        Map<String, Object> metrics = withOpenCircuitBreaker().getBehaviorMetrics(start, end);

        assertEquals(Map.of(), metrics.get("pageViewsPerProduct"));
        assertEquals(0.0, (double) metrics.get("cartAbandonmentRate"));
        verifyNoInteractions(repository);
    }
}
