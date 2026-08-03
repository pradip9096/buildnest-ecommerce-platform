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
import java.util.Set;

// Unlike Product/Category/Inventory, User never carried this guard — never
// hit until a raw User nested inside another entity (ProductReview.user,
// #441) got serialized while still a live Hibernate proxy: Jackson tried to
// introspect the proxy's own synthetic hibernateLazyInitializer/handler
// accessors and failed on their internal ByteBuddyInterceptor type.
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "roles", "addresses", "district", "createdAt",
        "updatedAt", "deletedAt", "lastLogin", "consentAt", "anonymizedAt" })
@ToString(exclude = { "roles", "addresses", "district" })
public class User implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // Never serialize the hash — no endpoint currently returns a raw User
    // (UserController/AdminUserController map to DTOs), but nothing
    // previously enforced that at the entity level; ProductReview.user
    // (#441) would have been the first to leak it had this gone unfixed.
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // GDPR consent (#128) — captured at registration, required true
    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven = false;

    @Column(name = "consent_at")
    private LocalDateTime consentAt;

    // Set once anonymization scrubs this row's PII; also the
    // idempotency guard so re-runs skip already-processed rows.
    @Column(name = "anonymized_at")
    private LocalDateTime anonymizedAt;

    // jpa-rule-exception: roles is jpa.md's own named EAGER exception —
    // a small, bounded collection always needed with the parent user
    // (security/authorization checks read it on every request).
    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    // Lazy collection, no backing fetch-join anywhere it's currently read —
    // ignored so any endpoint that nests a raw User (e.g. ProductReview.user,
    // #441) doesn't throw serializing this post-transaction.
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Address> addresses;

    // Buyer's own district (FR-LOC-02, ADR 0001, #561/#562), derived from
    // this user's default/most-recent Address by name match against the
    // fixed districts reference table — never set directly by the caller.
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;
}
