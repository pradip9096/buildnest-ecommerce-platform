package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;

/**
 * Fired when an order status changes.
 */
public class OrderStatusChangedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long orderId;
    private final String previousStatus;
    private final String newStatus;

    public OrderStatusChangedEvent(Object source, Long orderId, String previousStatus, String newStatus) {
        super(source);
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
