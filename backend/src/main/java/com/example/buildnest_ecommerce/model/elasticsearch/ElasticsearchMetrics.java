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

/**
 * Elasticsearch document for application and system metrics (RQ-ES-MON-01, RQ-ES-EL-01).
 * Stores performance metrics as structured JSON documents for time-series analysis and visualization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "metrics-#{T(java.time.LocalDate).now().toString()}")
public class ElasticsearchMetrics {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String metricName;

    @Field(type = FieldType.Double)
    private Double value;

    @Field(type = FieldType.Keyword)
    private String unit;

    @Field(type = FieldType.Keyword)
    private String service;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String host;

    @Field(type = FieldType.Keyword)
    private String environment;

    @Field(type = FieldType.Object)
    private java.util.Map<String, Object> tags;

    // JVM Metrics
    @Field(type = FieldType.Double)
    private Double jvmMemoryUsagePercent;

    @Field(type = FieldType.Long)
    private Long jvmHeapUsedBytes;

    // HTTP Metrics
    @Field(type = FieldType.Long)
    private Long httpRequestCount;

    @Field(type = FieldType.Double)
    private Double httpResponseTimeMs;

    @Field(type = FieldType.Integer)
    private Integer httpStatusCode;

    // Database Metrics
    @Field(type = FieldType.Integer)
    private Integer dbConnectionPoolSize;

    @Field(type = FieldType.Double)
    private Double dbQueryTimeMs;
}
