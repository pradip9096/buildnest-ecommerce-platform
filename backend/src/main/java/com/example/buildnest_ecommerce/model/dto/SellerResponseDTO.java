package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.Seller;
import java.time.LocalDateTime;
import java.util.List;

public record SellerResponseDTO(
        Long id,
        Long userId,
        String businessName,
        String businessRegistrationNumber,
        Seller.VerificationStatus verificationStatus,
        List<DistrictResponseDTO> districts,
        LocalDateTime createdAt) {

    public static SellerResponseDTO from(
            Seller seller, List<DistrictResponseDTO> districts) {
        return new SellerResponseDTO(
                seller.getId(),
                seller.getUser().getId(),
                seller.getBusinessName(),
                seller.getBusinessRegistrationNumber(),
                seller.getVerificationStatus(),
                districts,
                seller.getCreatedAt());
    }
}
