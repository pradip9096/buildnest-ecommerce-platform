package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "product", "variant", "thresholdBreaches", "updatedAt", "lastRestocked",
        "lastThresholdBreach" })
@ToString(exclude = { "product", "variant", "thresholdBreaches" })
public class Inventory implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable: a row tracks stock for either a product with no variants, or a
     * single variant (see {@link #variant}) — never both. Historically this
     * column was NOT NULL (1 row per product); it was relaxed to support
     * per-variant inventory (PROD-01, #81).
     */
    @JsonIgnore
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @JsonIgnore
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

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

    @Column(name = "reservation_expires_at")
    private LocalDateTime reservationExpiresAt;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY, orphanRemoval = true)
    private List<InventoryThresholdBreachEvent> thresholdBreaches = new ArrayList<>();

    /**
     * Get available quantity (in stock - reserved).
     */
    public Integer getAvailableQuantity() {
        return Math.max(0, quantityInStock - quantityReserved);
    }
}
