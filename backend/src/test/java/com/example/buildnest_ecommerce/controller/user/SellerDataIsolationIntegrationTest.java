package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seller data-isolation hardening (FR-SEL-08, #559) — proves, through the
 * real Spring Security filter chain and method-security proxy (not a
 * mocked-service unit test), that: (a) a non-SELLER role is rejected by
 * {@code @PreAuthorize("hasRole('SELLER')")} on every seller-scoped
 * controller; (b) a SELLER accessing their own resource succeeds; (c) a
 * SELLER attempting to access another seller's product/inventory/order is
 * rejected — mirroring the real service-layer ownership-scoped-query
 * behavior ({@code findByIdAndSeller_Id} etc.) rather than assuming it from
 * a mocked unit test, per the self-invocation-bypasses-@PreAuthorize lesson.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings({ "null", "removal" })
class SellerDataIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private OrderService orderService;

    private CustomUserDetails plainUserDetails;
    private CustomUserDetails sellerDetails;

    @BeforeEach
    void setUp() {
        plainUserDetails = new CustomUserDetails(
                1L, "plain-user", "plain@example.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true);
        sellerDetails = new CustomUserDetails(
                5L, "seller-user", "seller@example.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_SELLER")),
                true, true, true, true);
    }

    @Test
    @DisplayName("GET /api/user/seller/products — non-SELLER role rejected by @PreAuthorize")
    void getOwnProducts_nonSellerRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/user/seller/products")
                        .with(user(plainUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/user/seller/products — SELLER role allowed")
    void getOwnProducts_sellerRole_ok() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.emptyList(),
                org.springframework.data.domain.PageRequest.of(0, 10), 0);
        when(productService.getProductsForSeller(eq(5L), any())).thenReturn(page);

        mockMvc.perform(get("/api/user/seller/products")
                        .with(user(sellerDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/user/seller/products/{id} — cross-seller update rejected end-to-end")
    void updateOwnProduct_crossSeller_forbidden() throws Exception {
        when(productService.updateProductForSeller(eq(5L), eq(99L), any()))
                .thenThrow(new AccessDeniedException(
                        "Product 99 does not belong to seller 5"));

        mockMvc.perform(put("/api/user/seller/products/99")
                        .with(user(sellerDetails))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Test Product\",\"description\":\"a valid description\",\"price\":10,\"categoryId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/user/seller/inventory/{productId} — non-SELLER role rejected")
    void adjustOwnInventory_nonSellerRole_forbidden() throws Exception {
        mockMvc.perform(patch("/api/user/seller/inventory/1")
                        .with(user(plainUserDetails))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"delta\":5,\"reason\":\"restock\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/user/seller/inventory/{productId} — cross-seller adjustment rejected end-to-end")
    void adjustOwnInventory_crossSeller_forbidden() throws Exception {
        when(inventoryService.adjustStockForSeller(eq(5L), eq(99L), anyInt(), any()))
                .thenThrow(new ResourceNotFoundException(
                        "Product 99 does not belong to seller 5"));

        mockMvc.perform(patch("/api/user/seller/inventory/99")
                        .with(user(sellerDetails))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"delta\":5,\"reason\":\"restock\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/user/seller/orders/{id} — non-SELLER role rejected")
    void getOwnOrderDetail_nonSellerRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/user/seller/orders/1")
                        .with(user(plainUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/user/seller/orders/{id} — cross-seller order detail rejected end-to-end")
    void getOwnOrderDetail_crossSeller_forbidden() throws Exception {
        when(orderService.getSellerOrderById(eq(5L), eq(99L)))
                .thenThrow(new AccessDeniedException(
                        "Order not found or does not belong to seller with id: 5"));

        mockMvc.perform(get("/api/user/seller/orders/99")
                        .with(user(sellerDetails)))
                .andExpect(status().isForbidden());
    }
}
