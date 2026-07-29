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
 * Seller — 1:1 extension of {@link User}, mirroring the existing
 * {@link Address} extension-table pattern (SDD v4.0/4.8 §4.3.3/§4.5.1/§4.5.2).
 * Delivery districts are declared via {@link SellerDistrict}
 * ({@code Seller ──[N:M]──► District}, ADR 0001, #561/#562) — the earlier
 * plain nullable {@code district_id} column (#553) is dropped in favor of
 * this join table.
 */
@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "user", "createdAt", "updatedAt" })
@ToString(exclude = { "user" })
public class Seller implements AggregateRoot {

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
