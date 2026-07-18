package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductImage entity tests")
class ProductImageTest {

    @Test
    @DisplayName("Should set and get ProductImage fields")
    void testSettersAndGetters() {
        Product product = new Product();
        product.setId(1L);
        LocalDateTime now = LocalDateTime.now();

        ProductImage image = new ProductImage();
        image.setId(10L);
        image.setProduct(product);
        image.setImageUrl("/uploads/photo.jpg");
        image.setAltText("A photo");
        image.setDisplayOrder(2);
        image.setIsPrimary(true);
        image.setCreatedAt(now);

        assertEquals(10L, image.getId());
        assertEquals(product, image.getProduct());
        assertEquals("/uploads/photo.jpg", image.getImageUrl());
        assertEquals("A photo", image.getAltText());
        assertEquals(2, image.getDisplayOrder());
        assertTrue(image.getIsPrimary());
        assertEquals(now, image.getCreatedAt());
    }

    @Test
    @DisplayName("Should default isPrimary to false and displayOrder to 0")
    void testDefaults() {
        ProductImage image = new ProductImage();
        assertFalse(image.getIsPrimary());
        assertEquals(0, image.getDisplayOrder());
    }

    /**
     * Regression test for the LazyInitializationException found live-verifying #426:
     * AdminProductController returns raw ProductImage entities. Without @JsonIgnore on
     * the back-reference, Jackson walks into Product's own lazy fields (e.g. tags) once
     * the loading transaction has closed (open-in-view=false) and throws. This test
     * verifies the structural fix directly — no lazy proxy or transaction needed, since
     * @JsonIgnore is a static annotation Jackson honors regardless of initialization
     * state — so it can't pass "by accident" the way a timing-dependent test could.
     */
    @Test
    @DisplayName("Should never serialize the back-reference to Product")
    void testProductFieldIsNotSerialized() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Should never appear in image JSON");

        ProductImage image = new ProductImage();
        image.setId(10L);
        image.setProduct(product);
        image.setImageUrl("/uploads/photo.jpg");
        image.setDisplayOrder(0);
        image.setIsPrimary(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = mapper.writeValueAsString(image);

        assertFalse(json.contains("\"product\""), "Serialized ProductImage must not include the product field: " + json);
        assertTrue(json.contains("\"imageUrl\""));
    }
}
