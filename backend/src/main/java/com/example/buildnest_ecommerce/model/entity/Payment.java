package com.example.buildnest_ecommerce.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(length = 255)
    private String razorpayOrderId;
    
    @Column(length = 255)
    private String razorpayPaymentId;
    
    @Column(nullable = false, length = 50)
    private String status; // PENDING, SUCCESS, FAILED, REFUNDED, PARTIALLY_REFUNDED

    @Column(name = "refunded_amount", nullable = false)
    private Double refundedAmount = 0.0;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_initiated_at")
    private LocalDateTime refundInitiatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
