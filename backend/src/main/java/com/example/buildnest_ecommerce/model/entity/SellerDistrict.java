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
 * SellerDistrict — a seller's declared delivery district (FR-LOC-01, ADR 0001,
 * #561/#562). Modeled as its own entity (own {@code id}), not a plain
 * {@code @ManyToMany @JoinTable}, per SDD v4.0/4.8 §4.5.2's explicit design —
 * this is the {@code Seller ──[N:M]──► District} join table, superseding the
 * nullable {@code Seller.district_id} column added in #553.
 */
@Entity
@Table(name = "seller_districts", uniqueConstraints = @UniqueConstraint(
        columnNames = { "seller_id", "district_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "seller", "district", "createdAt" })
@ToString(exclude = { "seller", "district" })
public class SellerDistrict implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
