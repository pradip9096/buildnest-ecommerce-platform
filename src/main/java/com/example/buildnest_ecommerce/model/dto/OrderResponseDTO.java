package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    
    private Long id;
    
    private Long userId;
    
    private String status;
    
    private Double totalAmount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
