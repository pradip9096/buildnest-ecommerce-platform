package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserOrderControllerTest {

    private Authentication auth() {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails details = new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        when(authentication.getPrincipal()).thenReturn(details);
        return authentication;
    }

    @Test
    void getOrdersAndOrderDetails() throws Exception {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getOrderResponsesByUserId(1L)).thenReturn(List.of(new OrderResponseDTO()));
        when(orderService.getUserOrderById(1L, 2L)).thenReturn(new OrderResponseDTO());

        UserOrderController controller = new UserOrderController(orderService);
        assertEquals(HttpStatus.OK, controller.getUserOrders(auth()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getOrderDetails(2L, auth()).getStatusCode());
    }

    @Test
    void handlesForbiddenAndNotFound() throws Exception {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getUserOrderById(1L, 2L)).thenThrow(new IllegalAccessException("no"));

        UserOrderController controller = new UserOrderController(orderService);
        assertEquals(HttpStatus.FORBIDDEN, controller.getOrderDetails(2L, auth()).getStatusCode());
    }

    @Test
    void handlesOrderListError() {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getOrderResponsesByUserId(1L)).thenThrow(new RuntimeException("fail"));

        UserOrderController controller = new UserOrderController(orderService);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getUserOrders(auth()).getStatusCode());
    }

    @Test
    void handlesOrderNotFound() throws Exception {
        OrderService orderService = mock(OrderService.class);
        when(orderService.getUserOrderById(1L, 3L)).thenThrow(new RuntimeException("missing"));

        UserOrderController controller = new UserOrderController(orderService);
        assertEquals(HttpStatus.NOT_FOUND, controller.getOrderDetails(3L, auth()).getStatusCode());
    }
}
