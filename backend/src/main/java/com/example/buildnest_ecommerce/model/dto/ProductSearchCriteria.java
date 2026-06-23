package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Search Criteria DTO
 * 
 * Encapsulates search parameters for advanced product filtering.
 * Supports multiple search dimensions: name, category, price range, stock
 * status.
 * 
 * Section 6.1.3: Advanced Search & Filters
 * Section 2.2.3: DTO pattern for API consistency
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    /** Search query for product name/description */
    @Schema(example = "cement")
    private String query;

    /** Filter by category ID */
    @Schema(example = "3")
    private Long categoryId;

    /** Minimum price filter */
    @PositiveOrZero(message = "Minimum price must be zero or positive")
    @Schema(example = "100.00")
    private BigDecimal minPrice;

    /** Maximum price filter */
    @PositiveOrZero(message = "Maximum price must be zero or positive")
    @Schema(example = "1000.00")
    private BigDecimal maxPrice;

    /** Minimum rating filter (1-5 stars) */
    @Min(value = 1, message = "Minimum rating must be at least 1")
    @Max(value = 5, message = "Minimum rating must be at most 5")
    @Schema(example = "4")
    private Integer minRating;

    /** Only show in-stock products */
    @Schema(example = "true")
    private Boolean inStockOnly;

    /** Filter by active/inactive status */
    @Schema(example = "true")
    private Boolean isActive;

    /** Sort field */
    @Schema(example = "createdAt")
    private String sortBy;

    /** Sort direction (ASC/DESC) */
    @Schema(example = "DESC")
    private String direction;
}
