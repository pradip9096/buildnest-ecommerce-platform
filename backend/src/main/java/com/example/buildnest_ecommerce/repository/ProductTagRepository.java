package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link ProductTag} (PROD-03).
 */
@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    /**
     * Finds a tag by its exact name.
     *
     * @param name the tag name
     * @return the matching tag, if any
     */
    Optional<ProductTag> findByName(String name);

    /**
     * Finds a tag by its slug.
     *
     * @param slug the tag slug
     * @return the matching tag, if any
     */
    Optional<ProductTag> findBySlug(String slug);
}
