package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The seller declaring this delivery district. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    /** The declared delivery district. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    /** Row creation timestamp. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
