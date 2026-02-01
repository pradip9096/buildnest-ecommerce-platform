package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.example.buildnest_ecommerce.service.webhook.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private WebhookService webhookService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private DomainEventListener domainEventListener;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100.00"));
    }

    @Test
    void handleOrderPlacedShouldDispatchWebhookAndSendNotification() {
        OrderPlacedEvent event = new OrderPlacedEvent(this, testOrder, 123L);

        domainEventListener.handleOrderPlaced(event);

        verify(webhookService).dispatchEvent(eq("order.placed"), anyMap());
        verify(notificationService).sendAlert(eq("Order Placed"), anyString(), eq("INFO"), anyMap());
    }

    @Test
    void handlePaymentSuccessShouldUpdateOrderStatusAndDispatchEvents() {
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(this, 1L, 1L, new BigDecimal("100.00"));

        domainEventListener.handlePaymentSuccess(event);

        verify(orderService).updateOrderStatus(1L, "CONFIRMED");
        verify(webhookService).dispatchEvent(eq("payment.success"), anyMap());
        verify(notificationService).sendAlert(eq("Payment Success"), anyString(), eq("INFO"), anyMap());
    }

    @Test
    void handlePaymentSuccessWithNullOrderIdShouldNotUpdateOrderStatus() {
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(this, 1L, null, new BigDecimal("100.00"));

        domainEventListener.handlePaymentSuccess(event);

        verify(orderService, never()).updateOrderStatus(anyLong(), anyString());
        verify(webhookService).dispatchEvent(eq("payment.success"), anyMap());
        verify(notificationService).sendAlert(eq("Payment Success"), anyString(), eq("INFO"), anyMap());
    }

    @Test
    void handlePaymentSuccessWithExceptionShouldContinueExecution() {
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(this, 1L, 1L, new BigDecimal("100.00"));
        doThrow(new RuntimeException("Order update failed")).when(orderService).updateOrderStatus(anyLong(),
                anyString());

        domainEventListener.handlePaymentSuccess(event);

        verify(webhookService).dispatchEvent(eq("payment.success"), anyMap());
        verify(notificationService).sendAlert(eq("Payment Success"), anyString(), eq("INFO"), anyMap());
    }

    @Test
    void handlePaymentFailureShouldDispatchWebhookAndSendNotification() {
        PaymentFailedEvent event = new PaymentFailedEvent(this, 1L, "INSUFFICIENT_FUNDS");

        domainEventListener.handlePaymentFailure(event);

        verify(webhookService).dispatchEvent(eq("payment.failed"), anyMap());
        verify(notificationService).sendAlert(eq("Payment Failed"), anyString(), eq("WARN"), anyMap());
    }

    @Test
    void handleLowStockShouldDispatchWebhookAndSendNotification() {
        LowStockWarningEvent event = new LowStockWarningEvent(this, 1L, "Product A", 5, 10);

        domainEventListener.handleLowStock(event);

        verify(webhookService).dispatchEvent(eq("inventory.low_stock"), anyMap());
        verify(notificationService).sendAlert(eq("Low Stock Warning"), anyString(), eq("WARN"), anyMap());
    }

    @Test
    void handleUserRegisteredShouldDispatchWebhook() {
        UserRegisteredEvent event = new UserRegisteredEvent(this, 1L, "user@example.com");

        domainEventListener.handleUserRegistered(event);

        verify(webhookService).dispatchEvent(eq("user.registered"), anyMap());
    }

    @Test
    void handleOrderStatusChangeShouldDispatchWebhook() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(this, 1L, "PENDING", "CONFIRMED");

        domainEventListener.handleOrderStatusChange(event);

        verify(webhookService).dispatchEvent(eq("order.status.changed"), anyMap());
    }
}
