package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.District;

public record DistrictResponseDTO(Long id, String name) {

    public static DistrictResponseDTO from(District district) {
        return new DistrictResponseDTO(district.getId(), district.getName());
    }
}
