package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.wishlist.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistControllerTest {

    private CustomUserDetails userDetails() {
        return new CustomUserDetails(1L, "user", "u@example.com", "pass", Collections.emptyList(), true, true, true,
                true);
    }

    @Test
    void addRemoveGetContainsClearCount() {
        WishlistService service = mock(WishlistService.class);
        Wishlist wishlist = new Wishlist();
        wishlist.getProducts().add(new Product());
        wishlist.getProducts().add(new Product());

        when(service.addProduct(1L, 10L)).thenReturn(wishlist);
        when(service.removeProduct(1L, 10L)).thenReturn(wishlist);
        when(service.getWishlistProducts(1L)).thenReturn(Set.of(new Product()));
        when(service.isProductInWishlist(1L, 10L)).thenReturn(true);
        when(service.getWishlistCount(1L)).thenReturn(2L);

        WishlistController controller = new WishlistController(service);
        assertEquals(HttpStatus.OK, controller.addToWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.removeFromWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getWishlist(userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.isInWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.clearWishlist(userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getWishlistCount(userDetails()).getStatusCode());
    }

    @Test
    void handlesErrors() {
        WishlistService service = mock(WishlistService.class);
        when(service.getWishlistProducts(1L)).thenThrow(new RuntimeException("fail"));

        WishlistController controller = new WishlistController(service);
        assertEquals(HttpStatus.NOT_FOUND, controller.getWishlist(userDetails()).getStatusCode());
    }

    @Test
    void handlesAdditionalErrors() {
        WishlistService service = mock(WishlistService.class);
        when(service.addProduct(1L, 10L)).thenThrow(new RuntimeException("fail"));
        when(service.removeProduct(1L, 10L)).thenThrow(new RuntimeException("fail"));
        when(service.isProductInWishlist(1L, 10L)).thenThrow(new RuntimeException("fail"));
        doThrow(new RuntimeException("fail")).when(service).clearWishlist(1L);
        when(service.getWishlistCount(1L)).thenThrow(new RuntimeException("fail"));

        WishlistController controller = new WishlistController(service);
        assertEquals(HttpStatus.BAD_REQUEST, controller.addToWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.removeFromWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.isInWishlist(10L, userDetails()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.clearWishlist(userDetails()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getWishlistCount(userDetails()).getStatusCode());
    }
}
