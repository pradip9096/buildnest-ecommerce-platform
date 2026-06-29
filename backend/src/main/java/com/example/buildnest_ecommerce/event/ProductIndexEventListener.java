package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Keeps the Elasticsearch product index in sync with the JPA data store (SRCH-02, #75).
 *
 * Conditional on {@code elasticsearch.enabled=true} — mirrors the guard used by
 * {@link DomainEventListener} so the listener is absent in test and local profiles
 * where Elasticsearch is disabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public class ProductIndexEventListener {

    private final ProductSearchService productSearchService;

    @Async
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        log.debug("Indexing new product {}", event.getProduct().getId());
        productSearchService.indexProduct(event.getProduct());
    }

    @Async
    @EventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.debug("Re-indexing updated product {}", event.getProduct().getId());
        productSearchService.indexProduct(event.getProduct());
    }

    @Async
    @EventListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.debug("Removing deleted product {} from index", event.getProductId());
        productSearchService.deleteFromIndex(event.getProductId());
    }
}
