package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.payload.UpdateOrderStatusRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.order.OrderService;
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

class SellerOrderControllerTest {

    private static CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(id, "seller-user", "s@example.com",
                "hash", Collections.emptyList(), true, true, true, true);
    }

    @Test
    void getOwnOrders_returnsOkWithSellerScopedPage() {
        OrderService orderService = mock(OrderService.class);
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(dto));
        when(orderService.getSellerOrders(5L, PageRequest.of(0, 10)))
                .thenReturn(page);

        SellerOrderController controller =
                new SellerOrderController(orderService);
        ResponseEntity<?> response = controller.getOwnOrders(
                userDetails(5L), PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService)
                .getSellerOrders(5L, PageRequest.of(0, 10));
    }

    @Test
    void getOwnOrderDetail_returnsOkWithBody() {
        OrderService orderService = mock(OrderService.class);
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getSellerOrderById(5L, 1L)).thenReturn(dto);

        SellerOrderController controller =
                new SellerOrderController(orderService);
        ResponseEntity<?> response =
                controller.getOwnOrderDetail(userDetails(5L), 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateOwnOrderStatus_returnsOkAndDelegatesToService() {
        OrderService orderService = mock(OrderService.class);
        OrderResponseDTO updated = new OrderResponseDTO();
        updated.setId(1L);
        updated.setStatus("SHIPPED");
        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest("SHIPPED", null);
        when(orderService.updateSellerOrderStatus(5L, 1L, "SHIPPED"))
                .thenReturn(updated);

        SellerOrderController controller =
                new SellerOrderController(orderService);
        ResponseEntity<?> response = controller.updateOwnOrderStatus(
                userDetails(5L), 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).updateSellerOrderStatus(5L, 1L, "SHIPPED");
    }
}
