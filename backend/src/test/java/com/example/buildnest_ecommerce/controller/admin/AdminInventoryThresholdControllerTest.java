package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.inventory.InventoryThresholdManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminInventoryThresholdControllerTest {

    @Test
    void setProductThresholdValidatesInput() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);

        assertEquals(HttpStatus.BAD_REQUEST, controller.setProductThreshold(1L, -1).getStatusCode());

        assertEquals(HttpStatus.OK, controller.setProductThreshold(1L, 5).getStatusCode());
        verify(service).setProductThreshold(1L, 5);
    }

    @Test
    void getAndSetCategoryThresholdHandlesErrors() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        when(service.getCategoryThreshold(2L)).thenThrow(new RuntimeException("not found"));

        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);
        assertEquals(HttpStatus.OK, controller.setCategoryThreshold(2L, 10).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getCategoryThreshold(2L).getStatusCode());
    }

    @Test
    void getEffectiveAndToggleThreshold() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        when(service.getEffectiveThreshold(1L)).thenReturn(3);

        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);
        assertEquals(HttpStatus.OK, controller.getEffectiveThreshold(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.useProductCategoryThreshold(1L, true).getStatusCode());
    }

    @Test
    void useCategoryThresholdFalseBranch() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);

        assertEquals(HttpStatus.OK, controller.useProductCategoryThreshold(1L, false).getStatusCode());
        verify(service).useProductCategoryThreshold(1L, false);
    }

    @Test
    void getProductThresholdSuccess() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        when(service.getProductThreshold(7L)).thenReturn(4);

        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);
        assertEquals(HttpStatus.OK, controller.getProductThreshold(7L).getStatusCode());
    }

    @Test
    void categoryThresholdValidatesInputAndErrors() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);

        assertEquals(HttpStatus.BAD_REQUEST, controller.setCategoryThreshold(2L, -5).getStatusCode());

        doThrow(new RuntimeException("missing")).when(service).setCategoryThreshold(2L, 5);
        assertEquals(HttpStatus.NOT_FOUND, controller.setCategoryThreshold(2L, 5).getStatusCode());
    }

    @Test
    void handlesThresholdLookupErrors() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        doThrow(new RuntimeException("missing")).when(service).getProductThreshold(1L);
        doThrow(new RuntimeException("missing")).when(service).getEffectiveThreshold(1L);
        doThrow(new RuntimeException("missing")).when(service).useProductCategoryThreshold(1L, false);

        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);
        assertEquals(HttpStatus.NOT_FOUND, controller.getProductThreshold(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getEffectiveThreshold(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.useProductCategoryThreshold(1L, false).getStatusCode());
    }

    @Test
    void handlesSetProductThresholdError() {
        InventoryThresholdManagementService service = mock(InventoryThresholdManagementService.class);
        doThrow(new RuntimeException("missing")).when(service).setProductThreshold(1L, 5);

        AdminInventoryThresholdController controller = new AdminInventoryThresholdController(service);
        assertEquals(HttpStatus.NOT_FOUND, controller.setProductThreshold(1L, 5).getStatusCode());
    }
}
