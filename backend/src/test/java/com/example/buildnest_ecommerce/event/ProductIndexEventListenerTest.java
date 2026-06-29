package com.example.buildnest_ecommerce.event;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ProductIndexEventListener} (SRCH-02, #75).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductIndexEventListener unit tests")
class ProductIndexEventListenerTest {

    @Mock private ProductSearchService productSearchService;

    private ProductIndexEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProductIndexEventListener(productSearchService);
    }

    @Test
    @DisplayName("ProductCreatedEvent — calls indexProduct")
    void productCreated_callsIndexProduct() {
        Product product = new Product(); product.setId(1L); product.setName("cement");
        listener.handleProductCreated(new ProductCreatedEvent(this, product));
        verify(productSearchService).indexProduct(product);
    }

    @Test
    @DisplayName("ProductUpdatedEvent — calls indexProduct")
    void productUpdated_callsIndexProduct() {
        Product product = new Product(); product.setId(2L); product.setName("tile");
        listener.handleProductUpdated(new ProductUpdatedEvent(this, product));
        verify(productSearchService).indexProduct(product);
    }

    @Test
    @DisplayName("ProductDeletedEvent — calls deleteFromIndex")
    void productDeleted_callsDeleteFromIndex() {
        listener.handleProductDeleted(new ProductDeletedEvent(this, 99L));
        verify(productSearchService).deleteFromIndex(99L);
    }
}
