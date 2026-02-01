package com.example.buildnest_ecommerce.service.product;

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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

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
        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).deleteById(1L);
    }

    @Test
    void testSearchProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts("cement");

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 1);
    }

    @Test
    void testSearchProductsMatchesNameOrDescriptionCaseInsensitive() {
        Product other = new Product();
        other.setId(2L);
        other.setName("Steel Rods");
        other.setDescription("Premium rebar");

        when(productRepository.findAll()).thenReturn(List.of(testProduct, other));

        List<Product> byName = productService.searchProducts("cement");
        List<Product> byDesc = productService.searchProducts("ReBaR");

        assertEquals(1, byName.size());
        assertEquals("OPC 53 Grade Cement", byName.get(0).getName());
        assertEquals(1, byDesc.size());
        assertEquals("Steel Rods", byDesc.get(0).getName());
    }

    @Test
    void testFindByIdReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testAdvancedSearchFiltersAndBoundaries() {
        Category category = new Category();
        category.setId(1L);

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Cement Pro");
        p1.setDescription("Strong cement");
        p1.setPrice(new BigDecimal("100.00"));
        p1.setStockQuantity(10);
        p1.setCategory(category);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Cement Lite");
        p2.setDescription("Budget cement");
        p2.setPrice(new BigDecimal("50.00"));
        p2.setStockQuantity(0);
        p2.setCategory(category);

        Product p3 = new Product();
        p3.setId(3L);
        p3.setName("Steel Rod");
        p3.setDescription("Rebar");
        p3.setPrice(new BigDecimal("150.00"));
        p3.setStockQuantity(5);
        p3.setCategory(category);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> result = productService.advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void testFindByCategoryFiltersNullCategory() {
        Product withoutCategory = new Product();
        withoutCategory.setId(2L);
        withoutCategory.setCategory(null);

        when(productRepository.findAll()).thenReturn(List.of(testProduct, withoutCategory));

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> page = productService.findByCategory(1L, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, page.getContent().get(0).getId());
    }

    @Test
    void testSearchProductsMatchesDescription() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Concrete");
        product.setDescription("High strength cement mix");

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.searchProducts("CEMENT");

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void testGetProductsByCategory() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByCategory(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 1);
    }

    @Test
    void testGetProductsByCategoryIgnoresNullCategory() {
        Product productWithCategory = new Product();
        productWithCategory.setId(1L);
        productWithCategory.setCategory(testCategory);

        Product productWithoutCategory = new Product();
        productWithoutCategory.setId(2L);
        productWithoutCategory.setCategory(null);

        when(productRepository.findAll()).thenReturn(List.of(productWithCategory, productWithoutCategory));

        List<Product> result = productService.getProductsByCategory(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
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
    void testAdvancedSearchFilters() {
        Product inStock = new Product();
        inStock.setId(1L);
        inStock.setName("Cement Mix");
        inStock.setDescription("Mix");
        inStock.setPrice(new BigDecimal("100"));
        inStock.setStockQuantity(10);
        inStock.setCategory(testCategory);

        Product outOfStock = new Product();
        outOfStock.setId(2L);
        outOfStock.setName("Steel Rod");
        outOfStock.setDescription("Rod");
        outOfStock.setPrice(new BigDecimal("200"));
        outOfStock.setStockQuantity(0);
        outOfStock.setCategory(testCategory);

        when(productRepository.findAll()).thenReturn(List.of(inStock, outOfStock));

        Page<Product> result = productService.advancedSearch("Cement", 1L,
                new BigDecimal("50"), new BigDecimal("150"), true, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Cement Mix", result.getContent().get(0).getName());
    }

    @Test
    void testAdvancedSearchWithNullFilters() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Cement Mix");
        p1.setDescription("Mix");
        p1.setPrice(new BigDecimal("100"));
        p1.setStockQuantity(10);
        p1.setCategory(testCategory);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Steel Rod");
        p2.setDescription("Rod");
        p2.setPrice(new BigDecimal("200"));
        p2.setStockQuantity(0);
        p2.setCategory(testCategory);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        Page<Product> result = productService.advancedSearch(null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testAdvancedSearchWithPriceRange() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Cement Mix");
        p1.setDescription("Mix");
        p1.setPrice(new BigDecimal("100"));
        p1.setStockQuantity(10);
        p1.setCategory(testCategory);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Steel Rod");
        p2.setDescription("Rod");
        p2.setPrice(new BigDecimal("200"));
        p2.setStockQuantity(10);
        p2.setCategory(testCategory);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        Page<Product> result = productService.advancedSearch(null, null, new BigDecimal("150"), null, null,
                PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getId());
    }

    @Test
    void testAdvancedSearchInStockFalseIncludesOutOfStock() {
        Product inStock = new Product();
        inStock.setId(1L);
        inStock.setName("Cement Mix");
        inStock.setDescription("Mix");
        inStock.setPrice(new BigDecimal("100"));
        inStock.setStockQuantity(10);
        inStock.setCategory(testCategory);

        Product outOfStock = new Product();
        outOfStock.setId(2L);
        outOfStock.setName("Steel Rod");
        outOfStock.setDescription("Rod");
        outOfStock.setPrice(new BigDecimal("200"));
        outOfStock.setStockQuantity(0);
        outOfStock.setCategory(testCategory);

        when(productRepository.findAll()).thenReturn(List.of(inStock, outOfStock));

        Page<Product> result = productService.advancedSearch(null, null, null, null, false, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testFindByCategoryWithPagination() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        Page<Product> page = productService.findByCategory(1L, PageRequest.of(0, 5));
        assertEquals(1, page.getTotalElements());
    }
}
