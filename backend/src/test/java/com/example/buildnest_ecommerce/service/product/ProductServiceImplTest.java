package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Cement");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("OPC 53 Grade Cement");
        testProduct.setDescription("High-quality cement");
        testProduct.setPrice(BigDecimal.valueOf(450.00));
        testProduct.setCategory(testCategory);
        testProduct.setIsActive(true);

        createRequest = new CreateProductRequest();
        createRequest.setName("OPC 53 Grade Cement");
        createRequest.setDescription("High-quality cement");
        createRequest.setPrice(BigDecimal.valueOf(450.00));
        createRequest.setDiscountPrice(BigDecimal.valueOf(400.00));
        createRequest.setStockQuantity(25);
        createRequest.setSku("CEM-53");
        createRequest.setImageUrl("https://cdn.example.com/cement.jpg");
        createRequest.setCategoryId(1L);
    }

    @Test
    void testGetAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OPC 53 Grade Cement", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductById() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("OPC 53 Grade Cement", result.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.getProductById(999L));
    }

    @Test
    void testCreateProduct() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("OPC 53 Grade Cement", result.getName());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals("OPC 53 Grade Cement", saved.getName());
        assertEquals("High-quality cement", saved.getDescription());
        assertEquals(0, BigDecimal.valueOf(450.00).compareTo(saved.getPrice()));
        assertEquals(0, BigDecimal.valueOf(400.00).compareTo(saved.getDiscountPrice()));
        assertEquals(25, saved.getStockQuantity());
        assertEquals("CEM-53", saved.getSku());
        assertEquals("https://cdn.example.com/cement.jpg", saved.getImageUrl());
        assertNotNull(saved.getCreatedAt());
        assertEquals(testCategory, saved.getCategory());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void testCreateProductWithoutCategory() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("No Category Product");
        request.setDescription("No category");
        request.setPrice(BigDecimal.valueOf(99.99));

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(request);

        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        // Arrange
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Updated Cement");
        updateRequest.setDescription("Updated desc");
        updateRequest.setPrice(BigDecimal.valueOf(500.00));
        updateRequest.setDiscountPrice(BigDecimal.valueOf(450.00));
        updateRequest.setStockQuantity(99);
        updateRequest.setSku("CEM-UPDATED");
        updateRequest.setImageUrl("https://cdn.example.com/updated.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals("Updated Cement", saved.getName());
        assertEquals("Updated desc", saved.getDescription());
        assertEquals(0, BigDecimal.valueOf(500.00).compareTo(saved.getPrice()));
        assertEquals(0, BigDecimal.valueOf(450.00).compareTo(saved.getDiscountPrice()));
        assertEquals(99, saved.getStockQuantity());
        assertEquals("CEM-UPDATED", saved.getSku());
        assertEquals("https://cdn.example.com/updated.jpg", saved.getImageUrl());
        assertNotNull(saved.getUpdatedAt());

        verify(productRepository).findById(1L);
    }

    @Test
    void testUpdateProductWithCategory() {
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Updated Cement");
        updateRequest.setPrice(BigDecimal.valueOf(500.00));
        updateRequest.setCategoryId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updateRequest);

        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(testCategory, captor.getValue().getCategory());
    }

    @Test
    void testUpdateProductWithoutCategory() {
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Updated Cement");
        updateRequest.setPrice(BigDecimal.valueOf(500.00));
        updateRequest.setCategoryId(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updateRequest);

        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProductCategoryNotFound() {
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Updated Cement");
        updateRequest.setPrice(BigDecimal.valueOf(500.00));
        updateRequest.setCategoryId(99L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.updateProduct(1L, updateRequest));
    }

    @Test
    void testDeleteProduct() {
        // Soft delete: loads product, sets isActive=false, saves
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        org.junit.jupiter.api.Assertions.assertFalse(testProduct.getIsActive());
    }

    @Test
    void testDeleteProduct_notFound() {
        when(productRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(99L));
    }

    @Test
    void testSearchProducts() {
        when(productRepository.findByNameContainingIgnoreCase("cement")).thenReturn(List.of(testProduct));

        List<Product> result = productService.searchProducts("cement");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetFeaturedProducts() {
        testProduct.setIsFeatured(true);
        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue()).thenReturn(List.of(testProduct));

        List<Product> result = productService.getFeaturedProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsFeatured());
    }

    @Test
    void testGetFeaturedProductsEmpty() {
        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue()).thenReturn(List.of());

        List<Product> result = productService.getFeaturedProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateProduct_setsIsFeaturedFromRequest() {
        createRequest.setIsFeatured(true);
        when(categoryRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.createProduct(createRequest);

        assertTrue(result.getIsFeatured());
    }

    @Test
    void testCreateProduct_defaultsIsFeaturedToFalseWhenNotProvided() {
        when(categoryRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.createProduct(createRequest);

        assertFalse(result.getIsFeatured());
    }

    @Test
    void testUpdateProduct_doesNotClearIsFeaturedWhenNotProvidedInRequest() {
        testProduct.setIsFeatured(true);
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        when(categoryRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.updateProduct(1L, createRequest);

        assertTrue(result.getIsFeatured(), "isFeatured must be left unchanged when the request omits it");
    }

    @Test
    void testSearchProductsMatchesNameCaseInsensitive() {
        Product other = new Product();
        other.setId(2L);
        other.setName("Steel Rods");

        when(productRepository.findByNameContainingIgnoreCase("cement")).thenReturn(List.of(testProduct));
        when(productRepository.findByNameContainingIgnoreCase("steel")).thenReturn(List.of(other));

        List<Product> byName = productService.searchProducts("cement");
        List<Product> byOther = productService.searchProducts("steel");

        assertEquals(1, byName.size());
        assertEquals("OPC 53 Grade Cement", byName.get(0).getName());
        assertEquals(1, byOther.size());
        assertEquals("Steel Rods", byOther.get(0).getName());
    }

    @Test
    void testFindByIdReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testAdvancedSearchDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct));
        when(productRepository.advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, true, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        verify(productRepository).advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, true, pageable);
    }

    @Test
    void testFindByCategoryDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByCategory(1L, pageable)).thenReturn(expected);

        Page<Product> page = productService.findByCategory(1L, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, page.getContent().get(0).getId());
        verify(productRepository).findByCategory(1L, pageable);
    }

    @Test
    void testSearchProductsByNameIgnoresCase() {
        when(productRepository.findByNameContainingIgnoreCase("CEMENT")).thenReturn(List.of(testProduct));

        List<Product> result = productService.searchProducts("CEMENT");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetProductsByCategory() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByCategory(eq(1L), any(Pageable.class))).thenReturn(page);

        List<Product> result = productService.getProductsByCategory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetProductsByCategoryEmpty() {
        Page<Product> emptyPage = new PageImpl<>(List.of());
        when(productRepository.findByCategory(eq(99L), any(Pageable.class))).thenReturn(emptyPage);

        List<Product> result = productService.getProductsByCategory(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindByIdNotFound() {
        when(productRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.findById(123L));
    }

    @Test
    void testFindAllPageable() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Product> result = productService.findAll(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testAdvancedSearchWithQueryAndFilters() {
        PageRequest pageable = PageRequest.of(0, 10);
        Product p = new Product();
        p.setId(1L); p.setName("Cement Mix");
        Page<Product> expected = new PageImpl<>(List.of(p));
        when(productRepository.advancedSearch("Cement", 1L, new BigDecimal("50"), new BigDecimal("150"),
                true, true, pageable)).thenReturn(expected);

        Page<Product> result = productService.advancedSearch("Cement", 1L,
                new BigDecimal("50"), new BigDecimal("150"), true, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Cement Mix", result.getContent().get(0).getName());
    }

    @Test
    void testAdvancedSearchWithNullFilters() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct, new Product()));
        when(productRepository.advancedSearch(null, null, null, null, null, true, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, null, null, null, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testAdvancedSearchWithPriceRange() {
        PageRequest pageable = PageRequest.of(0, 10);
        Product p2 = new Product(); p2.setId(2L); p2.setName("Steel Rod");
        Page<Product> expected = new PageImpl<>(List.of(p2));
        when(productRepository.advancedSearch(null, null, new BigDecimal("150"), null, null, true, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, new BigDecimal("150"), null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getId());
    }

    @Test
    void testAdvancedSearchInStockFalse() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct, new Product()));
        when(productRepository.advancedSearch(null, null, null, null, false, true, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, null, null, false, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testFindByCategoryWithPagination() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(productRepository.findByCategory(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(testProduct)));

        Page<Product> page = productService.findByCategory(1L, pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testCreateProductCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.createProduct(createRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.updateProduct(999L, createRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testSearchProductsNoMatch() {
        when(productRepository.findByNameContainingIgnoreCase("nonexistentkeyword")).thenReturn(List.of());

        List<Product> result = productService.searchProducts("nonexistentkeyword");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAdvancedSearchWithCategoryFilter() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct));
        when(productRepository.advancedSearch(null, 1L, null, null, null, true, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, 1L, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void testFindByCategoryPageOffsetBeyondResults() {
        PageRequest pageable = PageRequest.of(5, 10);
        when(productRepository.findByCategory(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 1));

        Page<Product> page = productService.findByCategory(1L, pageable);

        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }
}
