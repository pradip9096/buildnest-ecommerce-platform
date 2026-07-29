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
     * Lists a seller's declared delivery districts by the owning
     * {@code User}'s ID rather than the {@code Seller} entity's own ID —
     * used wherever only the {@code Product.seller} (a {@code User}
     * reference, FR-SEL-03) is available (FR-LOC-03, #563).
     *
     * @param userId the seller's underlying User ID
     * @return the seller's declared district links
     */
    List<SellerDistrict> findAllBySeller_User_Id(Long userId);

    /**
     * Deletes every declared district link for a seller.
     *
     * @param sellerId the seller's own ID (not the user ID)
     */
    void deleteAllBySeller_Id(Long sellerId);
}
