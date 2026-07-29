package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.SellerDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SellerDistrictRepository
        extends JpaRepository<SellerDistrict, Long> {

    /**
     * Lists a seller's declared delivery districts.
     *
     * @param sellerId the seller's own ID (not the user ID)
     * @return the seller's declared district links
     */
    List<SellerDistrict> findAllBySeller_Id(Long sellerId);

    /**
     * Deletes every declared district link for a seller.
     *
     * @param sellerId the seller's own ID (not the user ID)
     */
    void deleteAllBySeller_Id(Long sellerId);
}
