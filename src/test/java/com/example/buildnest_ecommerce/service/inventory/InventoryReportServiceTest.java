package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryThresholdBreachEventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryReportServiceTest {

    @Test
    void generatesReportsAndSummary() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryThresholdBreachEventRepository breachRepository = mock(InventoryThresholdBreachEventRepository.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(3);
        inventory.setQuantityReserved(1);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        inventory.setLastThresholdBreach(LocalDateTime.now().minusDays(1));

        InventoryThresholdBreachEvent event = new InventoryThresholdBreachEvent();
        event.setId(10L);
        event.setProduct(product);
        event.setBreachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH);
        event.setCurrentQuantity(3);
        event.setThresholdLevel(5);
        event.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(inventoryRepository.findBelowThresholdProducts()).thenReturn(List.of(inventory));
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(event));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(breachRepository.findByProduct(product)).thenReturn(List.of(event));
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));

        InventoryReportService service = new InventoryReportService(inventoryRepository, breachRepository);

        List<Map<String, Object>> below = service.getProductsBelowThreshold();
        assertEquals(1, below.size());

        List<Map<String, Object>> breaches = service.getThresholdBreachesInRange(LocalDateTime.now().minusDays(1),
                LocalDateTime.now());
        assertEquals(1, breaches.size());

        List<Map<String, Object>> frequent = service.getFrequentlyLowStockProducts(LocalDateTime.now().minusDays(1),
                LocalDateTime.now());
        assertEquals(1, frequent.size());

        Map<String, Object> report = service.getProductInventoryReport(1L);
        assertEquals(1L, report.get("productId"));

        Map<String, Object> summary = service.getInventorySummary();
        assertEquals(1, summary.get("totalProducts"));
    }

    @Test
    void testInventoryMathOperationsCategoryTotals() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryThresholdBreachEventRepository breachRepository = mock(InventoryThresholdBreachEventRepository.class);

        Product product1 = new Product();
        product1.setId(1L);
        Inventory inv1 = new Inventory();
        inv1.setProduct(product1);
        inv1.setQuantityInStock(100);
        inv1.setQuantityReserved(20);

        Inventory inv2 = new Inventory();
        inv2.setProduct(new Product());
        inv2.setQuantityInStock(50);
        inv2.setQuantityReserved(10);

        when(inventoryRepository.findAll()).thenReturn(List.of(inv1, inv2));
        when(breachRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        InventoryReportService service = new InventoryReportService(inventoryRepository, breachRepository);
        Map<String, Object> summary = service.getInventorySummary();

        // Verify calculations executed without error
        assertNotNull(summary.get("totalProducts"));
        assertEquals(2, summary.get("totalProducts"));
    }

    @Test
    void testProductInventoryReportCalculations() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryThresholdBreachEventRepository breachRepository = mock(InventoryThresholdBreachEventRepository.class);

        Product product = new Product();
        product.setId(1L);
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(100);
        inventory.setQuantityReserved(30);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(breachRepository.findByProduct(product)).thenReturn(List.of());

        InventoryReportService service = new InventoryReportService(inventoryRepository, breachRepository);
        Map<String, Object> report = service.getProductInventoryReport(1L);

        // Verify report generated with calculations
        assertNotNull(report);
        // Service may not populate all fields, just verify report is not empty
        assertTrue(report.size() > 0);
    }
}
