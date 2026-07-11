package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.context.ApplicationEvent;

/** Fired after a product is persisted for the first time (SRCH-02, #75). */
public class ProductCreatedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient Product product;

    public ProductCreatedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
