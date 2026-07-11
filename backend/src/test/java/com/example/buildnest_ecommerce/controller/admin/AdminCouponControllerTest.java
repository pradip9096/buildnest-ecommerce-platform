package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.CreateCouponRequest;
import com.example.buildnest_ecommerce.model.entity.Coupon;
import com.example.buildnest_ecommerce.service.coupon.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminCouponController tests (CHK-02, #77)")
class AdminCouponControllerTest {

    @Test
    @DisplayName("createCoupon returns 201 Created on success")
    void createCoupon_success_returnsCreated() {
        CouponService couponService = mock(CouponService.class);
        Coupon saved = new Coupon();
        saved.setId(1L);
        saved.setCode("SAVE10");
        when(couponService.createCoupon(any(), any(), any(), any(), any(), any())).thenReturn(saved);

        AdminCouponController controller = new AdminCouponController(couponService);
        CreateCouponRequest request = new CreateCouponRequest("SAVE10", Coupon.DiscountType.PERCENTAGE,
                BigDecimal.TEN, null, null, null);

        var response = controller.createCoupon(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("createCoupon returns 400 Bad Request on ValidationException (e.g. duplicate code)")
    void createCoupon_validationException_returnsBadRequest() {
        CouponService couponService = mock(CouponService.class);
        when(couponService.createCoupon(any(), any(), any(), any(), any(), any()))
                .thenThrow(new ValidationException("A coupon with code SAVE10 already exists"));

        AdminCouponController controller = new AdminCouponController(couponService);
        CreateCouponRequest request = new CreateCouponRequest("SAVE10", Coupon.DiscountType.PERCENTAGE,
                BigDecimal.TEN, null, null, null);

        var response = controller.createCoupon(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("deactivateCoupon returns 200 OK on success")
    void deactivateCoupon_success_returnsOk() {
        CouponService couponService = mock(CouponService.class);
        Coupon deactivated = new Coupon();
        deactivated.setId(1L);
        deactivated.setIsActive(false);
        when(couponService.deactivateCoupon(1L)).thenReturn(deactivated);

        AdminCouponController controller = new AdminCouponController(couponService);
        var response = controller.deactivateCoupon(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("deactivateCoupon returns 404 Not Found for an unknown coupon id")
    void deactivateCoupon_notFound_returns404() {
        CouponService couponService = mock(CouponService.class);
        when(couponService.deactivateCoupon(99L)).thenThrow(new ResourceNotFoundException("Coupon", 99L));

        AdminCouponController controller = new AdminCouponController(couponService);
        var response = controller.deactivateCoupon(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
