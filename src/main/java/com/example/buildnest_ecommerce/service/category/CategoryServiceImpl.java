package com.example.buildnest_ecommerce.service.category;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
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
    public Category createCategory(Category category) {
        log.info("Creating new category: {}", category.getName());
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public Category updateCategory(Long categoryId, Category category) {
        log.info("Updating category with id: {}", categoryId);
        Category existingCategory = getCategoryById(categoryId);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setImageUrl(category.getImageUrl());
        existingCategory.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with id: {}", categoryId);
        categoryRepository.deleteById(categoryId);
    }
}
