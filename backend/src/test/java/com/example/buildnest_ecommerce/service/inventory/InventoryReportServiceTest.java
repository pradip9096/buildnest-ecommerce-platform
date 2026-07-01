package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryThresholdBreachEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryReportServiceTest {

    private InventoryRepository inventoryRepository;
    private InventoryThresholdBreachEventRepository breachRepository;
    private InventoryReportService service;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        breachRepository = mock(InventoryThresholdBreachEventRepository.class);
        service = new InventoryReportService(inventoryRepository, breachRepository);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProduct(product);
        inventory.setQuantityInStock(3);
        inventory.setQuantityReserved(1);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        inventory.setLastThresholdBreach(LocalDateTime.now().minusDays(1));
    }

    // ── getProductsBelowThreshold ─────────────────────────────────────────────

    @Test
    @DisplayName("getProductsBelowThreshold — maps productId, productName, currentQuantity, minimumThreshold, shortfall")
    void getProductsBelowThreshold_mapsAllFields() {
        when(inventoryRepository.findBelowThresholdProducts()).thenReturn(List.of(inventory));

        List<Map<String, Object>> result = service.getProductsBelowThreshold();

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals(1L, row.get("productId"), "productId must match");
        assertEquals("Test Product", row.get("productName"), "productName must match");
        assertEquals(3, row.get("currentQuantity"), "currentQuantity must match inventory stock");
        assertEquals(5, row.get("minimumThreshold"), "minimumThreshold must match minimumStockLevel");
        assertEquals(2, row.get("shortfall"), "shortfall must be threshold - current = 2");
    }

    @Test
    @DisplayName("getProductsBelowThreshold — sorted by shortfall ascending")
    void getProductsBelowThreshold_sortedByShortfallAscending() {
        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("P2");
        Inventory inv2 = new Inventory();
        inv2.setProduct(p2);
        inv2.setQuantityInStock(1);
        inv2.setMinimumStockLevel(10); // shortfall=9
        inv2.setStatus(InventoryStatus.LOW_STOCK);

        inv2.setLastThresholdBreach(LocalDateTime.now().minusDays(2));
        // inventory has shortfall=2, inv2 has shortfall=9
        when(inventoryRepository.findBelowThresholdProducts()).thenReturn(List.of(inv2, inventory));

        List<Map<String, Object>> result = service.getProductsBelowThreshold();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).get("shortfall"), "smaller shortfall must come first");
        assertEquals(9, result.get(1).get("shortfall"), "larger shortfall must come last");
    }

    @Test
    @DisplayName("getProductsBelowThreshold — empty list when no products below threshold")
    void getProductsBelowThreshold_empty() {
        when(inventoryRepository.findBelowThresholdProducts()).thenReturn(List.of());

        List<Map<String, Object>> result = service.getProductsBelowThreshold();

        assertTrue(result.isEmpty());
    }

    // ── getThresholdBreachesInRange ───────────────────────────────────────────

    @Test
    @DisplayName("getThresholdBreachesInRange — maps id, productId, productName, breachType, currentQuantity, thresholdLevel, timestamp")
    void getThresholdBreachesInRange_mapsAllFields() {
        LocalDateTime breachTime = LocalDateTime.of(2026, 6, 1, 12, 0);
        InventoryThresholdBreachEvent event = buildBreachEvent(10L, product, 3, 5, breachTime);
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(event));

        List<Map<String, Object>> result = service.getThresholdBreachesInRange(
                LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals(10L, row.get("id"), "id must match event id");
        assertEquals(1L, row.get("productId"), "productId must match");
        assertEquals("Test Product", row.get("productName"), "productName must match");
        assertEquals("THRESHOLD_BREACH", row.get("breachType"), "breachType must be name()");
        assertEquals(3, row.get("currentQuantity"), "currentQuantity must match");
        assertEquals(5, row.get("thresholdLevel"), "thresholdLevel must match");
        assertEquals(breachTime, row.get("timestamp"), "timestamp must match createdAt");
    }

    @Test
    @DisplayName("getThresholdBreachesInRange — sorted by timestamp descending (newest first)")
    void getThresholdBreachesInRange_sortedByTimestampDescending() {
        LocalDateTime earlier = LocalDateTime.of(2026, 6, 1, 8, 0);
        LocalDateTime later   = LocalDateTime.of(2026, 6, 1, 14, 0);
        InventoryThresholdBreachEvent e1 = buildBreachEvent(1L, product, 3, 5, earlier);
        InventoryThresholdBreachEvent e2 = buildBreachEvent(2L, product, 2, 5, later);
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(e1, e2));

        List<Map<String, Object>> result = service.getThresholdBreachesInRange(
                LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertEquals(2, result.size());
        assertEquals(later, result.get(0).get("timestamp"), "most recent breach must be first");
        assertEquals(earlier, result.get(1).get("timestamp"), "oldest breach must be last");
    }

    // ── getFrequentlyLowStockProducts ─────────────────────────────────────────

    @Test
    @DisplayName("getFrequentlyLowStockProducts — maps productId, productName, breachCount, currentStock")
    void getFrequentlyLowStockProducts_mapsFields() {
        LocalDateTime t1 = LocalDateTime.of(2026, 6, 1, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 6, 2, 8, 0);
        InventoryThresholdBreachEvent e1 = buildBreachEvent(1L, product, 3, 5, t1);
        InventoryThresholdBreachEvent e2 = buildBreachEvent(2L, product, 2, 5, t2);
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(e1, e2));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        List<Map<String, Object>> result = service.getFrequentlyLowStockProducts(
                LocalDateTime.now().minusDays(7), LocalDateTime.now());

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals(1L, row.get("productId"), "productId must match");
        assertEquals("Test Product", row.get("productName"), "productName must match");
        assertEquals(2L, row.get("breachCount"), "breachCount must be 2");
        assertEquals(3, row.get("currentStock"), "currentStock must reflect quantityInStock");
    }

    @Test
    @DisplayName("getFrequentlyLowStockProducts — sorted by breachCount descending")
    void getFrequentlyLowStockProducts_sortedByBreachCountDescending() {
        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("P2");
        Inventory inv2 = new Inventory();
        inv2.setProduct(p2);
        inv2.setQuantityInStock(5);
        inv2.setMinimumStockLevel(10);
        inv2.setStatus(InventoryStatus.LOW_STOCK);

        LocalDateTime t = LocalDateTime.of(2026, 6, 1, 8, 0);
        // product has 1 breach, p2 has 3 breaches
        InventoryThresholdBreachEvent e1 = buildBreachEvent(1L, product, 3, 5, t);
        InventoryThresholdBreachEvent e2 = buildBreachEvent(2L, p2, 4, 10, t.plusHours(1));
        InventoryThresholdBreachEvent e3 = buildBreachEvent(3L, p2, 3, 10, t.plusHours(2));
        InventoryThresholdBreachEvent e4 = buildBreachEvent(4L, p2, 2, 10, t.plusHours(3));
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(e1, e2, e3, e4));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findByProduct(p2)).thenReturn(Optional.of(inv2));

        List<Map<String, Object>> result = service.getFrequentlyLowStockProducts(
                LocalDateTime.now().minusDays(7), LocalDateTime.now());

        assertEquals(2, result.size());
        assertEquals(3L, result.get(0).get("breachCount"), "product with more breaches must be first");
        assertEquals(1L, result.get(1).get("breachCount"), "product with fewer breaches must be last");
    }

    // ── getProductInventoryReport ─────────────────────────────────────────────

    @Test
    @DisplayName("getProductInventoryReport — computes available = stock - reserved and maps all fields")
    void getProductInventoryReport_mapsComputedFields() {
        inventory.setQuantityInStock(100);
        inventory.setQuantityReserved(30);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(breachRepository.findByProduct(product)).thenReturn(List.of());

        Map<String, Object> report = service.getProductInventoryReport(1L);

        assertEquals(1L, report.get("productId"), "productId must match");
        assertEquals("Test Product", report.get("productName"), "productName must match");
        assertEquals(100, report.get("currentStock"), "currentStock must match quantityInStock");
        assertEquals(30, report.get("reserved"), "reserved must match quantityReserved");
        assertEquals(70, report.get("available"), "available must be stock - reserved = 70");
        assertEquals(5, report.get("minimumThreshold"), "minimumThreshold must match minimumStockLevel");
        assertNotNull(report.get("status"), "status must be set");
        assertNotNull(report.get("breachHistory"), "breachHistory must be present");
    }

    @Test
    @DisplayName("getProductInventoryReport — throws when inventory not found")
    void getProductInventoryReport_notFound_throws() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getProductInventoryReport(99L));
    }

    // ── getInventorySummary ───────────────────────────────────────────────────

    @Test
    @DisplayName("getInventorySummary — counts by status and computes totalQuantity, totalReserved, totalAvailable")
    void getInventorySummary_countsAndTotals() {
        Product p2 = new Product();
        p2.setId(2L);
        Inventory outOfStock = new Inventory();
        outOfStock.setProduct(p2);
        outOfStock.setQuantityInStock(0);
        outOfStock.setQuantityReserved(0);
        outOfStock.setStatus(InventoryStatus.OUT_OF_STOCK);

        // inventory (LOW_STOCK): stock=3, reserved=1
        // outOfStock (OUT_OF_STOCK): stock=0, reserved=0
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory, outOfStock));

        Map<String, Object> summary = service.getInventorySummary();

        assertEquals(2, summary.get("totalProducts"), "totalProducts must be 2");
        assertEquals(0L, summary.get("inStock"), "inStock count must be 0");
        assertEquals(1L, summary.get("lowStock"), "lowStock count must be 1");
        assertEquals(1L, summary.get("outOfStock"), "outOfStock count must be 1");
        assertEquals(3L, summary.get("totalQuantityInStock"), "totalQuantityInStock must be 3");
        assertEquals(1L, summary.get("totalQuantityReserved"), "totalQuantityReserved must be 1");
        assertEquals(2L, summary.get("totalAvailable"), "totalAvailable must be 3-1=2");
    }

    @Test
    @DisplayName("getInventorySummary — all IN_STOCK returns correct counts")
    void getInventorySummary_allInStock() {
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventory.setQuantityInStock(50);
        inventory.setQuantityReserved(10);
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));

        Map<String, Object> summary = service.getInventorySummary();

        assertEquals(1L, summary.get("inStock"));
        assertEquals(0L, summary.get("lowStock"));
        assertEquals(0L, summary.get("outOfStock"));
        assertEquals(50L, summary.get("totalQuantityInStock"));
        assertEquals(10L, summary.get("totalQuantityReserved"));
        assertEquals(40L, summary.get("totalAvailable"));
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private InventoryThresholdBreachEvent buildBreachEvent(Long id, Product p,
            int currentQty, int threshold, LocalDateTime createdAt) {
        InventoryThresholdBreachEvent event = new InventoryThresholdBreachEvent();
        event.setId(id);
        event.setProduct(p);
        event.setBreachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH);
        event.setCurrentQuantity(currentQty);
        event.setThresholdLevel(threshold);
        event.setCreatedAt(createdAt);
        return event;
    }
}
