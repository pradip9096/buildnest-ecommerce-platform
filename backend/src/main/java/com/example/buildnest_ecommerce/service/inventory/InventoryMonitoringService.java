package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryThresholdBreachEventRepository;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for monitoring inventory levels and generating alerts (RQ-INV-MON-01,
 * RQ-INV-MON-02, RQ-INV-MON-03).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class InventoryMonitoringService {

    private final InventoryRepository inventoryRepository;
    private final InventoryThresholdBreachEventRepository breachEventRepository;
    private final NotificationService notificationService;
    private final InventoryThresholdManagementService thresholdService;

    /**
     * Monitor all inventory levels continuously (RQ-INV-MON-01).
     * This is called by the scheduled task.
     */
    public void monitorInventoryLevels() {
        log.info("Starting inventory monitoring scan");
        List<Inventory> allInventories = inventoryRepository.findAll();

        int alertsGenerated = 0;
        for (Inventory inventory : allInventories) {
            if (checkAndGenerateAlert(inventory)) {
                alertsGenerated++;
            }
        }

        log.info("Inventory monitoring completed. {} alerts generated", alertsGenerated);
    }

    /**
     * Check if inventory falls below threshold and generate alert (RQ-INV-MON-02,
     * RQ-INV-MON-03).
     */
    public boolean checkAndGenerateAlert(Inventory inventory) {
        Integer effectiveThreshold = thresholdService.getEffectiveThreshold(inventory.getProduct().getId());

        // Check if below threshold (RQ-INV-MON-02, RQ-INV-MON-03)
        if (inventory.getQuantityInStock() <= effectiveThreshold) {

            // Check if this is a new breach
            if (inventory.getStatus() != InventoryStatus.LOW_STOCK &&
                    inventory.getStatus() != InventoryStatus.OUT_OF_STOCK) {
                generateThresholdAlert(inventory, effectiveThreshold);
                return true;
            }

            // Out of stock alert (RQ-INV-ALRT-02)
            if (inventory.getQuantityInStock() == 0 &&
                    inventory.getStatus() != InventoryStatus.OUT_OF_STOCK) {
                generateOutOfStockAlert(inventory);
                return true;
            }
        } else if (inventory.getStatus() == InventoryStatus.LOW_STOCK ||
                inventory.getStatus() == InventoryStatus.OUT_OF_STOCK) {
            // Generate back-in-stock alert
            generateBackInStockAlert(inventory);
            return true;
        }

        return false;
    }

    /**
     * Generate alert when inventory falls below threshold (RQ-INV-ALRT-01).
     */
    private void generateThresholdAlert(Inventory inventory, Integer threshold) {
        log.warn("LOW STOCK ALERT: Product {} quantity {} below threshold {}",
                inventory.getProduct().getId(), inventory.getQuantityInStock(), threshold);

        // Record breach event (RQ-INV-DATA-02)
        InventoryThresholdBreachEvent event = InventoryThresholdBreachEvent.builder()
                .product(inventory.getProduct())
                .currentQuantity(inventory.getQuantityInStock())
                .thresholdLevel(threshold)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .details(String.format("Product '%s' stock (%d) below threshold (%d)",
                        inventory.getProduct().getName(), inventory.getQuantityInStock(), threshold))
                .build();

        breachEventRepository.save(event);

        // Send notification (RQ-INV-ALRT-01, RQ-INV-ALRT-03, RQ-INV-ALRT-04)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", inventory.getProduct().getId());
        metadata.put("productName", inventory.getProduct().getName());
        metadata.put("currentQuantity", inventory.getQuantityInStock());
        metadata.put("threshold", threshold);
        metadata.put("breachType", "LOW_STOCK");

        notificationService.sendAlert(
                "Inventory Alert: Low Stock",
                String.format("Product '%s' stock level is critically low (%d/%d units)",
                        inventory.getProduct().getName(), inventory.getQuantityInStock(), threshold),
                "HIGH",
                metadata);
    }

    /**
     * Generate alert when product goes out of stock (RQ-INV-ALRT-02).
     */
    private void generateOutOfStockAlert(Inventory inventory) {
        log.error("OUT OF STOCK ALERT: Product {} is now out of stock",
                inventory.getProduct().getId());

        // Record breach event (RQ-INV-DATA-02)
        InventoryThresholdBreachEvent event = InventoryThresholdBreachEvent.builder()
                .product(inventory.getProduct())
                .currentQuantity(0)
                .thresholdLevel(inventory.getMinimumStockLevel())
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .details(String.format("Product '%s' is now out of stock", inventory.getProduct().getName()))
                .build();

        breachEventRepository.save(event);

        // Send notification (RQ-INV-ALRT-02, RQ-INV-ALRT-03, RQ-INV-ALRT-04)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", inventory.getProduct().getId());
        metadata.put("productName", inventory.getProduct().getName());
        metadata.put("breachType", "OUT_OF_STOCK");

        notificationService.sendAlert(
                "Inventory Alert: Out of Stock",
                String.format("Product '%s' is now OUT OF STOCK",
                        inventory.getProduct().getName()),
                "CRITICAL",
                metadata);
    }

    /**
     * Generate alert when product back in stock.
     */
    private void generateBackInStockAlert(Inventory inventory) {
        log.info("BACK IN STOCK: Product {} is back in stock", inventory.getProduct().getId());

        // Record event (RQ-INV-DATA-02)
        InventoryThresholdBreachEvent event = InventoryThresholdBreachEvent.builder()
                .product(inventory.getProduct())
                .currentQuantity(inventory.getQuantityInStock())
                .thresholdLevel(inventory.getMinimumStockLevel())
                .breachType(InventoryThresholdBreachEvent.BreachType.BACK_IN_STOCK)
                .newStatus(InventoryStatus.IN_STOCK)
                .details(String.format("Product '%s' is back in stock with %d units",
                        inventory.getProduct().getName(), inventory.getQuantityInStock()))
                .build();

        breachEventRepository.save(event);

        // Send notification (RQ-INV-ALRT-03, RQ-INV-ALRT-04)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", inventory.getProduct().getId());
        metadata.put("productName", inventory.getProduct().getName());
        metadata.put("currentQuantity", inventory.getQuantityInStock());
        metadata.put("breachType", "BACK_IN_STOCK");

        notificationService.sendAlert(
                "Inventory Update: Back in Stock",
                String.format("Product '%s' is back in stock with %d units available",
                        inventory.getProduct().getName(), inventory.getQuantityInStock()),
                "INFO",
                metadata);
    }
}