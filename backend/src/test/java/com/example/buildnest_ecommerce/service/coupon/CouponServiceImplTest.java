package com.example.buildnest_ecommerce.service.coupon;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.entity.Coupon;
import com.example.buildnest_ecommerce.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponServiceImpl tests (CHK-02, #77)")
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon activeCoupon;

    @BeforeEach
    void setUp() {
        activeCoupon = new Coupon();
        activeCoupon.setId(1L);
        activeCoupon.setCode("SAVE10");
        activeCoupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        activeCoupon.setDiscountValue(BigDecimal.TEN);
        activeCoupon.setMinOrderValue(BigDecimal.ZERO);
        activeCoupon.setUsageCount(0);
        activeCoupon.setIsActive(true);
    }

    // ===== validateCoupon =====

    @Test
    @DisplayName("validateCoupon returns the coupon when all rules pass")
    void validateCouponSucceedsWhenValid() {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        Coupon result = couponService.validateCoupon("save10", new BigDecimal("100"));

        assertEquals("SAVE10", result.getCode());
    }

    @Test
    @DisplayName("validateCoupon throws ResourceNotFoundException when the code doesn't exist")
    void validateCouponThrowsWhenNotFound() {
        when(couponRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> couponService.validateCoupon("MISSING", BigDecimal.TEN));
    }

    @Test
    @DisplayName("validateCoupon throws ValidationException when the coupon is inactive")
    void validateCouponThrowsWhenInactive() {
        activeCoupon.setIsActive(false);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertThrows(ValidationException.class,
                () -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    @Test
    @DisplayName("validateCoupon throws ValidationException when the coupon has expired")
    void validateCouponThrowsWhenExpired() {
        activeCoupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertThrows(ValidationException.class,
                () -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    @Test
    @DisplayName("validateCoupon succeeds when expiresAt is set but in the future")
    void validateCouponSucceedsWhenNotYetExpired() {
        activeCoupon.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertDoesNotThrow(() -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    @Test
    @DisplayName("validateCoupon throws ValidationException when the usage limit has been reached")
    void validateCouponThrowsWhenUsageLimitReached() {
        activeCoupon.setUsageLimit(5);
        activeCoupon.setUsageCount(5);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertThrows(ValidationException.class,
                () -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    @Test
    @DisplayName("validateCoupon succeeds when usage count is below the limit")
    void validateCouponSucceedsWhenUnderUsageLimit() {
        activeCoupon.setUsageLimit(5);
        activeCoupon.setUsageCount(4);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertDoesNotThrow(() -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    @Test
    @DisplayName("validateCoupon throws ValidationException when the order subtotal is below the coupon's minimum")
    void validateCouponThrowsWhenBelowMinOrderValue() {
        activeCoupon.setMinOrderValue(new BigDecimal("100"));
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertThrows(ValidationException.class,
                () -> couponService.validateCoupon("SAVE10", new BigDecimal("50")));
    }

    @Test
    @DisplayName("validateCoupon succeeds when the order subtotal exactly meets the minimum")
    void validateCouponSucceedsWhenSubtotalMeetsMinimum() {
        activeCoupon.setMinOrderValue(new BigDecimal("100"));
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertDoesNotThrow(() -> couponService.validateCoupon("SAVE10", new BigDecimal("100")));
    }

    // ===== calculateDiscount =====

    @Test
    @DisplayName("calculateDiscount computes a PERCENTAGE discount correctly")
    void calculateDiscountPercentage() {
        activeCoupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        activeCoupon.setDiscountValue(new BigDecimal("10"));

        BigDecimal discount = couponService.calculateDiscount(activeCoupon, new BigDecimal("200"));

        assertEquals(0, new BigDecimal("20.00").compareTo(discount));
    }

    @Test
    @DisplayName("calculateDiscount computes a FIXED discount correctly")
    void calculateDiscountFixed() {
        activeCoupon.setDiscountType(Coupon.DiscountType.FIXED);
        activeCoupon.setDiscountValue(new BigDecimal("15"));

        BigDecimal discount = couponService.calculateDiscount(activeCoupon, new BigDecimal("200"));

        assertEquals(0, new BigDecimal("15").compareTo(discount));
    }

    @Test
    @DisplayName("calculateDiscount never exceeds the subtotal, even for a FIXED discount larger than the order")
    void calculateDiscountCappedAtSubtotalForFixed() {
        activeCoupon.setDiscountType(Coupon.DiscountType.FIXED);
        activeCoupon.setDiscountValue(new BigDecimal("500"));

        BigDecimal discount = couponService.calculateDiscount(activeCoupon, new BigDecimal("100"));

        assertEquals(0, new BigDecimal("100").compareTo(discount));
    }

    @Test
    @DisplayName("calculateDiscount never exceeds the subtotal for a 100% PERCENTAGE discount")
    void calculateDiscountCappedAtSubtotalForFullPercentage() {
        activeCoupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        activeCoupon.setDiscountValue(new BigDecimal("100"));

        BigDecimal discount = couponService.calculateDiscount(activeCoupon, new BigDecimal("75"));

        assertEquals(0, new BigDecimal("75").compareTo(discount));
    }

    // ===== incrementUsage =====

    @Test
    @DisplayName("incrementUsage increases the coupon's usageCount by 1")
    void incrementUsageIncreasesCount() {
        activeCoupon.setUsageCount(3);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(activeCoupon));

        couponService.incrementUsage(1L);

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getUsageCount());
    }

    @Test
    @DisplayName("incrementUsage throws ResourceNotFoundException for an unknown coupon id")
    void incrementUsageThrowsWhenNotFound() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> couponService.incrementUsage(99L));
    }

    // ===== createCoupon =====

    @Test
    @DisplayName("createCoupon persists a new coupon with a normalized (uppercase) code")
    void createCouponPersistsNormalizedCode() {
        when(couponRepository.findByCode("WELCOME20")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Coupon result = couponService.createCoupon("welcome20", Coupon.DiscountType.FIXED,
                new BigDecimal("20"), null, null, null);

        assertEquals("WELCOME20", result.getCode());
        assertEquals(0, result.getUsageCount());
        assertTrue(result.getIsActive());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getMinOrderValue()));
    }

    @Test
    @DisplayName("createCoupon throws ValidationException when the code already exists")
    void createCouponThrowsOnDuplicateCode() {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(activeCoupon));

        assertThrows(ValidationException.class, () -> couponService.createCoupon("SAVE10",
                Coupon.DiscountType.FIXED, BigDecimal.TEN, null, null, null));
    }

    @Test
    @DisplayName("createCoupon throws ValidationException for a PERCENTAGE discount over 100")
    void createCouponThrowsWhenPercentageExceeds100() {
        when(couponRepository.findByCode("BIG")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> couponService.createCoupon("BIG",
                Coupon.DiscountType.PERCENTAGE, new BigDecimal("150"), null, null, null));
    }

    // ===== deactivateCoupon =====

    @Test
    @DisplayName("deactivateCoupon sets isActive to false")
    void deactivateCouponSetsInactive() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(activeCoupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Coupon result = couponService.deactivateCoupon(1L);

        assertFalse(result.getIsActive());
    }

    @Test
    @DisplayName("deactivateCoupon throws ResourceNotFoundException for an unknown coupon id")
    void deactivateCouponThrowsWhenNotFound() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> couponService.deactivateCoupon(99L));
    }
}
