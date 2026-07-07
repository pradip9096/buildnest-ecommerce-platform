package com.example.buildnest_ecommerce.service.category;

import com.example.buildnest_ecommerce.model.dto.CreateCategoryRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateCategoryRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long categoryId);
    Category createCategory(CreateCategoryRequest request);
    Category updateCategory(Long categoryId, UpdateCategoryRequest request);
    void deleteCategory(Long categoryId);
}
