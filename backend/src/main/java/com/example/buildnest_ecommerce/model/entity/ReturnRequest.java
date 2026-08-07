package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "order", "user", "requestedAt",
        "resolvedAt", "updatedAt" })
@ToString(exclude = { "order", "user" })
public class ReturnRequest implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, REFUNDED
    }
}
