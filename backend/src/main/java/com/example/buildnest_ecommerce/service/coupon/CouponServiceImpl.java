package com.example.buildnest_ecommerce.service.coupon;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.entity.Coupon;
import com.example.buildnest_ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public Coupon validateCoupon(String code, BigDecimal orderSubtotal) {
        Coupon coupon = couponRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coupon not found: " + code));

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new ValidationException(
                    "Coupon is no longer active: " + code);
        }
        if (coupon.getExpiresAt() != null
                && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Coupon has expired: " + code);
        }
        if (coupon.getUsageLimit() != null
                && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new ValidationException(
                    "Coupon usage limit reached: " + code);
        }
        if (coupon.getMinOrderValue() != null
                && orderSubtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new ValidationException(
                    "Order subtotal " + orderSubtotal
                            + " is below the minimum "
                            + coupon.getMinOrderValue()
                            + " required for coupon " + code);
        }

        return coupon;
    }

    @Override
    public BigDecimal calculateDiscount(
            Coupon coupon, BigDecimal orderSubtotal) {
        BigDecimal discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderSubtotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }
        // Never discount more than the subtotal — avoids a negative total.
        return discount.min(orderSubtotal);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @Transactional
    public void incrementUsage(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coupon", couponId));
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public Coupon createCoupon(String code, Coupon.DiscountType discountType,
            BigDecimal discountValue, BigDecimal minOrderValue,
            Integer usageLimit, LocalDateTime expiresAt) {
        String normalizedCode = code.trim().toUpperCase();
        couponRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new ValidationException(
                    "A coupon with code " + normalizedCode
                            + " already exists");
        });
        if (discountType == Coupon.DiscountType.PERCENTAGE
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException(
                    "Percentage discount cannot exceed 100");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(normalizedCode);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMinOrderValue(
                minOrderValue != null ? minOrderValue : BigDecimal.ZERO);
        coupon.setUsageLimit(usageLimit);
        coupon.setUsageCount(0);
        coupon.setExpiresAt(expiresAt);
        coupon.setIsActive(true);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        log.info("Created coupon {}", saved.getCode());
        return saved;
    }

    @Override
    @Transactional
    public Coupon deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coupon", couponId));
        coupon.setIsActive(false);
        coupon.setUpdatedAt(LocalDateTime.now());
        Coupon saved = couponRepository.save(coupon);
        log.info("Deactivated coupon {}", saved.getCode());
        return saved;
    }
}
