package com.example.buildnest_ecommerce.service.producttag;

import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.repository.ProductTagRepository;
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
@DisplayName("ProductTagServiceImpl tests")
class ProductTagServiceImplTest {

    @Mock
    private ProductTagRepository productTagRepository;

    @InjectMocks
    private ProductTagServiceImpl productTagService;

    @Test
    @DisplayName("Should return all tags")
    void testGetAllTags() {
        when(productTagRepository.findAll()).thenReturn(List.of(new ProductTag(), new ProductTag()));

        assertEquals(2, productTagService.getAllTags().size());
        verify(productTagRepository).findAll();
    }

    @Test
    @DisplayName("Should return tag by id")
    void testGetTagById() {
        ProductTag tag = new ProductTag();
        tag.setId(1L);
        when(productTagRepository.findById(1L)).thenReturn(Optional.of(tag));

        ProductTag found = productTagService.getTagById(1L);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Should throw when tag not found")
    void testGetTagByIdNotFound() {
        when(productTagRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productTagService.getTagById(1L));
        assertTrue(ex.getMessage().contains("Product tag not found"));
    }

    @Test
    @DisplayName("Should create tag and derive a slug from the name")
    void testCreateTag() {
        CreateProductTagRequest request = new CreateProductTagRequest("Eco Friendly");
        when(productTagRepository.findByName("Eco Friendly")).thenReturn(Optional.empty());
        when(productTagRepository.findBySlug("eco-friendly")).thenReturn(Optional.empty());
        when(productTagRepository.save(any(ProductTag.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductTag created = productTagService.createTag(request);
        assertEquals("Eco Friendly", created.getName());
        assertEquals("eco-friendly", created.getSlug());
        verify(productTagRepository).save(any(ProductTag.class));
    }

    @Test
    @DisplayName("Should throw when creating a tag with a duplicate name")
    void testCreateTagDuplicateNameThrows() {
        ProductTag existing = new ProductTag();
        existing.setId(1L);
        existing.setName("Best Seller");
        CreateProductTagRequest request = new CreateProductTagRequest("Best Seller");
        when(productTagRepository.findByName("Best Seller")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> productTagService.createTag(request));
        verify(productTagRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update tag name and slug")
    void testUpdateTag() {
        ProductTag existing = new ProductTag();
        existing.setId(2L);
        existing.setName("Old Name");
        existing.setSlug("old-name");

        UpdateProductTagRequest request = new UpdateProductTagRequest("New Name");
        when(productTagRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(productTagRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(productTagRepository.findBySlug("new-name")).thenReturn(Optional.empty());
        when(productTagRepository.save(any(ProductTag.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductTag updated = productTagService.updateTag(2L, request);
        assertEquals("New Name", updated.getName());
        assertEquals("new-name", updated.getSlug());
    }

    @Test
    @DisplayName("Should delete existing tag")
    void testDeleteTag() {
        ProductTag existing = new ProductTag();
        existing.setId(3L);
        when(productTagRepository.findById(3L)).thenReturn(Optional.of(existing));

        productTagService.deleteTag(3L);
        verify(productTagRepository).deleteById(3L);
    }
}
