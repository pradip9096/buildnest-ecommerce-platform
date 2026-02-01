package com.example.buildnest_ecommerce.controller.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("InventoryStatusController tests")
class InventoryStatusControllerTest {

    @Test
    @DisplayName("Should return product status")
    void testGetProductStatus() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(inventoryService.getInventoryStatus(1L)).thenReturn(InventoryStatus.IN_STOCK);

        ResponseEntity<ApiResponse> response = controller.getProductStatus(1L);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return inventory details")
    void testGetInventoryDetails() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        Product product = new Product();
        product.setId(1L);
        product.setName("Name");

        Inventory inventory = new Inventory();
        inventory.setQuantityInStock(10);
        inventory.setQuantityReserved(2);
        inventory.setMinimumStockLevel(3);
        product.setInventory(inventory);

        when(productService.getProductById(1L)).thenReturn(product);
        when(inventoryService.getInventoryStatus(1L)).thenReturn(InventoryStatus.IN_STOCK);
        when(inventoryService.isBelowThreshold(1L)).thenReturn(false);

        ResponseEntity<ApiResponse> response = controller.getInventoryDetails(1L);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return not found for missing product")
    void testGetInventoryDetailsMissingProduct() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(productService.getProductById(1L)).thenReturn(null);

        ResponseEntity<ApiResponse> response = controller.getInventoryDetails(1L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Should return availability response")
    void testIsProductAvailable() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(inventoryService.getInventoryStatus(1L)).thenReturn(InventoryStatus.LOW_STOCK);

        ResponseEntity<ApiResponse> response = controller.isProductAvailable(1L);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return unavailable when out of stock")
    void testIsProductUnavailable() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(inventoryService.getInventoryStatus(1L)).thenReturn(InventoryStatus.OUT_OF_STOCK);

        ResponseEntity<ApiResponse> response = controller.isProductAvailable(1L);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        @SuppressWarnings("unchecked")
        var data = (java.util.Map<String, Object>) response.getBody().getData();
        assertEquals(false, data.get("available"));
    }

    @Test
    @DisplayName("Should return not found when inventory missing")
    void testGetInventoryDetailsMissingInventory() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        Product product = new Product();
        product.setId(1L);
        product.setName("Name");
        product.setInventory(null);

        when(productService.getProductById(1L)).thenReturn(product);

        ResponseEntity<ApiResponse> response = controller.getInventoryDetails(1L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Should handle errors in status and availability")
    void testStatusAndAvailabilityErrors() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(inventoryService.getInventoryStatus(1L)).thenThrow(new RuntimeException("missing"));

        ResponseEntity<ApiResponse> statusResponse = controller.getProductStatus(1L);
        assertEquals(404, statusResponse.getStatusCode().value());

        ResponseEntity<ApiResponse> availabilityResponse = controller.isProductAvailable(1L);
        assertEquals(404, availabilityResponse.getStatusCode().value());
    }

    @Test
    @DisplayName("Should handle exceptions in inventory details")
    void testGetInventoryDetailsException() {
        InventoryService inventoryService = mock(InventoryService.class);
        ProductService productService = mock(ProductService.class);
        InventoryStatusController controller = new InventoryStatusController(inventoryService, productService);

        when(productService.getProductById(1L)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<ApiResponse> response = controller.getInventoryDetails(1L);
        assertEquals(500, response.getStatusCode().value());
    }
}
