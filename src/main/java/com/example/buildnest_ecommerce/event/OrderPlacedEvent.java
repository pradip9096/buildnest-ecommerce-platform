package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.model.entity.Order;
import org.springframework.context.ApplicationEvent;

/**
 * 4.3 MEDIUM - Event-Driven Architecture
 * Domain events for key business operations
 */

/**
 * Fired when an order is successfully placed
 */
public class OrderPlacedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient Order order;
    private final Long userId;

    public OrderPlacedEvent(Object source, Order order, Long userId) {
        super(source);
        this.order = order;
        this.userId = userId;
    }

    public Order getOrder() {
        return order;
    }

    public Long getUserId() {
        return userId;
    }
}
