package com.example.buildnest_ecommerce.service.checkout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCheckoutSessionStore implements CheckoutSessionStore {

    private static final String KEY_PREFIX = "checkout:session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Long userId, CheckoutSession session) {
        try {
            String json = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + userId, json, SESSION_TTL);
            log.debug("Saved checkout session for user {}, step={}", userId, session.getStep());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize checkout session for user " + userId, e);
        }
    }

    @Override
    public Optional<CheckoutSession> find(Long userId) {
        String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, CheckoutSession.class));
        } catch (JsonProcessingException e) {
            log.warn("Corrupted checkout session for user {}; discarding", userId);
            delete(userId);
            return Optional.empty();
        }
    }

    @Override
    public void delete(Long userId) {
        stringRedisTemplate.delete(KEY_PREFIX + userId);
        log.debug("Deleted checkout session for user {}", userId);
    }
}
