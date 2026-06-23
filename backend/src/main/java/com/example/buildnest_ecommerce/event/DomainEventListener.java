package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.service.notification.NotificationService;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.example.buildnest_ecommerce.service.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class DomainEventListener {

    private final NotificationService notificationService;
    private final WebhookService webhookService;
    private final OrderService orderService;

    @Async
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrder().getId());
        payload.put("userId", event.getUserId());
        payload.put("status", event.getOrder().getStatus().toString());
        payload.put("total", event.getOrder().getTotalAmount());

        webhookService.dispatchEvent("order.placed", payload);
        notificationService.sendAlert("Order Placed",
                "Order placed with id " + event.getOrder().getId(),
                "INFO", payload);
    }

    @Async
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessfulEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", event.getPaymentId());
        payload.put("orderId", event.getOrderId());
        payload.put("amount", event.getAmount());

        if (event.getOrderId() != null) {
            try {
                orderService.updateOrderStatus(event.getOrderId(), "CONFIRMED");
            } catch (Exception ex) {
                log.warn("Failed to update order status after payment success", ex);
            }
        }

        webhookService.dispatchEvent("payment.success", payload);
        notificationService.sendAlert("Payment Success",
                "Payment successful for order " + event.getOrderId(),
                "INFO", payload);
    }

    @Async
    @EventListener
    public void handlePaymentFailure(PaymentFailedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("reason", event.getReason());

        webhookService.dispatchEvent("payment.failed", payload);
        notificationService.sendAlert("Payment Failed",
                "Payment failed for order " + event.getOrderId(),
                "WARN", payload);
    }

    @Async
    @EventListener
    public void handleLowStock(LowStockWarningEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", event.getProductId());
        payload.put("productName", event.getProductName());
        payload.put("currentStock", event.getCurrentStock());
        payload.put("minimumStock", event.getMinimumStock());

        webhookService.dispatchEvent("inventory.low_stock", payload);
        notificationService.sendAlert("Low Stock Warning",
                "Low stock for product " + event.getProductName(),
                "WARN", payload);
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.getUserId());
        payload.put("email", event.getEmail());

        webhookService.dispatchEvent("user.registered", payload);
    }

    @Async
    @EventListener
    public void handleOrderStatusChange(OrderStatusChangedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("previousStatus", event.getPreviousStatus());
        payload.put("newStatus", event.getNewStatus());

        webhookService.dispatchEvent("order.status.changed", payload);
    }
}
