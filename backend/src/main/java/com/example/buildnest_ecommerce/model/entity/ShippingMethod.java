package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "base_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "cost_per_kg", nullable = false, precision = 19, scale = 2)
    private BigDecimal costPerKg = BigDecimal.ZERO;

    @Column(name = "estimated_days_min", nullable = false)
    private Integer estimatedDaysMin;

    @Column(name = "estimated_days_max", nullable = false)
    private Integer estimatedDaysMax;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
