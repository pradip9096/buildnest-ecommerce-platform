package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    Optional<ProductTag> findByName(String name);

    Optional<ProductTag> findBySlug(String slug);
}
