package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.context.ApplicationEvent;

/** Fired after a product is updated (SRCH-02, #75). */
public class ProductUpdatedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient Product product;

    public ProductUpdatedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
