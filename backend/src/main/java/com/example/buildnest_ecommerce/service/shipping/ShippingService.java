package com.example.buildnest_ecommerce.service.shipping;

import com.example.buildnest_ecommerce.model.dto.ShippingOptionDTO;
import com.example.buildnest_ecommerce.model.entity.ShippingMethod;
import com.example.buildnest_ecommerce.model.payload.CreateShippingMethodRequest;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingService {

    /**
     * Returns all active shipping methods with cost calculated for the caller's
     * active cart and the given delivery postal code.
     */
    List<ShippingOptionDTO> getShippingOptions(Long userId, String postalCode);

    /**
     * Calculates the shipping cost for the given method, weight, and postal code.
     * Formula: baseCost + (costPerKg × totalWeightKg × zoneMultiplier)
     */
    BigDecimal calculateCost(Long shippingMethodId, BigDecimal totalWeightKg, String postalCode);

    // ─── Admin management ─────────────────────────────────────────────────────

    List<ShippingMethod> getAllShippingMethods();

    ShippingMethod getShippingMethod(Long id);

    ShippingMethod createShippingMethod(CreateShippingMethodRequest request);

    ShippingMethod updateShippingMethod(Long id, CreateShippingMethodRequest request);

    void deactivateShippingMethod(Long id);
}
