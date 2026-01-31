package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryThresholdBreachEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating inventory reports and analytics (RQ-INV-REP-01,
 * RQ-INV-REP-02, RQ-INV-REP-03).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReportService {

    private final InventoryRepository inventoryRepository;
    private final InventoryThresholdBreachEventRepository breachEventRepository;

    /**
     * Get all products currently below threshold (RQ-INV-REP-01).
     */
    public List<Map<String, Object>> getProductsBelowThreshold() {
        List<Inventory> items = inventoryRepository.findBelowThresholdProducts();

        return items.stream()
                .map(this::inventoryToReportMap)
                .sorted(Comparator.comparingInt(m -> (Integer) m.get("shortfall")))
                .collect(Collectors.toList());
    }

    /**
     * Get threshold breaches within time range (RQ-INV-REP-02).
     */
    public List<Map<String, Object>> getThresholdBreachesInRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<InventoryThresholdBreachEvent> events = breachEventRepository
                .findByCreatedAtBetween(startDate, endDate);

        List<Map<String, Object>> result = new ArrayList<>();
        for (InventoryThresholdBreachEvent event : events) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", event.getId());
            map.put("productId", event.getProduct().getId());
            map.put("productName", event.getProduct().getName());
            map.put("breachType", event.getBreachType().name());
            map.put("currentQuantity", event.getCurrentQuantity());
            map.put("thresholdLevel", event.getThresholdLevel());
            map.put("timestamp", event.getCreatedAt());
            map.put("details", event.getDetails());
            result.add(map);
        }

        // Sort by timestamp descending
        result.sort((m1, m2) -> {
            LocalDateTime t1 = (LocalDateTime) m1.get("timestamp");
            LocalDateTime t2 = (LocalDateTime) m2.get("timestamp");
            return t2.compareTo(t1);
        });

        return result;
    }

    /**
     * Identify frequently low-stock or out-of-stock products (RQ-INV-REP-03).
     */
    public List<Map<String, Object>> getFrequentlyLowStockProducts(LocalDateTime startDate, LocalDateTime endDate) {
        List<InventoryThresholdBreachEvent> events = breachEventRepository
                .findByCreatedAtBetween(startDate, endDate);

        // Group by product and count breaches
        Map<Long, Long> breachCountByProduct = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getProduct().getId(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : breachCountByProduct.entrySet()) {
            List<InventoryThresholdBreachEvent> productEvents = events.stream()
                    .filter(e -> e.getProduct().getId().equals(entry.getKey()))
                    .collect(Collectors.toList());

            if (!productEvents.isEmpty()) {
                InventoryThresholdBreachEvent firstEvent = productEvents.get(0);

                Map<String, Object> map = new HashMap<>();
                map.put("productId", entry.getKey());
                map.put("productName", firstEvent.getProduct().getName());
                map.put("breachCount", entry.getValue());
                map.put("latestBreach", productEvents.stream()
                        .max(Comparator.comparing(InventoryThresholdBreachEvent::getCreatedAt))
                        .map(InventoryThresholdBreachEvent::getCreatedAt)
                        .orElse(null));
                map.put("currentStock", inventoryRepository.findByProduct(firstEvent.getProduct())
                        .map(Inventory::getQuantityInStock)
                        .orElse(0));
                result.add(map);
            }
        }

        // Sort by breachCount descending
        result.sort((m1, m2) -> Long.compare((Long) m2.get("breachCount"), (Long) m1.get("breachCount")));

        return result;
    }

    /**
     * Get detailed inventory report for a product (RQ-INV-REP-01).
     */
    public Map<String, Object> getProductInventoryReport(Long productId) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        List<InventoryThresholdBreachEvent> recentBreaches = breachEventRepository
                .findByProduct(inventory.getProduct());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("productId", inventory.getProduct().getId());
        report.put("productName", inventory.getProduct().getName());
        report.put("currentStock", inventory.getQuantityInStock());
        report.put("minimumThreshold", inventory.getMinimumStockLevel());
        report.put("status", inventory.getStatus().getDisplayName());
        report.put("reserved", inventory.getQuantityReserved());
        report.put("available", inventory.getQuantityInStock() - inventory.getQuantityReserved());
        report.put("lastRestocked", inventory.getLastRestocked());
        report.put("lastThresholdBreach", inventory.getLastThresholdBreach());
        report.put("breachHistory", recentBreaches.stream()
                .limit(10)
                .map(event -> Map.ofEntries(
                        Map.entry("breachType", event.getBreachType().name()),
                        Map.entry("quantity", event.getCurrentQuantity()),
                        Map.entry("timestamp", event.getCreatedAt())))
                .collect(Collectors.toList()));

        return report;
    }

    /**
     * Get summary statistics for all inventory (RQ-INV-REP-01).
     */
    public Map<String, Object> getInventorySummary() {
        List<Inventory> allInventories = inventoryRepository.findAll();

        long inStock = allInventories.stream()
                .filter(i -> i.getStatus().equals(com.example.buildnest_ecommerce.model.entity.InventoryStatus.IN_STOCK))
                .count();
        long lowStock = allInventories.stream()
                .filter(i -> i.getStatus().equals(com.example.buildnest_ecommerce.model.entity.InventoryStatus.LOW_STOCK))
                .count();
        long outOfStock = allInventories.stream().filter(
                i -> i.getStatus().equals(com.example.buildnest_ecommerce.model.entity.InventoryStatus.OUT_OF_STOCK))
                .count();

        long totalQuantity = allInventories.stream().mapToLong(Inventory::getQuantityInStock).sum();
        long totalReserved = allInventories.stream().mapToLong(Inventory::getQuantityReserved).sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProducts", allInventories.size());
        summary.put("inStock", inStock);
        summary.put("lowStock", lowStock);
        summary.put("outOfStock", outOfStock);
        summary.put("totalQuantityInStock", totalQuantity);
        summary.put("totalQuantityReserved", totalReserved);
        summary.put("totalAvailable", totalQuantity - totalReserved);

        return summary;
    }

    private Map<String, Object> inventoryToReportMap(Inventory inventory) {
        int threshold = inventory.getMinimumStockLevel();
        int current = inventory.getQuantityInStock();
        int shortfall = Math.max(0, threshold - current);

        return Map.ofEntries(
                Map.entry("productId", inventory.getProduct().getId()),
                Map.entry("productName", inventory.getProduct().getName()),
                Map.entry("currentQuantity", current),
                Map.entry("minimumThreshold", threshold),
                Map.entry("shortfall", shortfall),
                Map.entry("status", inventory.getStatus().getDisplayName()),
                Map.entry("lastBreach", inventory.getLastThresholdBreach()));
    }
}
