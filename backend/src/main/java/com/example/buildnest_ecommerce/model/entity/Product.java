package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ignoreUnknown=true only affects deserialization (Redis cache-hit reads);
// getStockQuantity() (derived, no backing field, #485/#651) still
// serializes on writes and on live HTTP responses that read it.
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" },
        ignoreUnknown = true)
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "category", "inventory", "variants", "tags",
        "seller", "createdAt", "updatedAt" })
@ToString(exclude = { "category", "inventory", "variants", "tags", "seller" })
public class Product implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    @Column(name = "sku", unique = true)
    private String sku;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Owning seller (FR-SEL-03) — reactivates the dormant {@code
     * supplier_id}/{@code fk_product_supplier} FK to {@code users} already
     * present in the original bootstrap schema (db.changelog-master.sql),
     * per SDD v4.0 Revision History and §4.5.2. Nullable — pre-marketplace
     * products (admin-created, no owning seller) are unaffected.
     */
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private User seller;

    @JsonIgnoreProperties({ "product", "hibernateLazyInitializer", "handler" })
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = jakarta.persistence.FetchType.LAZY)
    private Inventory inventory;

    @JsonIgnoreProperties({ "product", "hibernateLazyInitializer", "handler" })
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = jakarta.persistence.FetchType.LAZY, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties({ "products", "hibernateLazyInitializer", "handler" })
    @ManyToMany(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinTable(name = "product_tag_map",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<ProductTag> tags = new HashSet<>();

    /**
     * Derived from {@code Inventory.quantityInStock} — {@code Inventory}
     * is the single source of truth for stock (#485); there is no backing
     * column here. Requires {@code inventory} to already be initialized
     * (fetch join or {@code Hibernate.initialize()}) before this is called
     * outside an active session.
     */
    public Integer getStockQuantity() {
        return inventory != null ? inventory.getQuantityInStock() : null;
    }
}
