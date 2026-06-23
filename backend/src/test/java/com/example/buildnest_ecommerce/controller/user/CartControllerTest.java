package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.payload.AddItemRequest;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;
import com.example.buildnest_ecommerce.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CartControllerTest {

    @Test
    void addGetRemoveClearTotal() {
        CartService cartService = mock(CartService.class);
        when(cartService.getCartByUserId(1L)).thenReturn(new CartResponseDTO());
        when(cartService.getCartTotal(1L)).thenReturn(100.0);

        CartController controller = new CartController(cartService);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        assertEquals(HttpStatus.OK, controller.addToCart(1L, request).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCart(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.removeFromCart(5L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.clearCart(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCartTotal(1L).getStatusCode());
    }

    @Test
    void handlesErrors() {
        CartService cartService = mock(CartService.class);
        when(cartService.getCartByUserId(1L)).thenThrow(new RuntimeException("not found"));

        CartController controller = new CartController(cartService);
        assertEquals(HttpStatus.NOT_FOUND, controller.getCart(1L).getStatusCode());
    }

    @Test
    void handlesAdditionalErrors() {
        CartService cartService = mock(CartService.class);
        doThrow(new RuntimeException("add"))
                .when(cartService).addToCart(eq(1L), eq(10L), eq(2));
        doThrow(new RuntimeException("remove"))
                .when(cartService).removeItemFromCart(eq(5L));
        doThrow(new RuntimeException("clear"))
                .when(cartService).clearCart(eq(1L));
        when(cartService.getCartTotal(1L)).thenThrow(new RuntimeException("total"));

        CartController controller = new CartController(cartService);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        assertEquals(HttpStatus.BAD_REQUEST, controller.addToCart(1L, request).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.removeFromCart(5L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.clearCart(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getCartTotal(1L).getStatusCode());
    }
}
