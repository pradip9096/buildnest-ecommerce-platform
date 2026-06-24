package com.example.buildnest_ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configurable shipping rate matrix (SHIP-01, #87).
 *
 * <p>Zone multipliers are applied to the per-kg component of shipping cost:
 * {@code calculatedCost = baseCost + (costPerKg × totalWeightKg × zoneMultiplier)}
 */
@Configuration
@ConfigurationProperties(prefix = "app.shipping")
@Data
public class ShippingConfig {

    /** Assumed weight in kg for each unit of any cart item. */
    private BigDecimal defaultWeightPerItemKg = new BigDecimal("0.5");

    /**
     * Zone multipliers indexed 0-based.
     * Zone is determined from the delivery postal code prefix: zone index =
     * abs(postalCodePrefix.hashCode()) % zoneMultipliers.size().
     */
    private List<BigDecimal> zoneMultipliers = List.of(
            new BigDecimal("1.0"),
            new BigDecimal("1.5"),
            new BigDecimal("2.0"));
}
