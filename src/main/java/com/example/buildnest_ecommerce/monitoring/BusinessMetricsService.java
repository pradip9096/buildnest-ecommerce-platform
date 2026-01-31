package com.example.buildnest_ecommerce.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Business Metrics Service - Collects and exposes business-level metrics
 * for production monitoring and observability (7.3 Monitoring &amp;
 * Observability)
 * 
 * Metrics exposed to Prometheus at /actuator/prometheus
 * Dashboard: http://grafana:3000/d/buildnest-ecommerce
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    // Order Metrics
    private Counter ordersCreatedCounter;
    private Counter ordersCompletedCounter;
    private Counter ordersCancelledCounter;
    private Timer orderProcessingTimer;

    // Product Metrics
    private Counter productsViewedCounter;
    private Counter productsSearchedCounter;

    // Payment Metrics
    private Counter paymentsProcessedCounter;
    private Counter paymentsFailedCounter;
    private Timer paymentProcessingTimer;

    // Cart Metrics
    private Counter cartItemsAddedCounter;
    private Counter cartItemsRemovedCounter;

    /**
     * Initialize all metric counters and timers on startup
     */
    public void initializeMetrics() {
        // Order Metrics
        ordersCreatedCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .tag("event", "order_creation")
                .register(meterRegistry);

        ordersCompletedCounter = Counter.builder("orders.completed")
                .description("Total number of orders completed successfully")
                .tag("event", "order_completion")
                .register(meterRegistry);

        ordersCancelledCounter = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("event", "order_cancellation")
                .register(meterRegistry);

        orderProcessingTimer = Timer.builder("orders.processing.time")
                .description("Time taken to process orders")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Product Metrics
        productsViewedCounter = Counter.builder("products.viewed")
                .description("Total number of product views")
                .tag("event", "product_view")
                .register(meterRegistry);

        productsSearchedCounter = Counter.builder("products.searched")
                .description("Total number of product searches")
                .tag("event", "product_search")
                .register(meterRegistry);

        // Payment Metrics
        paymentsProcessedCounter = Counter.builder("payments.processed")
                .description("Total number of successful payments")
                .tag("event", "payment_success")
                .register(meterRegistry);

        paymentsFailedCounter = Counter.builder("payments.failed")
                .description("Total number of failed payments")
                .tag("event", "payment_failure")
                .register(meterRegistry);

        paymentProcessingTimer = Timer.builder("payments.processing.time")
                .description("Time taken to process payments")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Cart Metrics
        cartItemsAddedCounter = Counter.builder("cart.items.added")
                .description("Total number of items added to carts")
                .tag("event", "cart_add")
                .register(meterRegistry);

        cartItemsRemovedCounter = Counter.builder("cart.items.removed")
                .description("Total number of items removed from carts")
                .tag("event", "cart_remove")
                .register(meterRegistry);

        log.info("✅ Business metrics initialized and ready for collection");
    }

    /**
     * Record order creation event
     */
    public void recordOrderCreated() {
        ordersCreatedCounter.increment();
    }

    /**
     * Record order completion event
     */
    public void recordOrderCompleted() {
        ordersCompletedCounter.increment();
    }

    /**
     * Record order cancellation event
     */
    public void recordOrderCancelled() {
        ordersCancelledCounter.increment();
    }

    /**
     * Record order processing time
     */
    public void recordOrderProcessingTime(long duration, TimeUnit timeUnit) {
        orderProcessingTimer.record(duration, timeUnit);
    }

    /**
     * Record product view event
     */
    public void recordProductViewed() {
        productsViewedCounter.increment();
    }

    /**
     * Record product search event
     */
    public void recordProductSearched() {
        productsSearchedCounter.increment();
    }

    /**
     * Record successful payment
     */
    public void recordPaymentProcessed() {
        paymentsProcessedCounter.increment();
    }

    /**
     * Record failed payment
     */
    public void recordPaymentFailed() {
        paymentsFailedCounter.increment();
    }

    /**
     * Record payment processing time
     */
    public void recordPaymentProcessingTime(long duration, TimeUnit timeUnit) {
        paymentProcessingTimer.record(duration, timeUnit);
    }

    /**
     * Record cart item addition
     */
    public void recordCartItemAdded() {
        cartItemsAddedCounter.increment();
    }

    /**
     * Record cart item removal
     */
    public void recordCartItemRemoved() {
        cartItemsRemovedCounter.increment();
    }
}
