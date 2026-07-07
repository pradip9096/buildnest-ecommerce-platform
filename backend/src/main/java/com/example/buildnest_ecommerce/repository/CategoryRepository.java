package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    java.util.Optional<Category> findByName(String name);

    /**
     * Count direct child categories, used to block category deletion while
     * subcategories still reference it as their parent (ADM-02, #68).
     */
    long countByParentCategoryId(Long parentCategoryId);
}
