package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    @EntityGraph(attributePaths = { "product", "inventory" })
    List<ProductVariant> findByProductId(Long productId);

    @EntityGraph(attributePaths = { "product", "inventory" })
    Optional<ProductVariant> findById(Long id);

    boolean existsBySku(String sku);
}
