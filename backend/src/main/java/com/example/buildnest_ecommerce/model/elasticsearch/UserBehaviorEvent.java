package com.example.buildnest_ecommerce.model.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Elasticsearch document for user behaviour analytics events (ANL-02, #65).
 * Records page views, add-to-cart, and checkout-started events for
 * conversion-funnel and cart-abandonment reporting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "user-behavior-events")
public class UserBehaviorEvent {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String eventType; // PRODUCT_VIEW, ADD_TO_CART, CHECKOUT_STARTED

    @Field(type = FieldType.Keyword)
    private Long userId; // null for anonymous/unauthenticated views

    @Field(type = FieldType.Keyword)
    private Long productId; // null for events not tied to a single product (e.g. CHECKOUT_STARTED)

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Object)
    private Map<String, Object> metadata;
}
