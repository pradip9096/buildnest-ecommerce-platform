package com.example.buildnest_ecommerce.service.coupon;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.entity.Coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for coupon/discount-code validation, discount
 * calculation, and admin management (CHK-02, #77).
 */
public interface CouponService {

    /**
     * Validate a coupon code against the given order subtotal.
     *
     * @param code          the coupon code (case-insensitive)
     * @param orderSubtotal the cart/order subtotal the coupon would apply to
     * @return the valid, active {@link Coupon}
     * @throws ResourceNotFoundException if no coupon with this code
     *     exists
     * @throws ValidationException if the coupon is inactive, expired,
     *     usage-limit exceeded, or the subtotal is below the coupon's
     *     minimum order value
     */
    Coupon validateCoupon(String code, BigDecimal orderSubtotal);

    /**
     * Calculate the discount amount a valid coupon applies to a subtotal.
     * PERCENTAGE discounts are capped at the subtotal itself (never negative
     * totals); FIXED discounts are capped the same way.
     */
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderSubtotal);

    /**
     * Record that a coupon was actually consumed by a confirmed order.
     * Called once, at checkout confirmation — not at apply-time — so
     * abandoned checkout sessions don't consume a limited-use code.
     */
    void incrementUsage(Long couponId);

    /**
     * Admin: list every coupon.
     */
    List<Coupon> getAllCoupons();

    /**
     * Admin: create a new coupon.
     */
    Coupon createCoupon(String code, Coupon.DiscountType discountType,
            BigDecimal discountValue, BigDecimal minOrderValue,
            Integer usageLimit, LocalDateTime expiresAt);

    /**
     * Admin: deactivate a coupon so it can no longer be applied.
     */
    Coupon deactivateCoupon(Long couponId);
}
