package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.service.notification.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Pushes order-status-change events to a user's live SSE connection
 * (NOTIF-02, #63).
 *
 * Deliberately a separate listener from {@link DomainEventListener}, which is
 * gated behind {@code elasticsearch.enabled} — in-app notifications are a
 * user-facing feature, not observability infrastructure, and must keep
 * working regardless of that flag.
 */
@Component
@RequiredArgsConstructor
public class OrderStatusSseListener {

    private final SseNotificationService sseNotificationService;

    @Async
    @EventListener
    public void handleOrderStatusChange(OrderStatusChangedEvent event) {
        sseNotificationService.sendOrderStatusUpdate(
                event.getUserId(), event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
    }
}
