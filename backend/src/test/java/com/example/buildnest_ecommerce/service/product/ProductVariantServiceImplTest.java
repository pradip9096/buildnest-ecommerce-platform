package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.dto.CreateProductVariantRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductVariantRequest;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductVariantServiceImplTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    private Product testProduct;
    private ProductVariant testVariant;
    private CreateProductVariantRequest createRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Premium Cement 50kg");
        testProduct.setPrice(BigDecimal.valueOf(500.00));

        testVariant = new ProductVariant();
        testVariant.setId(10L);
        testVariant.setProduct(testProduct);
        testVariant.setSku("CEM-50KG-RED");
        testVariant.setSize("50kg");
        testVariant.setColour("Red");
        testVariant.setPriceAdjustment(BigDecimal.ZERO);
        testVariant.setIsActive(true);

        createRequest = new CreateProductVariantRequest();
        createRequest.setSku("CEM-50KG-RED");
        createRequest.setSize("50kg");
        createRequest.setColour("Red");
        createRequest.setPriceAdjustment(BigDecimal.valueOf(10.00));
        createRequest.setIsActive(true);
        createRequest.setInitialStockQuantity(50);
        createRequest.setMinimumStockLevel(5);
    }

    @Test
    void getVariantsByProduct_returnsVariantList() {
        when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(testVariant));

        List<ProductVariant> result = productVariantService.getVariantsByProduct(1L);

        assertEquals(1, result.size());
        assertEquals("CEM-50KG-RED", result.get(0).getSku());
        verify(productVariantRepository).findByProductId(1L);
    }

    @Test
    void getVariantById_existingVariant_returnsVariant() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(testVariant));

        ProductVariant result = productVariantService.getVariantById(10L);

        assertEquals("CEM-50KG-RED", result.getSku());
    }

    @Test
    void getVariantById_missingVariant_throws() {
        when(productVariantRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productVariantService.getVariantById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void createVariant_validRequest_createsVariantAndInventory() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.existsBySku("CEM-50KG-RED")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> {
            ProductVariant v = invocation.getArgument(0);
            v.setId(10L);
            return v;
        });
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Simulates the service's re-fetch-after-create (see ProductVariantServiceImpl.createVariant comment)
        when(productVariantRepository.findById(10L)).thenAnswer(invocation -> {
            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(inventoryRepository).save(captor.capture());
            testVariant.setId(10L);
            testVariant.setInventory(captor.getValue());
            return Optional.of(testVariant);
        });

        ProductVariant result = productVariantService.createVariant(1L, createRequest);

        assertEquals("CEM-50KG-RED", result.getSku());
        assertNotNull(result.getInventory());
        assertEquals(50, result.getInventory().getQuantityInStock());
        assertEquals(InventoryStatus.IN_STOCK, result.getInventory().getStatus());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertNull(inventoryCaptor.getValue().getProduct());
    }

    @Test
    void createVariant_zeroInitialStock_marksOutOfStock() {
        createRequest.setInitialStockQuantity(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.existsBySku("CEM-50KG-RED")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productVariantRepository.findById(any())).thenAnswer(invocation -> {
            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(inventoryRepository).save(captor.capture());
            testVariant.setInventory(captor.getValue());
            return Optional.of(testVariant);
        });

        ProductVariant result = productVariantService.createVariant(1L, createRequest);

        assertEquals(InventoryStatus.OUT_OF_STOCK, result.getInventory().getStatus());
    }

    @Test
    void createVariant_missingProduct_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.createVariant(1L, createRequest));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void createVariant_duplicateSku_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.existsBySku("CEM-50KG-RED")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productVariantService.createVariant(1L, createRequest));
        assertTrue(ex.getMessage().contains("SKU"));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void updateVariant_validRequest_updatesFields() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(testVariant));
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProductVariantRequest updateRequest = new UpdateProductVariantRequest();
        updateRequest.setSku("CEM-50KG-RED");
        updateRequest.setSize("50kg");
        updateRequest.setColour("Maroon");
        updateRequest.setPriceAdjustment(BigDecimal.valueOf(15.00));
        updateRequest.setIsActive(true);

        ProductVariant result = productVariantService.updateVariant(10L, updateRequest);

        assertEquals("Maroon", result.getColour());
        assertEquals(BigDecimal.valueOf(15.00), result.getPriceAdjustment());
    }

    @Test
    void updateVariant_skuChangedToExistingSku_throws() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(testVariant));
        when(productVariantRepository.existsBySku("TAKEN-SKU")).thenReturn(true);

        UpdateProductVariantRequest updateRequest = new UpdateProductVariantRequest();
        updateRequest.setSku("TAKEN-SKU");
        updateRequest.setPriceAdjustment(BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> productVariantService.updateVariant(10L, updateRequest));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void deleteVariant_existingVariant_softDeletes() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(testVariant));
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productVariantService.deleteVariant(10L);

        ArgumentCaptor<ProductVariant> captor = ArgumentCaptor.forClass(ProductVariant.class);
        verify(productVariantRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsActive());
    }
}
