package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Audit log for inventory threshold breaches (RQ-INV-DATA-02).
 */
@Entity
@Table(name = "inventory_threshold_breach_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryThresholdBreachEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(nullable = false)
    private Integer thresholdLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BreachType breachType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus newStatus;

    @Column(name = "created_at")
    @lombok.Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String details;

    public enum BreachType {
        THRESHOLD_BREACH("Below minimum threshold"),
        OUT_OF_STOCK("Product out of stock"),
        BACK_IN_STOCK("Product back in stock"),
        THRESHOLD_RESTORED("Inventory restored above threshold");

        private final String description;

        BreachType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
