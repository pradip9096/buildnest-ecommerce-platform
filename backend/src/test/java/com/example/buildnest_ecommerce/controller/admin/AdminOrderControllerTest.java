package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.AdminOrderDetailDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.payload.UpdateOrderStatusRequest;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminOrderController unit tests")
class AdminOrderControllerTest {

    private OrderService orderService;
    private AdminOrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        controller = new AdminOrderController(orderService);
    }

    @Test
    @DisplayName("getAdminOrders – no filters – returns 200 with page")
    void getAdminOrders_noFilters_returns200() {
        AdminOrderDetailDTO dto = new AdminOrderDetailDTO();
        dto.setId(1L);
        Page<AdminOrderDetailDTO> page = new PageImpl<>(List.of(dto));
        when(orderService.getAdminOrders(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

        var response = controller.getAdminOrders(null, null, null, null, PageRequest.of(0, 20));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("getAdminOrders – invalid status string – returns 400")
    void getAdminOrders_invalidStatus_returns400() {
        var response = controller.getAdminOrders("FLYING", null, null, null, PageRequest.of(0, 20));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("getAdminOrders – valid status – passes parsed enum to service")
    void getAdminOrders_validStatus_delegatesToService() {
        when(orderService.getAdminOrders(eq(Order.OrderStatus.PENDING), isNull(), isNull(), isNull(), any()))
                .thenReturn(Page.empty());

        var response = controller.getAdminOrders("PENDING", null, null, null, PageRequest.of(0, 20));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).getAdminOrders(eq(Order.OrderStatus.PENDING), isNull(), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("getAdminOrderDetail – found – returns 200")
    void getAdminOrderDetail_found_returns200() {
        AdminOrderDetailDTO dto = new AdminOrderDetailDTO();
        dto.setId(5L);
        when(orderService.getAdminOrderDetail(5L)).thenReturn(dto);

        var response = controller.getAdminOrderDetail(5L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateOrderStatus – valid request – returns 200")
    void updateOrderStatus_valid_returns200() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderService.adminUpdateOrderStatus(eq(1L), eq("CONFIRMED"), isNull())).thenReturn(order);

        var request = new UpdateOrderStatusRequest("CONFIRMED", null);
        var response = controller.updateOrderStatus(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("updateOrderStatus – service throws – propagates exception to GlobalExceptionHandler")
    void updateOrderStatus_serviceThrows_propagatesException() {
        when(orderService.adminUpdateOrderStatus(eq(99L), anyString(), any()))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        var request = new UpdateOrderStatusRequest("CONFIRMED", null);
        assertThrows(ResourceNotFoundException.class, () -> controller.updateOrderStatus(99L, request));
    }
}
