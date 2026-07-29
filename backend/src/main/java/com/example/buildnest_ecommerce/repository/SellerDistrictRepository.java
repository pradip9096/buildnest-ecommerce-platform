package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.SellerDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SellerDistrictRepository
        extends JpaRepository<SellerDistrict, Long> {

    List<SellerDistrict> findAllBySeller_Id(Long sellerId);

    void deleteAllBySeller_Id(Long sellerId);
}
