package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.admin.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReportControllerTest {

    @Test
    void dashboardAndCountsReturnOk() {
        AdminService adminService = mock(AdminService.class);
        when(adminService.getTotalUsers()).thenReturn(10L);
        when(adminService.getTotalProducts()).thenReturn(20L);
        when(adminService.getTotalOrders()).thenReturn(30L);
        when(adminService.getTotalRevenue()).thenReturn(99.0);

        AdminReportController controller = new AdminReportController(adminService);
        assertEquals(HttpStatus.OK, controller.getDashboardStats().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getUsersCount().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getProductsCount().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getOrdersCount().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getTotalRevenue().getStatusCode());
    }

    @Test
    void handlesErrors() {
        AdminService adminService = mock(AdminService.class);
        when(adminService.getTotalUsers()).thenThrow(new RuntimeException("fail"));
        when(adminService.getTotalProducts()).thenThrow(new RuntimeException("fail"));
        when(adminService.getTotalOrders()).thenThrow(new RuntimeException("fail"));
        when(adminService.getTotalRevenue()).thenThrow(new RuntimeException("fail"));

        AdminReportController controller = new AdminReportController(adminService);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getDashboardStats().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getUsersCount().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getProductsCount().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getOrdersCount().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getTotalRevenue().getStatusCode());
    }
}
