package com.example.buildnest_ecommerce.service.address;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;

public interface AddressService {
    AddressResponseDTO createAddress(Long userId, CreateAddressRequest request);
}
