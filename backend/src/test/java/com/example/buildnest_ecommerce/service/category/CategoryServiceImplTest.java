package com.example.buildnest_ecommerce.service.category;

import com.example.buildnest_ecommerce.model.dto.CreateCategoryRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateCategoryRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
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

    @Mock
    private ProductRepository productRepository;

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
    @DisplayName("Should create category without a parent")
    void testCreateCategoryNoParent() {
        CreateCategoryRequest request = new CreateCategoryRequest("Tools", "desc", null, null);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category created = categoryService.createCategory(request);
        assertEquals("Tools", created.getName());
        assertNull(created.getParentCategory());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should create category with a valid parent")
    void testCreateCategoryWithParent() {
        Category parent = new Category();
        parent.setId(1L);
        parent.setName("Tools");

        CreateCategoryRequest request = new CreateCategoryRequest("Power Tools", "desc", null, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category created = categoryService.createCategory(request);
        assertEquals(parent, created.getParentCategory());
    }

    @Test
    @DisplayName("Should throw when creating category with a non-existent parent")
    void testCreateCategoryParentNotFound() {
        CreateCategoryRequest request = new CreateCategoryRequest("Power Tools", "desc", null, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update category name, description, image, and parent")
    void testUpdateCategory() {
        Category existing = new Category();
        existing.setId(2L);
        existing.setName("Old");

        Category parent = new Category();
        parent.setId(1L);

        UpdateCategoryRequest request = new UpdateCategoryRequest("New", "Desc", "img", 1L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category updated = categoryService.updateCategory(2L, request);
        assertEquals("New", updated.getName());
        assertEquals("Desc", updated.getDescription());
        assertEquals("img", updated.getImageUrl());
        assertEquals(parent, updated.getParentCategory());
    }

    @Test
    @DisplayName("Should throw when a category is set as its own parent")
    void testUpdateCategorySelfParentThrows() {
        Category existing = new Category();
        existing.setId(2L);
        existing.setName("Old");

        UpdateCategoryRequest request = new UpdateCategoryRequest("New", "Desc", "img", 2L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(2L, request));
    }

    @Test
    @DisplayName("Should throw when setting parent to a descendant would create a cycle")
    void testUpdateCategoryCycleThrows() {
        Category grandparent = new Category();
        grandparent.setId(1L);
        Category parent = new Category();
        parent.setId(2L);
        parent.setParentCategory(grandparent);
        Category child = new Category();
        child.setId(3L);
        child.setParentCategory(parent);

        // Attempt to set grandparent's parent to child (its own descendant) — a cycle.
        UpdateCategoryRequest request = new UpdateCategoryRequest("Grandparent", "Desc", null, 3L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(grandparent));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(child));

        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(1L, request));
    }

    @Test
    @DisplayName("Should delete category with no products and no subcategories")
    void testDeleteCategory() {
        Category existing = new Category();
        existing.setId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(productRepository.countByCategoryId(3L)).thenReturn(0L);
        when(categoryRepository.countByParentCategoryId(3L)).thenReturn(0L);

        categoryService.deleteCategory(3L);
        verify(categoryRepository).deleteById(3L);
    }

    @Test
    @DisplayName("Should block delete when products still reference the category")
    void testDeleteCategoryBlockedByProducts() {
        Category existing = new Category();
        existing.setId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(productRepository.countByCategoryId(3L)).thenReturn(2L);

        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(3L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should block delete when subcategories still reference the category as parent")
    void testDeleteCategoryBlockedBySubcategories() {
        Category existing = new Category();
        existing.setId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(productRepository.countByCategoryId(3L)).thenReturn(0L);
        when(categoryRepository.countByParentCategoryId(3L)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(3L));
        verify(categoryRepository, never()).deleteById(any());
    }
}
