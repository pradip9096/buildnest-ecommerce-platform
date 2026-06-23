package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Wishlist Entity
 * Represents a user's wishlist containing favorite products
 * Implements Section 6.1 - E-Commerce Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@Entity
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "wishlist_products", joinColumns = @JoinColumn(name = "wishlist_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add product to wishlist
     */
    public void addProduct(Product product) {
        this.products.add(product);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Remove product from wishlist
     */
    public void removeProduct(Product product) {
        this.products.remove(product);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if product is in wishlist
     */
    public boolean containsProduct(Product product) {
        return this.products.contains(product);
    }

    /**
     * Get product count in wishlist
     */
    public int getProductCount() {
        return this.products.size();
    }

    /**
     * Clear all products from wishlist
     */
    public void clearProducts() {
        this.products.clear();
        this.updatedAt = LocalDateTime.now();
    }
}
