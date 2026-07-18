package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {
        "product", "inventory", "createdAt", "updatedAt" })
@ToString(exclude = { "product", "inventory" })
public class ProductVariant implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Never serialized: the API consumer already has the parent product in
    // view, and Product carries its own lazy collections (e.g. tags) that
    // would throw LazyInitializationException once Jackson tried to walk
    // them outside the transaction under open-in-view=false. Still used
    // server-side by getEffectivePrice() below.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "size")
    private String size;

    @Column(name = "colour")
    private String colour;

    @Column(name = "price_adjustment", nullable = false)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @JsonIgnoreProperties({
            "variant", "hibernateLazyInitializer", "handler" })
    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Inventory inventory;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Effective unit price = parent product's active price (discountPrice
     * if set, else price) plus this variant's adjustment. Kept here rather
     * than in a DTO/service since it is a pure function of the entity's own
     * state plus its parent, matching how Inventory.getAvailableQuantity()
     * is derived on-entity.
     */
    public BigDecimal getEffectivePrice() {
        BigDecimal basePrice = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getPrice();
        return basePrice.add(priceAdjustment);
    }
}
