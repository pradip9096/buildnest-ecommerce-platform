package com.example.buildnest_ecommerce.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BusinessMetricsServiceTest {

    @Test
    void initializesAndRecordsMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BusinessMetricsService service = new BusinessMetricsService(registry);

        service.initializeMetrics();

        service.recordOrderCreated();
        service.recordOrderCompleted();
        service.recordOrderCancelled();
        service.recordOrderProcessingTime(10, TimeUnit.MILLISECONDS);
        service.recordProductViewed();
        service.recordProductSearched();
        service.recordPaymentProcessed();
        service.recordPaymentFailed();
        service.recordPaymentProcessingTime(20, TimeUnit.MILLISECONDS);
        service.recordCartItemAdded();
        service.recordCartItemRemoved();

        assertEquals(1.0, registry.counter("orders.created", "event", "order_creation").count());
        assertEquals(1.0, registry.counter("orders.completed", "event", "order_completion").count());
        assertEquals(1.0, registry.counter("orders.cancelled", "event", "order_cancellation").count());
        assertEquals(1.0, registry.counter("products.viewed", "event", "product_view").count());
        assertEquals(1.0, registry.counter("products.searched", "event", "product_search").count());
        assertEquals(1.0, registry.counter("payments.processed", "event", "payment_success").count());
        assertEquals(1.0, registry.counter("payments.failed", "event", "payment_failure").count());
        assertEquals(1.0, registry.counter("cart.items.added", "event", "cart_add").count());
        assertEquals(1.0, registry.counter("cart.items.removed", "event", "cart_remove").count());
    }
}
