package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.controller.user.NotificationController;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@DisplayName("SseNotificationServiceImpl tests (NOTIF-02, #63)")
class SseNotificationServiceImplTest {

    private final SseNotificationServiceImpl service = new SseNotificationServiceImpl();

    @SuppressWarnings("unchecked")
    private Map<Long, List<SseEmitter>> emittersByUser() throws Exception {
        Field field = SseNotificationServiceImpl.class.getDeclaredField("emittersByUser");
        field.setAccessible(true);
        return (Map<Long, List<SseEmitter>>) field.get(service);
    }

    private Authentication authFor(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        return new TestingAuthenticationToken(principal, null);
    }

    /**
     * Drives a real MockMvc request through the controller so the returned emitter is
     * genuinely initialized by Spring's async machinery (a bare `new SseEmitter()` has no
     * effect for callback/response-writing purposes outside a real servlet request — see
     * completionRemovesEmitterFromRegistry's comment below for why).
     */
    private MvcResult connect(Long userId) throws Exception {
        NotificationController controller = new NotificationController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        return mockMvc.perform(get("/api/user/notifications/stream").principal(authFor(userId)))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    @Test
    @DisplayName("register returns a live emitter, stores it for the user, and writes the initial handshake event")
    void registerStoresEmitterAndWritesHandshake() throws Exception {
        MvcResult result = connect(1L);

        Map<Long, List<SseEmitter>> registry = emittersByUser();
        assertTrue(registry.containsKey(1L));
        assertEquals(1, registry.get(1L).size());

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("event:connected"), "handshake event name should be written: " + body);
        assertTrue(body.contains("data:connected"), "handshake payload should be written: " + body);
    }

    @Test
    @DisplayName("a user can have more than one concurrent connection")
    void registerSupportsMultipleConnectionsPerUser() throws Exception {
        connect(1L);
        connect(1L);

        assertEquals(2, emittersByUser().get(1L).size());
    }

    @Test
    @DisplayName("sendOrderStatusUpdate is a no-op when the user has never connected")
    void sendOrderStatusUpdateNoOpWhenUserNeverConnected() {
        assertDoesNotThrow(() -> service.sendOrderStatusUpdate(999L, 5L, "PENDING", "CONFIRMED"));
    }

    @Test
    @DisplayName("sendOrderStatusUpdate is a no-op when the user's connection list is empty")
    void sendOrderStatusUpdateNoOpWhenConnectionListEmpty() throws Exception {
        emittersByUser().put(1L, new CopyOnWriteArrayList<>());

        assertDoesNotThrow(() -> service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED"));
    }

    @Test
    @DisplayName("sendOrderStatusUpdate writes the order-status event with the exact orderId/previousStatus/newStatus to the connection")
    void sendOrderStatusUpdateWritesCorrectPayload() throws Exception {
        MvcResult result = connect(1L);

        service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED");

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("event:order-status"), "event name should be order-status: " + body);
        assertTrue(body.contains("\"orderId\":5"), "payload should carry the orderId: " + body);
        assertTrue(body.contains("\"previousStatus\":\"PENDING\""), "payload should carry previousStatus: " + body);
        assertTrue(body.contains("\"newStatus\":\"CONFIRMED\""), "payload should carry newStatus: " + body);
    }

    @Test
    @DisplayName("sendOrderStatusUpdate delivers the event to every connection registered for the user, and only that user")
    void sendOrderStatusUpdateDeliversToAllUserEmittersOnly() throws Exception {
        MvcResult firstConnection = connect(1L);
        MvcResult secondConnection = connect(1L);
        MvcResult otherUserConnection = connect(2L);

        service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED");

        assertTrue(firstConnection.getResponse().getContentAsString().contains("event:order-status"));
        assertTrue(secondConnection.getResponse().getContentAsString().contains("event:order-status"));
        assertFalse(otherUserConnection.getResponse().getContentAsString().contains("event:order-status"),
                "user 2's connection must not receive user 1's order-status event");

        // Both of user 1's connections should still be registered (send succeeded, no cleanup triggered)
        assertEquals(2, emittersByUser().get(1L).size());
        assertEquals(1, emittersByUser().get(2L).size());
    }

