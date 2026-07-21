package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductVariant entity tests")
class ProductVariantTest {

    @Test
    @DisplayName("Should set and get ProductVariant fields")
    void testSettersAndGetters() {
        Product product = new Product();
        product.setId(1L);

        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setSku("SKU-001");
        variant.setSize("M");
        variant.setColour("Red");
        variant.setPriceAdjustment(BigDecimal.valueOf(5.00));
        variant.setIsActive(true);

        assertEquals(10L, variant.getId());
        assertEquals(product, variant.getProduct());
        assertEquals("SKU-001", variant.getSku());
        assertEquals("M", variant.getSize());
        assertEquals("Red", variant.getColour());
        assertEquals(BigDecimal.valueOf(5.00), variant.getPriceAdjustment());
        assertTrue(variant.getIsActive());
    }

    /**
     * Regression test for #482 (latent LazyInitializationException risk, same
     * defect class as #426's ProductImage.product fix): AdminProductController's
     * variant endpoints return raw ProductVariant entities. Without @JsonIgnore
     * on the back-reference, Jackson walks into Product's own lazy fields (e.g.
     * tags) once the loading transaction has closed (open-in-view=false) and
     * throws. This test verifies the structural fix directly — no lazy proxy or
     * transaction needed, since @JsonIgnore is a static annotation Jackson honors
     * regardless of initialization state — so it can't pass "by accident" the
     * way a timing-dependent test could.
     */
    @Test
    @DisplayName("Should never serialize the back-reference to Product")
    void testProductFieldIsNotSerialized() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Should never appear in variant JSON");
        product.setPrice(BigDecimal.valueOf(100.00));

        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setSku("SKU-001");
        variant.setPriceAdjustment(BigDecimal.ZERO);
        variant.setIsActive(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = mapper.writeValueAsString(variant);

        assertFalse(json.contains("\"product\""), "Serialized ProductVariant must not include the product field: " + json);
        assertTrue(json.contains("\"sku\""));
    }
}
