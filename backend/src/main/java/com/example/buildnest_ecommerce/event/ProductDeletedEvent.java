package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;

/** Fired after a product is soft-deleted (SRCH-02, #75). */
public class ProductDeletedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long productId;

    public ProductDeletedEvent(Object source, Long productId) {
        super(source);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
