package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
