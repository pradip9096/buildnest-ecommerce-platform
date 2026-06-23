package com.example.buildnest_ecommerce.service.category;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Should return all categories")
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category(), new Category()));

        assertEquals(2, categoryService.getAllCategories().size());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return category by id")
    void testGetCategoryById() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category found = categoryService.getCategoryById(1L);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Should throw when category not found")
    void testGetCategoryByIdNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(1L));
        assertTrue(ex.getMessage().contains("Category not found"));
    }

    @Test
    @DisplayName("Should create category")
    void testCreateCategory() {
        Category category = new Category();
        category.setName("Tools");
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category created = categoryService.createCategory(category);
        assertEquals("Tools", created.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update category")
    void testUpdateCategory() {
        Category existing = new Category();
        existing.setId(2L);
        existing.setName("Old");

        Category update = new Category();
        update.setName("New");
        update.setDescription("Desc");
        update.setImageUrl("img");

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(existing);

        Category updated = categoryService.updateCategory(2L, update);
        assertEquals("New", updated.getName());
        assertEquals("Desc", updated.getDescription());
        assertEquals("img", updated.getImageUrl());
    }

    @Test
    @DisplayName("Should delete category")
    void testDeleteCategory() {
        categoryService.deleteCategory(3L);
        verify(categoryRepository).deleteById(3L);
    }
}
