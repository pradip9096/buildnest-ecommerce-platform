package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTagTest {

    @Test
    @DisplayName("Should create ProductTag with no-args constructor and setters")
    void testNoArgsConstructorAndSetters() {
        ProductTag tag = new ProductTag();
        LocalDateTime now = LocalDateTime.now();

        tag.setId(1L);
        tag.setName("Eco-Friendly");
        tag.setSlug("eco-friendly");
        tag.setCreatedAt(now);

        assertEquals(1L, tag.getId());
        assertEquals("Eco-Friendly", tag.getName());
        assertEquals("eco-friendly", tag.getSlug());
        assertEquals(now, tag.getCreatedAt());
    }

    @Test
    @DisplayName("Should default createdAt to a non-null value")
    void testDefaultCreatedAt() {
        ProductTag tag = new ProductTag();
        assertNotNull(tag.getCreatedAt());
    }

    @Test
    @DisplayName("Equal tags with the same id/name/slug should be equal")
    void testEquals() {
        ProductTag tag1 = new ProductTag();
        tag1.setId(1L);
        tag1.setName("Best Seller");
        tag1.setSlug("best-seller");

        ProductTag tag2 = new ProductTag();
        tag2.setId(1L);
        tag2.setName("Best Seller");
        tag2.setSlug("best-seller");

        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }
}
