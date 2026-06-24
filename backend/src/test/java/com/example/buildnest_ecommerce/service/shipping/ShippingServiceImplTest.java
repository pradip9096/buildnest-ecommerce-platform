package com.example.buildnest_ecommerce.service.shipping;

import com.example.buildnest_ecommerce.config.ShippingConfig;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.ShippingOptionDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.model.payload.CreateShippingMethodRequest;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingServiceImpl — cost calculation and admin CRUD (SHIP-01, #87).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShippingServiceImplTest {

    @Mock ShippingMethodRepository shippingMethodRepository;
    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock ShippingConfig shippingConfig;

    @InjectMocks ShippingServiceImpl service;

    private ShippingMethod standardMethod;
    private ShippingMethod expressMethod;

    @BeforeEach
    void setUp() {
        standardMethod = new ShippingMethod();
        standardMethod.setId(1L);
        standardMethod.setName("Standard");
        standardMethod.setBaseCost(new BigDecimal("50.00"));
        standardMethod.setCostPerKg(new BigDecimal("10.00"));
        standardMethod.setEstimatedDaysMin(3);
        standardMethod.setEstimatedDaysMax(5);
        standardMethod.setIsActive(true);

        expressMethod = new ShippingMethod();
        expressMethod.setId(2L);
        expressMethod.setName("Express");
        expressMethod.setBaseCost(new BigDecimal("120.00"));
        expressMethod.setCostPerKg(new BigDecimal("15.00"));
        expressMethod.setEstimatedDaysMin(1);
        expressMethod.setEstimatedDaysMax(2);
        expressMethod.setIsActive(true);

        when(shippingConfig.getZoneMultipliers()).thenReturn(
                List.of(new BigDecimal("1.0"), new BigDecimal("1.5"), new BigDecimal("2.0")));
        when(shippingConfig.getDefaultWeightPerItemKg()).thenReturn(new BigDecimal("0.5"));
    }

    // ─── computeCost ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-001: computeCost — zero costPerKg returns baseCost regardless of weight/zone")
    void computeCost_zeroCostPerKg_returnsBaseCostOnly() {
        standardMethod.setCostPerKg(BigDecimal.ZERO);
        BigDecimal result = service.computeCost(standardMethod, new BigDecimal("5.0"), "40");
        assertThat(result).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("TC-SHIP-002: computeCost — zone 1 (multiplier 1.0): baseCost + costPerKg × weight")
    void computeCost_zone1_correctFormula() {
        // postalCode "10" → hash → zone index determined by hash("10") % 3
        // We need to find a postalCode that maps to index 0 (multiplier 1.0)
        // "10".hashCode() = 1567, 1567 % 3 = 1 → multiplier 1.5
        // Let's instead test with a null postalCode → multiplier defaults to 1.0
        BigDecimal result = service.computeCost(standardMethod, new BigDecimal("2.0"), null);
        // baseCost=50, costPerKg=10, weight=2, multiplier=1.0 → 50 + 10*2*1.0 = 70
        assertThat(result).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("TC-SHIP-003: computeCost — null postalCode falls back to multiplier 1.0")
    void computeCost_nullPostalCode_usesMultiplier1() {
        BigDecimal result = service.computeCost(expressMethod, new BigDecimal("3.0"), null);
        // baseCost=120, costPerKg=15, weight=3, multiplier=1.0 → 120 + 45 = 165
        assertThat(result).isEqualByComparingTo("165.00");
    }

    @Test
    @DisplayName("TC-SHIP-004: computeCost — blank postalCode falls back to multiplier 1.0")
    void computeCost_blankPostalCode_usesMultiplier1() {
        BigDecimal result = service.computeCost(standardMethod, new BigDecimal("4.0"), "   ");
        // baseCost=50, costPerKg=10, weight=4, multiplier=1.0 → 50 + 40 = 90
        assertThat(result).isEqualByComparingTo("90.00");
    }

    @Test
    @DisplayName("TC-SHIP-005: computeCost — zone multiplier applied correctly (non-null postal)")
    void computeCost_withPostalCode_appliesZoneMultiplier() {
        // Find a postal prefix that maps to index 1 (multiplier=1.5) or 2 (multiplier=2.0)
        // Verify that with a specific postalCode, cost differs from null postalCode
        // Use "40" → resolveZoneMultiplier("40") returns a known multiplier from config
        BigDecimal multiplier = service.resolveZoneMultiplier("40");
        BigDecimal expected = new BigDecimal("50.00")
                .add(new BigDecimal("10.00").multiply(new BigDecimal("2.0")).multiply(multiplier))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal result = service.computeCost(standardMethod, new BigDecimal("2.0"), "40");
        assertThat(result).isEqualByComparingTo(expected);
    }

    // ─── resolveZoneMultiplier ────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-006: resolveZoneMultiplier — null returns 1.0 (fallback)")
    void resolveZoneMultiplier_null_returnsOne() {
        assertThat(service.resolveZoneMultiplier(null)).isEqualByComparingTo("1.0");
    }

    @Test
    @DisplayName("TC-SHIP-007: resolveZoneMultiplier — single-char postal uses it as prefix")
    void resolveZoneMultiplier_shortPostal_doesNotThrow() {
        assertThatCode(() -> service.resolveZoneMultiplier("4")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TC-SHIP-008: resolveZoneMultiplier — always returns a value from config list")
    void resolveZoneMultiplier_alwaysWithinConfigList() {
        List<BigDecimal> allowed = List.of(
                new BigDecimal("1.0"), new BigDecimal("1.5"), new BigDecimal("2.0"));
        for (String postal : List.of("10", "20", "30", "40", "50", "60", "70", "80", "90")) {
            BigDecimal multiplier = service.resolveZoneMultiplier(postal);
            assertThat(allowed).contains(multiplier);
        }
    }

    // ─── calculateCost (public API) ──────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-009: calculateCost — delegates to repository and computes correctly")
    void calculateCost_delegatesToRepository() {
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(standardMethod));
        BigDecimal cost = service.calculateCost(1L, new BigDecimal("2.0"), null);
        assertThat(cost).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("TC-SHIP-010: calculateCost — unknown method → ResourceNotFoundException")
    void calculateCost_unknownMethod_throwsNotFound() {
        when(shippingMethodRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.calculateCost(99L, BigDecimal.ONE, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getShippingOptions ───────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-011: getShippingOptions — returns calculated options for each active method")
    void getShippingOptions_returnsOptionsWithCalculatedCosts() {
        User user = new User(); user.setId(1L);
        Cart cart = buildCart(user, 4); // 4 units × 0.5kg = 2.0kg
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(shippingMethodRepository.findAllByIsActiveTrue()).thenReturn(List.of(standardMethod, expressMethod));

        List<ShippingOptionDTO> options = service.getShippingOptions(1L, null);

        assertThat(options).hasSize(2);
        // Standard: 50 + (10 × 2.0 × 1.0) = 70
        assertThat(options.get(0).getCalculatedCost()).isEqualByComparingTo("70.00");
        // Express: 120 + (15 × 2.0 × 1.0) = 150
        assertThat(options.get(1).getCalculatedCost()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("TC-SHIP-012: getShippingOptions — empty cart → IllegalArgumentException")
    void getShippingOptions_emptyCart_throws() {
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> service.getShippingOptions(1L, "40"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("TC-SHIP-013: getShippingOptions — user not found → ResourceNotFoundException")
    void getShippingOptions_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getShippingOptions(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── Admin CRUD ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-014: createShippingMethod — saves with isActive=true")
    void createShippingMethod_setsActiveTrue() {
        CreateShippingMethodRequest req = new CreateShippingMethodRequest(
                "Overnight", "Next day", new BigDecimal("200.00"), new BigDecimal("20.00"), 0, 1);
        ShippingMethod saved = new ShippingMethod();
        saved.setId(10L); saved.setName("Overnight"); saved.setIsActive(true);
        when(shippingMethodRepository.save(any())).thenReturn(saved);

        ShippingMethod result = service.createShippingMethod(req);

        assertThat(result.getIsActive()).isTrue();
        verify(shippingMethodRepository).save(any(ShippingMethod.class));
    }

    @Test
    @DisplayName("TC-SHIP-015: updateShippingMethod — applies request fields and saves")
    void updateShippingMethod_appliesChanges() {
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(standardMethod));
        when(shippingMethodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateShippingMethodRequest req = new CreateShippingMethodRequest(
                "Standard Plus", "Updated", new BigDecimal("60.00"), new BigDecimal("12.00"), 2, 4);
        ShippingMethod result = service.updateShippingMethod(1L, req);

        assertThat(result.getName()).isEqualTo("Standard Plus");
        assertThat(result.getBaseCost()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("TC-SHIP-016: deactivateShippingMethod — sets isActive=false")
    void deactivateShippingMethod_setsInactive() {
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(standardMethod));
        when(shippingMethodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deactivateShippingMethod(1L);

        assertThat(standardMethod.getIsActive()).isFalse();
        verify(shippingMethodRepository).save(standardMethod);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Cart buildCart(User user, int unitCount) {
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("100.00"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(unitCount);
        item.setPrice(new BigDecimal("100.00"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(item)));
        return cart;
    }
}
