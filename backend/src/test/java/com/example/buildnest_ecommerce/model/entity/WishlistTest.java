package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WishlistTest {

    @Test
    void addRemoveAndClearProducts() {
        Wishlist wishlist = new Wishlist();
        Product product = new Product();
        product.setId(1L);

        wishlist.addProduct(product);
        assertTrue(wishlist.containsProduct(product));
        assertEquals(1, wishlist.getProductCount());

        wishlist.removeProduct(product);
        assertFalse(wishlist.containsProduct(product));
        assertEquals(0, wishlist.getProductCount());

        wishlist.addProduct(product);
        wishlist.clearProducts();
        assertEquals(0, wishlist.getProductCount());
    }

    @Test
    void onCreateAndUpdateSetTimestamps() {
        Wishlist wishlist = new Wishlist();
        assertNull(wishlist.getCreatedAt());

        wishlist.onCreate();
        LocalDateTime createdAt = wishlist.getCreatedAt();
        assertNotNull(createdAt);
        assertNotNull(wishlist.getUpdatedAt());

        wishlist.onUpdate();
        assertNotNull(wishlist.getUpdatedAt());
    }
}
