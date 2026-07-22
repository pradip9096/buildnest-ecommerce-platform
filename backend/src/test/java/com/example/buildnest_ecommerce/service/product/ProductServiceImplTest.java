package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
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
    private SellerRepository sellerRepository;

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
        // Return the same argument (not the unrelated testProduct fixture) —
        // createProduct() links Inventory onto its own `saved` reference
        // in-memory (#485), so the stub must mirror real save() semantics
        // (same entity back) for that link to be observable here.
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        // #485: Inventory is persisted via cascade (Product.inventory is
        // cascade=ALL) rather than an explicit inventoryRepository.save()
        // call — an explicit call here plus Hibernate's own cascade-persist
        // at flush double-inserted the identical row (verified empirically
        // against a real H2 instance). Assert directly on the linked
        // Inventory instead of a repository interaction.
        Inventory createdInventory = saved.getInventory();
        assertNotNull(createdInventory);
        assertEquals(saved, createdInventory.getProduct());
        assertEquals(25, createdInventory.getQuantityInStock());
        assertEquals(InventoryStatus.IN_STOCK, createdInventory.getStatus());
    }

    @Test
    void testCreateProduct_zeroStock_createsOutOfStockInventory() {
        // Arrange
        createRequest.setStockQuantity(0);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.createProduct(createRequest);

        // Assert — Inventory is cascade-persisted (#485), not saved via an
        // explicit inventoryRepository call; see testCreateProduct's comment.
        Inventory createdInventory = result.getInventory();
        assertNotNull(createdInventory);
        assertEquals(0, createdInventory.getQuantityInStock());
        assertEquals(InventoryStatus.OUT_OF_STOCK, createdInventory.getStatus());
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
        // #485: stockQuantity on an update request must be ignored —
        // Inventory is the sole writable source of stock.
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
        // #485: request.getStockQuantity() (99) must NOT reach the product
        // or its Inventory — updateProduct has no writable stock field left.
        assertNull(saved.getInventory());
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
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, true, null, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        verify(productRepository).advancedSearch("cement", 1L,
                new BigDecimal("50.00"), new BigDecimal("100.00"), true, true, null, pageable);
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
                true, true, null, pageable)).thenReturn(expected);

        Page<Product> result = productService.advancedSearch("Cement", 1L,
                new BigDecimal("50"), new BigDecimal("150"), true, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Cement Mix", result.getContent().get(0).getName());
    }

    @Test
    void testAdvancedSearchWithNullFilters() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct, new Product()));
        when(productRepository.advancedSearch(null, null, null, null, null, true, null, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, null, null, null, null, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testAdvancedSearchWithPriceRange() {
        PageRequest pageable = PageRequest.of(0, 10);
        Product p2 = new Product(); p2.setId(2L); p2.setName("Steel Rod");
        Page<Product> expected = new PageImpl<>(List.of(p2));
        when(productRepository.advancedSearch(null, null, new BigDecimal("150"), null, null, true, null, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, new BigDecimal("150"), null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getId());
    }

    @Test
    void testAdvancedSearchInStockFalse() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> expected = new PageImpl<>(List.of(testProduct, new Product()));
        when(productRepository.advancedSearch(null, null, null, null, false, true, null, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, null, null, null, false, null, pageable);

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
        when(productRepository.advancedSearch(null, 1L, null, null, null, true, null, pageable))
                .thenReturn(expected);

        Page<Product> result = productService.advancedSearch(null, 1L, null, null, null, null, pageable);

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

    @Test
    void testGetRelatedProductsRanksSourceCategoryAndTags() {
        // Arrange — source product has category 1 and tag 7
        com.example.buildnest_ecommerce.model.entity.ProductTag tag = new com.example.buildnest_ecommerce.model.entity.ProductTag();
        tag.setId(7L);
        tag.setName("eco-friendly");
        testProduct.setTags(java.util.Set.of(tag));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product sameCategoryMatch = new Product();
        sameCategoryMatch.setId(2L);
        sameCategoryMatch.setCategory(testCategory);

        Product sharedTagMatch = new Product();
        sharedTagMatch.setId(3L);

        List<Product> repositoryResult = List.of(sameCategoryMatch, sharedTagMatch);

        ArgumentCaptor<List<Long>> tagIdsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(productRepository.findRelatedProducts(eq(1L), eq(1L), tagIdsCaptor.capture(), pageableCaptor.capture()))
                .thenReturn(repositoryResult);

        // Act
        List<Product> result = productService.getRelatedProducts(1L);

        // Assert — same-category result ranked ahead of the shared-tag-only result,
        // exactly as returned by the ranked repository query
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(3L, result.get(1).getId());
        assertEquals(List.of(7L), tagIdsCaptor.getValue());
        assertEquals(8, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void testGetRelatedProductsWithNoTagsUsesSentinelTagId() {
        // Arrange — source product has a category but no tags
        testProduct.setTags(java.util.Set.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ArgumentCaptor<List<Long>> tagIdsCaptor = ArgumentCaptor.forClass(List.class);
        when(productRepository.findRelatedProducts(eq(1L), eq(1L), tagIdsCaptor.capture(), any(Pageable.class)))
                .thenReturn(List.of());

        // Act
        List<Product> result = productService.getRelatedProducts(1L);

        // Assert — an empty JPQL IN clause is invalid, so a sentinel value that
        // matches no real tag id must be passed instead
        assertTrue(result.isEmpty());
        assertEquals(List.of(-1L), tagIdsCaptor.getValue());
    }

    // --- Seller-owned catalogue (FR-SEL-03/04, #555) ---

    private Seller verifiedSeller(Long userId) {
        User user = new User();
        user.setId(userId);
        Seller seller = new Seller();
        seller.setId(100L);
        seller.setUser(user);
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        return seller;
    }

    @Test
    void testCreateProductForSellerRejectsUnverifiedSeller() {
        Seller pending = verifiedSeller(5L);
        pending.setVerificationStatus(Seller.VerificationStatus.PENDING);
        when(sellerRepository.findByUser_Id(5L))
                .thenReturn(Optional.of(pending));

        assertThrows(AccessDeniedException.class,
                () -> productService
                        .createProductForSeller(createRequest, 5L));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testCreateProductForSellerRejectsUnknownSeller() {
        when(sellerRepository.findByUser_Id(5L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService
                        .createProductForSeller(createRequest, 5L));
    }

    @Test
    void testCreateProductForSellerSetsOwningSeller() {
        Seller seller = verifiedSeller(5L);
        when(sellerRepository.findByUser_Id(5L))
                .thenReturn(Optional.of(seller));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Product result = productService
                .createProductForSeller(createRequest, 5L);

        assertNotNull(result.getSeller());
        assertEquals(5L, result.getSeller().getId());
    }

    @Test
    void testUpdateProductForSellerRejectsNonOwner() {
        when(productRepository.findByIdAndSeller_Id(1L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> productService.updateProductForSeller(
                        5L, 1L, createRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProductForSellerAllowsOwner() {
        testProduct.setSeller(verifiedSeller(5L).getUser());
        when(productRepository.findByIdAndSeller_Id(1L, 5L))
                .thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.updateProductForSeller(
                5L, 1L, createRequest);

        assertEquals(createRequest.getName(), result.getName());
    }

    @Test
    void testDeleteProductForSellerRejectsNonOwner() {
        when(productRepository.findByIdAndSeller_Id(1L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> productService.deleteProductForSeller(5L, 1L));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProductForSellerAllowsOwner() {
        when(productRepository.findByIdAndSeller_Id(1L, 5L))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        productService.deleteProductForSeller(5L, 1L);

        assertFalse(testProduct.getIsActive());
    }

    @Test
    void testGetProductsForSellerScopedToOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findBySeller_Id(5L, pageable))
                .thenReturn(new PageImpl<>(List.of(testProduct)));

        Page<Product> result =
                productService.getProductsForSeller(5L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findBySeller_Id(5L, pageable);
    }
}
