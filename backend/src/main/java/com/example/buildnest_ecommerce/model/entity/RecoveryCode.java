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

/**
 * One-time recovery code issued when a user enables TOTP 2FA (#91, AUTH-02).
 * The raw code is shown to the client exactly once, at generation time, and
 * is never persisted -- only its BCrypt hash is stored.
 */
@Entity
@Table(name = "recovery_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "user", "createdAt", "usedAt" })
@ToString(exclude = { "user" })
public class RecoveryCode implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
