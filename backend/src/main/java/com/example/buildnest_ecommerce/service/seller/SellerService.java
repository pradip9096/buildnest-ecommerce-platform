package com.example.buildnest_ecommerce.service.seller;

import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;

public interface SellerService {
    SellerResponseDTO registerSeller(
            Long userId, RegisterSellerRequest request);

    SellerResponseDTO getSellerProfile(Long userId);
}
