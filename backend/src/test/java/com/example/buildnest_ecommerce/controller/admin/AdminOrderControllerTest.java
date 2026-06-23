package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOrderControllerTest {

    @Test
    void getAllOrdersSuccessAndFailure() {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(new Order()));

        AdminOrderController controller = new AdminOrderController(orderService);
        assertEquals(HttpStatus.OK, controller.getAllOrders().getStatusCode());

        when(orderService.getAllOrders()).thenThrow(new RuntimeException("fail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllOrders().getStatusCode());
    }

    @Test
    void getOrderByIdSuccessAndFailure() {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getOrderById(1L)).thenReturn(new Order());
        when(orderService.getOrderById(2L)).thenThrow(new RuntimeException("not found"));

        AdminOrderController controller = new AdminOrderController(orderService);
        assertEquals(HttpStatus.OK, controller.getOrderById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getOrderById(2L).getStatusCode());
    }

    @Test
    void updateStatusAndDelete() {
        OrderService orderService = mock(OrderService.class);
        when(orderService.updateOrderStatus(eq(1L), eq("CONFIRMED"))).thenReturn(new Order());

        AdminOrderController controller = new AdminOrderController(orderService);
        assertEquals(HttpStatus.OK, controller.updateOrderStatus(1L, "CONFIRMED").getStatusCode());

        when(orderService.updateOrderStatus(eq(2L), eq("BAD"))).thenThrow(new RuntimeException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.updateOrderStatus(2L, "BAD").getStatusCode());

        doThrow(new RuntimeException("bad")).when(orderService).deleteOrder(2L);
        assertEquals(HttpStatus.OK, controller.deleteOrder(1L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteOrder(2L).getStatusCode());
    }
}
