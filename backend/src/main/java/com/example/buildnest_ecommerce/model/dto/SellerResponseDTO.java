package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.Seller;
import java.time.LocalDateTime;

public record SellerResponseDTO(
        Long id,
        Long userId,
        String businessName,
        String businessRegistrationNumber,
        Seller.VerificationStatus verificationStatus,
        LocalDateTime createdAt) {

    public static SellerResponseDTO from(Seller seller) {
        return new SellerResponseDTO(
                seller.getId(),
                seller.getUser().getId(),
                seller.getBusinessName(),
                seller.getBusinessRegistrationNumber(),
                seller.getVerificationStatus(),
                seller.getCreatedAt());
    }
}
