package com.example.buildnest_ecommerce.service.notification;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service interface for in-app Server-Sent Events notifications (NOTIF-02, #63).
 * Defines the contract for registering a user's live connection and pushing
 * order-status-change events to it.
 */
public interface SseNotificationService {

    /**
     * Register a new SSE connection for a user.
     *
     * @param userId the authenticated user's id
     * @return the emitter the caller's controller method should return to keep
     *         the connection open
     */
    SseEmitter register(Long userId);

    /**
     * Push an order status change to every live connection registered for the
     * given user. A no-op if the user has no open connection.
     *
     * @param userId         the order owner's user id
     * @param orderId        the order whose status changed
     * @param previousStatus the status before the change
     * @param newStatus      the status after the change
     */
    void sendOrderStatusUpdate(Long userId, Long orderId, String previousStatus, String newStatus);
}
