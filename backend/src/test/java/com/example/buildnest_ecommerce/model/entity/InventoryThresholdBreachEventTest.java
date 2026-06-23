package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryThresholdBreachEventTest {

    @Test
    void builderDefaultsAndEqualityWork() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setQuantityInStock(5);
        inventory.setMinimumStockLevel(3);

        LocalDateTime now = LocalDateTime.now();

        InventoryThresholdBreachEvent base = InventoryThresholdBreachEvent.builder()
                .id(1L)
                .inventory(inventory)
                .product(product)
                .currentQuantity(2)
                .thresholdLevel(5)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(now)
                .details("detail")
                .build();

        InventoryThresholdBreachEvent same = InventoryThresholdBreachEvent.builder()
                .id(1L)
                .inventory(inventory)
                .product(product)
                .currentQuantity(2)
                .thresholdLevel(5)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(now)
                .details("detail")
                .build();

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-event");

        InventoryThresholdBreachEvent diffType = InventoryThresholdBreachEvent.builder()
                .id(1L)
                .inventory(inventory)
                .product(product)
                .currentQuantity(2)
                .thresholdLevel(5)
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .createdAt(now)
                .details("detail")
                .build();

        assertNotEquals(base, diffType);
    }

    @Test
    void breachTypeDescriptionsAreStable() {
        assertEquals("Below minimum threshold",
                InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH.getDescription());
        assertEquals("Product out of stock", InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK.getDescription());
        assertEquals("Product back in stock", InventoryThresholdBreachEvent.BreachType.BACK_IN_STOCK.getDescription());
        assertEquals("Inventory restored above threshold",
                InventoryThresholdBreachEvent.BreachType.THRESHOLD_RESTORED.getDescription());
    }
}
