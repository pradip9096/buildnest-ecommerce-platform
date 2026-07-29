package com.example.buildnest_ecommerce.service.seller;

import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;

public interface SellerService {
    SellerResponseDTO registerSeller(
            Long userId, RegisterSellerRequest request);

    SellerResponseDTO getSellerProfile(Long userId);

    /**
     * Replaces the seller's declared delivery districts (FR-LOC-01).
     *
     * @param userId the requesting user's ID (the seller's owning user)
     * @param districtIds the full replacement set of district IDs
     * @return the seller's updated profile
     */
    SellerResponseDTO updateSellerDistricts(
            Long userId, Set<Long> districtIds);

    Page<SellerResponseDTO> getSellersByVerificationStatus(
            Seller.VerificationStatus status, Pageable pageable);

    SellerResponseDTO updateVerificationStatus(
            Long sellerId, String newStatus, String rejectionReason);
}
