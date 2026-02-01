package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventory entity tests")
class InventoryTest {

    @Test
    @DisplayName("Should create Inventory with all fields")
    void testInventoryConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setId(1L);
        List<InventoryThresholdBreachEvent> breaches = new ArrayList<>();

        Inventory inventory = new Inventory(
                10L,
                product,
                100,
                10,
                20,
                false,
                InventoryStatus.IN_STOCK,
                now,
                now,
                5L,
                now,
                breaches);

        assertEquals(10L, inventory.getId());
        assertEquals(product, inventory.getProduct());
        assertEquals(100, inventory.getQuantityInStock());
        assertEquals(10, inventory.getQuantityReserved());
        assertEquals(20, inventory.getMinimumStockLevel());
        assertFalse(inventory.getUseCategoryThreshold());
        assertEquals(InventoryStatus.IN_STOCK, inventory.getStatus());
        assertEquals(now, inventory.getLastRestocked());
        assertEquals(now, inventory.getUpdatedAt());
        assertEquals(5L, inventory.getVersion());
        assertEquals(now, inventory.getLastThresholdBreach());
        assertEquals(breaches, inventory.getThresholdBreaches());
    }

    @Test
    @DisplayName("Should create Inventory with no-args constructor")
    void testNoArgsConstructor() {
        Inventory inventory = new Inventory();
        assertNotNull(inventory);
        assertNull(inventory.getId());
        assertNull(inventory.getProduct());
    }

    @Test
    @DisplayName("Should set and get Inventory fields")
    void testSettersAndGetters() {
        Inventory inventory = new Inventory();
        Product product = new Product();
        product.setId(5L);
        LocalDateTime now = LocalDateTime.now();

        inventory.setId(20L);
        inventory.setProduct(product);
        inventory.setQuantityInStock(200);
        inventory.setQuantityReserved(50);
        inventory.setMinimumStockLevel(30);
        inventory.setUseCategoryThreshold(true);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        inventory.setLastRestocked(now);
        inventory.setUpdatedAt(now);
        inventory.setVersion(10L);
        inventory.setLastThresholdBreach(now);

        assertEquals(20L, inventory.getId());
        assertEquals(product, inventory.getProduct());
        assertEquals(200, inventory.getQuantityInStock());
        assertEquals(50, inventory.getQuantityReserved());
        assertEquals(30, inventory.getMinimumStockLevel());
        assertTrue(inventory.getUseCategoryThreshold());
        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
        assertEquals(now, inventory.getLastRestocked());
        assertEquals(now, inventory.getUpdatedAt());
        assertEquals(10L, inventory.getVersion());
        assertEquals(now, inventory.getLastThresholdBreach());
    }

    @Test
    @DisplayName("Should test getAvailableQuantity calculation")
    void testGetAvailableQuantity() {
        Inventory inventory = new Inventory();
        inventory.setQuantityInStock(100);
        inventory.setQuantityReserved(30);

        assertEquals(70, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should test getAvailableQuantity with negative result returns zero")
    void testGetAvailableQuantityNegative() {
        Inventory inventory = new Inventory();
        inventory.setQuantityInStock(50);
        inventory.setQuantityReserved(80);

        assertEquals(0, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should test getAvailableQuantity with equal stock and reserved")
    void testGetAvailableQuantityZero() {
        Inventory inventory = new Inventory();
        inventory.setQuantityInStock(100);
        inventory.setQuantityReserved(100);

        assertEquals(0, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should test equals and hashCode for identical Inventories")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setId(1L);

        Inventory inv1 = new Inventory();
        inv1.setId(1L);
        inv1.setProduct(product);
        inv1.setQuantityInStock(100);
        inv1.setQuantityReserved(10);
        inv1.setStatus(InventoryStatus.IN_STOCK);
        inv1.setUpdatedAt(now);

        Inventory inv2 = new Inventory();
        inv2.setId(1L);
        inv2.setProduct(product);
        inv2.setQuantityInStock(100);
        inv2.setQuantityReserved(10);
        inv2.setStatus(InventoryStatus.IN_STOCK);
        inv2.setUpdatedAt(now);

        assertEquals(inv1, inv2);
        assertEquals(inv1.hashCode(), inv2.hashCode());
    }

    @Test
    @DisplayName("Should test equals with different Inventories")
    void testEqualsDifferentInventories() {
        Inventory inv1 = new Inventory();
        inv1.setId(1L);
        inv1.setQuantityInStock(100);

        Inventory inv2 = new Inventory();
        inv2.setId(2L);
        inv2.setQuantityInStock(200);

        assertNotEquals(inv1, inv2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);

        assertNotEquals(inventory, null);
        assertNotEquals(inventory, "Not an Inventory");
        assertEquals(inventory, inventory);
    }

    @Test
    @DisplayName("Should test equals with null fields")
    void testEqualsWithNullFields() {
        Inventory inv1 = new Inventory();
        Inventory inv2 = new Inventory();
        assertEquals(inv1, inv2);

        inv1.setQuantityInStock(100);
        assertNotEquals(inv1, inv2);

        inv2.setQuantityInStock(100);
        assertEquals(inv1, inv2);
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setQuantityInStock(100);
        inventory.setQuantityReserved(10);
        inventory.setStatus(InventoryStatus.IN_STOCK);

        String result = inventory.toString();
        assertTrue(result.contains("Inventory"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("IN_STOCK"));
    }

    @Test
    @DisplayName("Should test all InventoryStatus enum values")
    void testInventoryStatusEnum() {
        assertEquals(3, InventoryStatus.values().length);
        assertEquals(InventoryStatus.IN_STOCK, InventoryStatus.valueOf("IN_STOCK"));
        assertEquals(InventoryStatus.LOW_STOCK, InventoryStatus.valueOf("LOW_STOCK"));
        assertEquals(InventoryStatus.OUT_OF_STOCK, InventoryStatus.valueOf("OUT_OF_STOCK"));
    }

    @Test
    @DisplayName("Should test canEqual with subclass")
    void testCanEqualWithSubclass() {
        LocalDateTime now = LocalDateTime.now();
        Inventory inv1 = new Inventory();
        inv1.setId(1L);
        inv1.setUpdatedAt(now);

        Inventory subclass = new Inventory() {
        };
        subclass.setId(1L);
        subclass.setUpdatedAt(now);

        assertEquals(inv1, subclass);
    }

    @Test
    @DisplayName("Should test all null fields equals another with all null fields")
    void testAllNullFieldsEquals() {
        Inventory inv1 = new Inventory();
        Inventory inv2 = new Inventory();

        assertEquals(inv1, inv2);
        assertEquals(inv1.hashCode(), inv2.hashCode());
    }

    @Test
    @DisplayName("Should test Inventory with threshold breaches collection")
    void testThresholdBreachesCollection() {
        Inventory inventory = new Inventory();
        List<InventoryThresholdBreachEvent> breaches = new ArrayList<>();

        InventoryThresholdBreachEvent event = new InventoryThresholdBreachEvent();
        event.setId(1L);
        breaches.add(event);

        inventory.setThresholdBreaches(breaches);
        assertEquals(1, inventory.getThresholdBreaches().size());
        assertTrue(inventory.getThresholdBreaches().contains(event));
    }

    @Test
    @DisplayName("Should test useCategoryThreshold flag")
    void testUseCategoryThreshold() {
        Inventory inventory = new Inventory();
        assertFalse(inventory.getUseCategoryThreshold()); // Default is false
        inventory.setUseCategoryThreshold(false);
        assertFalse(inventory.getUseCategoryThreshold());
    }

    @Test
    @DisplayName("Should test version field for optimistic locking")
    void testVersionField() {
        Inventory inventory = new Inventory();
        assertEquals(0L, inventory.getVersion()); // Default is 0L

        inventory.setVersion(0L);
        assertEquals(0L, inventory.getVersion());

        inventory.setVersion(5L);
        assertEquals(5L, inventory.getVersion());
    }
}
