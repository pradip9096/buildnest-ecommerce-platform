package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for #485 (INV-01): {@code Inventory.quantityInStock} is now
 * the sole writable source of product stock — {@code Product.stockQuantity}
 * has no backing column and is derived from it.
 *
 * <p>Before this fix, {@code ProductServiceImpl.updateProduct()} wrote a
 * second, independently-writable copy of stock onto
 * {@code Product.stock_quantity} without touching {@code Inventory}, so an
 * ordinary product edit (name/price/etc., with the shared
 * {@code CreateProductRequest} DTO still carrying whatever
 * {@code stockQuantity} value the caller happened to send) would silently
 * desync the two — the exact drift #309 already fixed once via data
 * backfill, but never structurally prevented from recurring. Real
 * persistence (not mocks) is required here because the bug is specifically
 * about what ends up durably stored in two real columns/rows, not about
 * which in-memory setter gets called.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class ProductInventorySingleSourceOfTruthIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("INV-01 Test Category");
        category.setDescription("Category for #485 regression test");
        categoryId = categoryRepository.save(category).getId();
    }

    @Test
    void createProduct_seedsInventoryAsTheOnlyStockRecord() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Regression Test Cement");
        request.setDescription("Seeded for the #485 dual-source-of-truth regression test");
        request.setPrice(java.math.BigDecimal.valueOf(499.99));
        request.setStockQuantity(30);
        request.setCategoryId(categoryId);

        Product created = productService.createProduct(request);

        assertEquals(30, created.getStockQuantity());
        Inventory persisted = inventoryRepository.findByProductId(created.getId());
        assertEquals(30, persisted.getQuantityInStock());
    }

    @Test
    void updateProduct_ignoresStockQuantity_inventoryStaysTheSingleSourceOfTruth() {
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setName("Regression Test Paint");
        createRequest.setDescription("Seeded for the #485 dual-source-of-truth regression test");
        createRequest.setPrice(java.math.BigDecimal.valueOf(899.00));
        createRequest.setStockQuantity(40);
        createRequest.setCategoryId(categoryId);
        Product created = productService.createProduct(createRequest);

        // Simulate an ordinary product edit that happens to carry a stale/
        // different stockQuantity in the shared request DTO (e.g. a client
        // that reuses form state) — this must NOT reach Inventory.
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Regression Test Paint (Updated)");
        updateRequest.setDescription("Updated description for the regression test");
        updateRequest.setPrice(java.math.BigDecimal.valueOf(950.00));
        updateRequest.setStockQuantity(999);
        updateRequest.setCategoryId(categoryId);

        Product updated = productService.updateProduct(created.getId(), updateRequest);

        assertEquals(40, updated.getStockQuantity());
        Inventory persisted = inventoryRepository.findByProductId(created.getId());
        assertEquals(40, persisted.getQuantityInStock());
    }
}
