package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;
import java.math.BigDecimal;

/**
 * Fired when payment is successfully processed.
 */
public class PaymentSuccessfulEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long paymentId;
    private final Long orderId;
    private final BigDecimal amount;

    public PaymentSuccessfulEvent(Object source, Long paymentId, Long orderId, BigDecimal amount) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
