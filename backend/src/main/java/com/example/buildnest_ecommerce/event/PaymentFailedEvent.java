package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;

/**
 * Fired when payment processing fails.
 */
public class PaymentFailedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long orderId;
    private final String reason;

    public PaymentFailedEvent(Object source, Long orderId, String reason) {
        super(source);
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }
}
