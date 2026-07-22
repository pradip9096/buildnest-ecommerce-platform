package com.example.buildnest_ecommerce.service.seller;

import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerService {
    SellerResponseDTO registerSeller(
            Long userId, RegisterSellerRequest request);

    SellerResponseDTO getSellerProfile(Long userId);

    Page<SellerResponseDTO> getSellersByVerificationStatus(
            Seller.VerificationStatus status, Pageable pageable);

    SellerResponseDTO updateVerificationStatus(
            Long sellerId, String newStatus, String rejectionReason);
}
