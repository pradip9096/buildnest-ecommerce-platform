package com.example.buildnest_ecommerce.service.checkout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisCheckoutSessionStore")
class RedisCheckoutSessionStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RedisCheckoutSessionStore store;

    private static final Long USER_ID = 42L;
    private static final String EXPECTED_KEY = "checkout:session:42";
    private static final Duration EXPECTED_TTL = Duration.ofMinutes(30);

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        store = new RedisCheckoutSessionStore(redisTemplate, objectMapper);
    }

    // ── save ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save serializes session to JSON and writes it with the correct key and 30-minute TTL")
    void save_validSession_writesJsonWithKeyAndTtl() throws Exception {
        CheckoutSession session = CheckoutSession.builder()
                .userId(USER_ID)
                .step(CheckoutStep.PENDING_SHIPPING)
                .build();

        store.save(USER_ID, session);

        String expectedJson = objectMapper.writeValueAsString(session);
        verify(valueOps).set(eq(EXPECTED_KEY), eq(expectedJson), eq(EXPECTED_TTL));
    }

    @Test
    @DisplayName("save throws IllegalStateException when serialization fails")
    void save_serializationFailure_throwsIllegalStateException() throws Exception {
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        when(brokenMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("simulated") {});

        RedisCheckoutSessionStore storeWithBrokenMapper =
                new RedisCheckoutSessionStore(redisTemplate, brokenMapper);

        CheckoutSession session = CheckoutSession.builder().userId(USER_ID).build();

        assertThrows(IllegalStateException.class, () -> storeWithBrokenMapper.save(USER_ID, session));
        verify(valueOps, never()).set(any(), any(), any(Duration.class));
    }

    // ── find ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find returns Optional.empty when no session exists in Redis")
    void find_keyAbsent_returnsEmpty() {
        when(valueOps.get(EXPECTED_KEY)).thenReturn(null);

        Optional<CheckoutSession> result = store.find(USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("find deserializes stored JSON and returns the session")
    void find_validJson_returnsDeserializedSession() throws Exception {
        CheckoutSession session = CheckoutSession.builder()
                .userId(USER_ID)
                .step(CheckoutStep.PENDING_PAYMENT)
                .shippingCost(new BigDecimal("9.99"))
                .build();
        String json = objectMapper.writeValueAsString(session);
        when(valueOps.get(EXPECTED_KEY)).thenReturn(json);

        Optional<CheckoutSession> result = store.find(USER_ID);

        assertTrue(result.isPresent());
        CheckoutSession found = result.get();
        assertEquals(USER_ID, found.getUserId());
        assertEquals(CheckoutStep.PENDING_PAYMENT, found.getStep());
        assertEquals(new BigDecimal("9.99"), found.getShippingCost());
    }

    @Test
    @DisplayName("find returns Optional.empty and deletes the key when stored JSON is corrupted")
    void find_corruptedJson_deletesKeyAndReturnsEmpty() {
        when(valueOps.get(EXPECTED_KEY)).thenReturn("not-valid-json{{{");

        Optional<CheckoutSession> result = store.find(USER_ID);

        assertTrue(result.isEmpty());
        verify(redisTemplate).delete(EXPECTED_KEY);
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete removes the session key from Redis using the correct key")
    void delete_removesKeyFromRedis() {
        store.delete(USER_ID);

        verify(redisTemplate).delete(EXPECTED_KEY);
    }
}
