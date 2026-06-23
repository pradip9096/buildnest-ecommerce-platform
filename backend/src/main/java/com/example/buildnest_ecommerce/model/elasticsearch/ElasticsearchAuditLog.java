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
 * Elasticsearch document for centralized audit log storage (RQ-ES-EL-01,
 * RQ-ES-LOG-04).
 * Stores audit events as structured JSON documents for high-performance search
 * and analysis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "audit-logs-#{T(java.time.LocalDate).now().toString()}")
public class ElasticsearchAuditLog {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Keyword)
    private String entityType;

    @Field(type = FieldType.Keyword)
    private Long entityId;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Ip)
    private String ipAddress;

    @Field(type = FieldType.Text)
    private String userAgent;

    @Field(type = FieldType.Text)
    private String oldValue;

    @Field(type = FieldType.Text)
    private String newValue;

    @Field(type = FieldType.Keyword)
    private String severity; // INFO, WARN, ERROR, CRITICAL

    // API Error tracking (RQ-SRCH-04)
    @Field(type = FieldType.Integer)
    private Integer httpStatusCode; // HTTP status codes for API errors

    @Field(type = FieldType.Keyword)
    private String errorCategory; // CLIENT_ERROR (4xx), SERVER_ERROR (5xx), SUCCESS (2xx), REDIRECT (3xx)

    @Field(type = FieldType.Keyword)
    private String endpoint; // API endpoint path for error filtering

    @Field(type = FieldType.Object)
    private Map<String, Object> additionalContext;
}
