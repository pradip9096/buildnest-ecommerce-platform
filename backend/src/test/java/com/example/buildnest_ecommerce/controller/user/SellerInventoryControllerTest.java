package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.InventoryDTO;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.payload.AdjustInventoryRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SellerInventoryControllerTest {

    private static CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(id, "seller-user", "s@example.com",
                "hash", Collections.emptyList(), true, true, true, true);
    }

    @Test
    void getOwnInventory_returnsOkWithSellerScopedPage() {
        InventoryService inventoryService = mock(InventoryService.class);
        InventoryDTO dto = new InventoryDTO(
                1L, 1L, "Test Product", 10, 0, 10, 2, "IN_STOCK", null);
        Page<InventoryDTO> page = new PageImpl<>(List.of(dto));
        when(inventoryService.getInventoryForSeller(
                5L, PageRequest.of(0, 10))).thenReturn(page);

        SellerInventoryController controller =
                new SellerInventoryController(inventoryService);
        ResponseEntity<?> response = controller.getOwnInventory(
                userDetails(5L), PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inventoryService)
                .getInventoryForSeller(5L, PageRequest.of(0, 10));
    }

    @Test
    void adjustOwnInventory_returnsOkAndDelegatesToService() {
        InventoryService inventoryService = mock(InventoryService.class);
        AdjustInventoryRequest request =
                new AdjustInventoryRequest(5, "restock");
        Inventory updated = new Inventory();
        updated.setId(1L);
        when(inventoryService.adjustStockForSeller(5L, 1L, 5, "restock"))
                .thenReturn(updated);

        SellerInventoryController controller =
                new SellerInventoryController(inventoryService);
        ResponseEntity<?> response = controller.adjustOwnInventory(
                userDetails(5L), 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inventoryService)
                .adjustStockForSeller(5L, 1L, 5, "restock");
    }
}
