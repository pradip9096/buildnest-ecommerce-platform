package com.example.buildnest_ecommerce.service.address;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.model.payload.UpdateAddressRequest;

import java.util.List;

public interface AddressService {
    AddressResponseDTO createAddress(Long userId, CreateAddressRequest request);

    List<AddressResponseDTO> getAddresses(Long userId);

    AddressResponseDTO updateAddress(Long userId, Long addressId, UpdateAddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    AddressResponseDTO setDefaultAddress(Long userId, Long addressId);
}
