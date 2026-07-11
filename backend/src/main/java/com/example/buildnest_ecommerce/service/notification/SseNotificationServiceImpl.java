package com.example.buildnest_ecommerce.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory SSE emitter registry (NOTIF-02, #63). A user may have more than
 * one live connection at once (multiple tabs/devices), so emitters are kept
 * in a list per user id.
 *
 * Deliberately independent of the {@code elasticsearch.enabled} flag — this
 * is a user-facing feature, not observability infrastructure, so it must
 * keep working when Elasticsearch is disabled (e.g. local dev).
 */
@Slf4j
@Service
public class SseNotificationServiceImpl implements SseNotificationService {

    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    @Override
    public SseEmitter register(Long userId) {
        return register(userId, new SseEmitter(EMITTER_TIMEOUT_MS));
    }

    /**
     * Package-private seam so tests can register a pre-built (e.g. spied or
     * pre-completed) emitter and deterministically exercise the timeout/error
     * callback bodies and the initial-handshake-failure branch below, none of
     * which are reachable by driving a real servlet request alone.
     */
    SseEmitter register(Long userId, SseEmitter emitter) {
        emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("connected"));
        } catch (IOException | IllegalStateException e) {
            log.debug("Failed to send initial SSE handshake to user {}, removing emitter", userId);
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    @Override
    public void sendOrderStatusUpdate(Long userId, Long orderId, String previousStatus, String newStatus) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("previousStatus", previousStatus);
        payload.put("newStatus", newStatus);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("order-status").data(payload));
            } catch (IOException | IllegalStateException e) {
                log.debug("Failed to send SSE order-status event to user {}, removing emitter", userId);
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        emittersByUser.computeIfPresent(userId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}
