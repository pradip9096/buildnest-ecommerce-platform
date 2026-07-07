package com.example.buildnest_ecommerce.service.category;

import com.example.buildnest_ecommerce.model.dto.CreateCategoryRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateCategoryRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "categories")
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Cacheable(key = "'all'")
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Override
    @Cacheable(key = "#categoryId")
    public Category getCategoryById(Long categoryId) {
        log.info("Fetching category with id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public Category createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setParentCategory(resolveParent(request.getParentId(), null));
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public Category updateCategory(Long categoryId, UpdateCategoryRequest request) {
        log.info("Updating category with id: {}", categoryId);
        Category existingCategory = getCategoryById(categoryId);
        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setImageUrl(request.getImageUrl());
        existingCategory.setParentCategory(resolveParent(request.getParentId(), categoryId));
        existingCategory.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with id: {}", categoryId);
        getCategoryById(categoryId);

        long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete category " + categoryId + ": " + productCount + " product(s) still reference it");
        }

        long subcategoryCount = categoryRepository.countByParentCategoryId(categoryId);
        if (subcategoryCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete category " + categoryId + ": " + subcategoryCount
                            + " subcategory(ies) still reference it as parent");
        }

        categoryRepository.deleteById(categoryId);
    }

    /**
     * Resolves and validates the parent category reference for create/update.
     * Rejects a category naming itself as its own parent, and rejects a parent
     * chain that would create a cycle (e.g. setting a category's parent to one
     * of its own descendants).
     */
    private Category resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found with id: " + parentId));

        if (selfId != null) {
            Category ancestor = parent;
            while (ancestor != null) {
                if (ancestor.getId().equals(selfId)) {
                    throw new IllegalArgumentException(
                            "Cannot set parent to " + parentId + ": would create a category hierarchy cycle");
                }
                ancestor = ancestor.getParentCategory();
            }
        }

        return parent;
    }
}
