package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * District — fixed, admin-maintained reference table for location-based
 * seller/buyer matching (FR-LOC-01/02, ADR 0001, #561/#562). No geocoding,
 * no free-text address parsing — sellers declare delivery districts
 * ({@link SellerDistrict}) and a buyer's district is derived from their
 * {@link Address} by name match.
 */
@Entity
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "createdAt" })
@ToString
public class District implements AggregateRoot {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference-table display name, unique. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Row creation timestamp. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
