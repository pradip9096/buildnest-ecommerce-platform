package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.District;

public record DistrictResponseDTO(Long id, String name) {

    /**
     * Maps a {@link District} entity to its response DTO.
     *
     * @param district the entity to map
     * @return the mapped DTO
     */
    public static DistrictResponseDTO from(final District district) {
        return new DistrictResponseDTO(district.getId(), district.getName());
    }
}
