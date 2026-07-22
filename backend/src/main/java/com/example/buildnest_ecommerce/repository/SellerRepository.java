package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}