    @Test
    @DisplayName("onCompletion removes the emitter from the registry, pruning the user entry once empty")
    void completionRemovesEmitterFromRegistry() throws Exception {
        // A bare `new SseEmitter()` never has its Spring-internal `handler` initialized outside a
        // real servlet async request, so calling complete() on it directly is a no-op for callback
        // purposes (verified against ResponseBodyEmitter's bytecode: complete() only delegates to
        // `handler.complete()`, and `handler` stays null until DispatcherServlet's async return-value
        // processing calls initialize()). connect() above drives a real MockMvc request first so the
        // emitter is genuinely initialized, matching production behavior.
        MvcResult mvcResult = connect(1L);

        Map<Long, List<SseEmitter>> registry = emittersByUser();
        assertTrue(registry.containsKey(1L), "emitter should be registered once the stream request is handled");

        SseEmitter emitter = registry.get(1L).get(0);
        emitter.complete();

        // emitter.complete() only tells the servlet Handler to finish the response; the registered
        // onCompletion callback is actually invoked by the async context's own completion listener,
        // which MockMvc only fires when the async cycle is explicitly replayed via asyncDispatch.
        mockMvcAsyncDispatch(mvcResult);

        assertFalse(registry.containsKey(1L), "user entry should be pruned once its last emitter completes");
    }

    @Test
    @DisplayName("completing one of a user's several connections only removes that one, keeping the entry alive")
    void completingOneOfSeveralConnectionsKeepsUserEntryAlive() throws Exception {
        MvcResult first = connect(1L);
        connect(1L);

        Map<Long, List<SseEmitter>> registry = emittersByUser();
        SseEmitter firstEmitter = registry.get(1L).get(0);
        firstEmitter.complete();
        mockMvcAsyncDispatch(first);

        assertTrue(registry.containsKey(1L), "user entry must survive while one connection is still open");
        assertEquals(1, registry.get(1L).size());
        assertFalse(registry.get(1L).contains(firstEmitter));
    }

    private void mockMvcAsyncDispatch(MvcResult mvcResult) throws Exception {
        NotificationController controller = new NotificationController(service);
        MockMvcBuilders.standaloneSetup(controller).build().perform(asyncDispatch(mvcResult));
    }

    @Test
    @DisplayName("a broken emitter is removed from the registry and does not break delivery to the user's other connections")
    void brokenEmitterIsRemovedWithoutBreakingOtherDeliveries() throws Exception {
        SseEmitter broken = new SseEmitter(1000L);
        broken.complete(); // any further send() on a completed emitter throws IllegalStateException

        MvcResult healthyConnection = connect(1L);

        Map<Long, List<SseEmitter>> registry = emittersByUser();
        registry.get(1L).add(0, broken);
        assertEquals(2, registry.get(1L).size());

        service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED");

        assertFalse(registry.get(1L).contains(broken), "the broken emitter should have been pruned");
        assertEquals(1, registry.get(1L).size());
        assertTrue(healthyConnection.getResponse().getContentAsString().contains("event:order-status"),
                "the healthy connection should still have received the event");
    }

    @Test
    @DisplayName("onTimeout removes the emitter from the registry")
    void onTimeoutRemovesEmitterFromRegistry() throws Exception {
        // Real container timeouts can't be simulated deterministically in a unit test; spy on the
        // emitter passed through the package-private register(userId, emitter) seam, capture the
        // Runnable registered via onTimeout(...), and invoke it directly to exercise that exact
        // callback body (the same one wired to removeEmitter in production).
        SseEmitter spyEmitter = spy(new SseEmitter(1000L));
        service.register(1L, spyEmitter);

        ArgumentCaptor<Runnable> timeoutCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(spyEmitter).onTimeout(timeoutCallback.capture());

        assertTrue(emittersByUser().containsKey(1L));
        timeoutCallback.getValue().run();

        assertFalse(emittersByUser().containsKey(1L), "user entry should be pruned once the emitter times out");
    }

    @Test
    @DisplayName("onError removes the emitter from the registry")
    void onErrorRemovesEmitterFromRegistry() throws Exception {
        SseEmitter spyEmitter = spy(new SseEmitter(1000L));
        service.register(1L, spyEmitter);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<Throwable>> errorCallback = ArgumentCaptor.forClass(Consumer.class);
        verify(spyEmitter).onError(errorCallback.capture());

        assertTrue(emittersByUser().containsKey(1L));
        errorCallback.getValue().accept(new IOException("simulated client disconnect"));

        assertFalse(emittersByUser().containsKey(1L), "user entry should be pruned once the emitter errors");
    }

    @Test
    @DisplayName("register removes the emitter immediately if the initial handshake send fails")
    void registerRemovesEmitterWhenInitialHandshakeFails() throws Exception {
        SseEmitter alreadyCompleted = new SseEmitter(1000L);
        alreadyCompleted.complete(); // any send() on it now throws IllegalStateException, even pre-init

        service.register(1L, alreadyCompleted);

        assertFalse(emittersByUser().containsKey(1L),
                "a handshake-send failure should remove the emitter rather than leaving a dead entry registered");
    }
}
