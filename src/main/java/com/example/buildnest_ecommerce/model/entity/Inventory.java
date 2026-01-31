package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantityInStock;

    @Column(nullable = false)
    private Integer quantityReserved = 0;

    @Column(nullable = false)
    private Integer minimumStockLevel;

    @Column(name = "use_category_threshold")
    private Boolean useCategoryThreshold = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status = InventoryStatus.IN_STOCK;

    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Column(name = "last_threshold_breach")
    private LocalDateTime lastThresholdBreach;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY, orphanRemoval = true)
    private List<InventoryThresholdBreachEvent> thresholdBreaches = new ArrayList<>();

    /**
     * Get available quantity (in stock - reserved).
     */
    public Integer getAvailableQuantity() {
        return Math.max(0, quantityInStock - quantityReserved);
    }
}
