package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.ApplyCouponRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import com.example.buildnest_ecommerce.service.checkout.CheckoutStep;
import com.example.buildnest_ecommerce.service.shipping.ShippingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MultiStepCheckoutController tests (CHK-02, #77 — applyCoupon)")
class MultiStepCheckoutControllerTest {

    private CustomUserDetails currentUser() {
        return new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
    }

    @Test
    @DisplayName("applyCoupon delegates to CheckoutService and returns the updated session")
    void applyCoupon_delegatesAndReturnsSession() {
        CheckoutService checkoutService = mock(CheckoutService.class);
        ShippingService shippingService = mock(ShippingService.class);

        CheckoutSessionDTO sessionDTO = new CheckoutSessionDTO();
        sessionDTO.setStep(CheckoutStep.PENDING_SHIPPING);
        sessionDTO.setCouponCode("SAVE10");
        sessionDTO.setDiscountAmount(new BigDecimal("20.00"));
        when(checkoutService.applyCoupon(1L, "SAVE10")).thenReturn(sessionDTO);

        MultiStepCheckoutController controller = new MultiStepCheckoutController(checkoutService, shippingService);
        ResponseEntity<ApiResponse> response = controller.applyCoupon(
                new ApplyCouponRequest("SAVE10"), currentUser());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(checkoutService).applyCoupon(1L, "SAVE10");
    }
}
