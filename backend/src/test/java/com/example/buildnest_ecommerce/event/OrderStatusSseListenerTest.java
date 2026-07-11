package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.service.notification.SseNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@DisplayName("OrderStatusSseListener tests (NOTIF-02, #63)")
class OrderStatusSseListenerTest {

    @Test
    @DisplayName("handleOrderStatusChange forwards the event to the SSE service, unconditionally of any feature flag")
    void handleOrderStatusChangeForwardsToSseService() {
        SseNotificationService sseNotificationService = mock(SseNotificationService.class);
        OrderStatusSseListener listener = new OrderStatusSseListener(sseNotificationService);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(this, 5L, 42L, "PENDING", "CONFIRMED");
        listener.handleOrderStatusChange(event);

        verify(sseNotificationService).sendOrderStatusUpdate(42L, 5L, "PENDING", "CONFIRMED");
    }
}
