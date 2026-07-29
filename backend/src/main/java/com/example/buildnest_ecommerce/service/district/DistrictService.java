package com.example.buildnest_ecommerce.service.district;

import com.example.buildnest_ecommerce.model.dto.DistrictResponseDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import java.util.List;
import java.util.Set;

/**
 * District reference-data operations (FR-LOC-01/02, ADR 0001, #561/#562).
 */
public interface DistrictService {

    /**
     * Lists every district in the fixed, admin-maintained reference table.
     *
     * @return all districts
     */
    List<DistrictResponseDTO> getAllDistricts();

    /**
     * Replaces the seller's full set of declared delivery districts
     * (FR-LOC-01).
     *
     * @param sellerId the seller's own ID (not the user ID)
     * @param districtIds the full replacement set of district IDs
     * @return the seller's new declared district set
     */
    List<DistrictResponseDTO> updateSellerDistricts(
            Long sellerId, Set<Long> districtIds);

    /**
     * Lists a seller's currently declared delivery districts.
     *
     * @param sellerId the seller's own ID (not the user ID)
     * @return the seller's declared districts
     */
    List<DistrictResponseDTO> getSellerDistricts(Long sellerId);

    /**
     * Derives and stores {@code user}'s own district (FR-LOC-02) by
     * matching {@code city} against the fixed reference table. A no-op
     * (leaves {@code user.district} unchanged) when no matching district
     * exists — the reference table not yet covering a city is not an error.
     *
     * @param user the buyer whose district is being derived
     * @param city the city name to match against the reference table
     */
    void deriveBuyerDistrict(User user, String city);
}
