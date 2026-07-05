package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Plain, Jackson-round-trippable substitute for {@link Page} in the audit log cache.
 *
 * <p>Spring Data's {@code PageImpl} has no default constructor or Jackson creator, so it
 * serializes to Redis fine on a cache miss but cannot be deserialized back out on a cache hit.
 * This type is intentionally non-generic (rather than a generic {@code PageResult<T>}) — a
 * generic type has the same reified-type problem Jackson has with {@code PageImpl}: without
 * explicit type information, deserializing a cached {@code List<T>} back out reconstructs each
 * element as a raw {@code LinkedHashMap}, not the original {@code T}. A concrete
 * {@code List<AuditLog>} field carries its element type at compile time, so Jackson always knows
 * exactly what to deserialize.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPageDTO {
    private List<AuditLog> content;
    private int totalPages;
    private long totalElements;
    private int number;

    public static AuditLogPageDTO from(Page<AuditLog> page) {
        return new AuditLogPageDTO(page.getContent(), page.getTotalPages(), page.getTotalElements(), page.getNumber());
    }
}
