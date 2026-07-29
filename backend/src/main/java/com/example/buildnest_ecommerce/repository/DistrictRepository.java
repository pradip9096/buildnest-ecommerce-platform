package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    /**
     * Finds a district by name, case-insensitively.
     *
     * @param name the district name to match
     * @return the matching district, if any
     */
    Optional<District> findByNameIgnoreCase(String name);
}
