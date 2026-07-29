package com.example.buildnest_ecommerce.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch document representing a product in the search index
 * (SRCH-01, #74).
 *
 * Index name is static ("products") unlike the daily-partitioned
 * audit-logs index, because the product catalog is a live mutable
 * dataset rather than an append-only log.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Double)
    private Double discountPrice;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Boolean)
    private Boolean inStock;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * Districts the owning seller declared for delivery (FR-LOC-03, #563)
     * — mirrors {@code Seller.sellerDistricts} via {@code SellerDistrict}.
     * Empty/null for products with no seller (admin-created catalog).
     */
    @Field(type = FieldType.Long)
    private List<Long> districtIds;
}
