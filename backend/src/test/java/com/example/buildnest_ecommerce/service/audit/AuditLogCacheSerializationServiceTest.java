package com.example.buildnest_ecommerce.service.audit;

import com.example.buildnest_ecommerce.model.dto.AuditLogPageDTO;
import com.example.buildnest_ecommerce.model.entity.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression test for #307: the Admin Audit Log tab succeeded on a Redis cache miss (raw
 * {@code Page<AuditLog>} serializes fine) but failed on every subsequent cache hit for the
 * cache's full TTL, because {@code PageImpl} has no default constructor or Jackson creator and
 * cannot be deserialized back out of the cache.
 *
 * <p>Reproduces the exact serializer configuration used in {@code CacheConfig} (a
 * {@code GenericJackson2JsonRedisSerializer} over an {@code ObjectMapper} with default typing
 * enabled) against an actual in-memory byte round-trip, without needing a running Redis
 * instance — this is what {@code RedisCacheManager} does internally on every cache write/read.
 */
class AuditLogCacheSerializationServiceTest {

    private GenericJackson2JsonRedisSerializer buildCacheSerializer() {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                        ObjectMapper.DefaultTyping.NON_FINAL);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    private AuditLog sampleAuditLog() {
        return AuditLog.builder()
                .id(1L)
                .userId(42L)
                .action("LOGIN")
                .entityType("AUTHENTICATION")
                .timestamp(LocalDateTime.of(2026, 7, 5, 12, 0))
                .ipAddress("127.0.0.1")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();
    }

    @Test
    void rawPageImpl_failsToDeserializeAfterCacheRoundTrip_documentsTheOriginalBug() {
        GenericJackson2JsonRedisSerializer serializer = buildCacheSerializer();
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog()), PageRequest.of(0, 20), 1);

        byte[] cached = serializer.serialize(page);

        assertThatThrownBy(() -> serializer.deserialize(cached))
                .as("PageImpl has no default constructor/creator — this is the exact #307 cache-hit failure")
                .isInstanceOf(SerializationException.class);
    }

    @Test
    void auditLogPageDTO_roundTripsCleanlyThroughCacheSerialization() {
        GenericJackson2JsonRedisSerializer serializer = buildCacheSerializer();
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog()), PageRequest.of(0, 20), 1);
        AuditLogPageDTO dto = AuditLogPageDTO.from(page);

        byte[] cached = serializer.serialize(dto);
        Object deserialized = serializer.deserialize(cached);

        assertThat(deserialized).isInstanceOf(AuditLogPageDTO.class);
        AuditLogPageDTO roundTripped = (AuditLogPageDTO) deserialized;
        assertThat(roundTripped.getTotalElements()).isEqualTo(1L);
        assertThat(roundTripped.getTotalPages()).isEqualTo(1);
        assertThat(roundTripped.getNumber()).isEqualTo(0);
        assertThat(roundTripped.getContent()).hasSize(1);
        assertThat(roundTripped.getContent().get(0).getAction()).isEqualTo("LOGIN");
    }

    @Test
    void auditLogPageDTO_secondReadAfterCacheHit_stillSucceeds() {
        // Simulates the exact reproduction in #307: request the same page twice.
        GenericJackson2JsonRedisSerializer serializer = buildCacheSerializer();
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog()), PageRequest.of(0, 20), 1);
        AuditLogPageDTO dto = AuditLogPageDTO.from(page);

        byte[] cached = serializer.serialize(dto);

        // First read (cache hit #1)
        AuditLogPageDTO firstRead = (AuditLogPageDTO) serializer.deserialize(cached);
        // Second read (cache hit #2) — must not throw or degrade
        AuditLogPageDTO secondRead = (AuditLogPageDTO) serializer.deserialize(cached);

        assertThat(firstRead.getTotalElements()).isEqualTo(secondRead.getTotalElements());
        assertThat(firstRead.getContent()).hasSize(secondRead.getContent().size());
    }
}
