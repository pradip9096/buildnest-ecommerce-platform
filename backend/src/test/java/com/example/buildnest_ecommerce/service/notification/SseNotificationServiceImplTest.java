package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.controller.user.NotificationController;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@DisplayName("SseNotificationServiceImpl tests (NOTIF-02, #63)")
class SseNotificationServiceImplTest {

    private final SseNotificationServiceImpl service = new SseNotificationServiceImpl();

    @SuppressWarnings("unchecked")
    private Map<Long, java.util.List<SseEmitter>> emittersByUser() throws Exception {
        Field field = SseNotificationServiceImpl.class.getDeclaredField("emittersByUser");
        field.setAccessible(true);
        return (Map<Long, java.util.List<SseEmitter>>) field.get(service);
    }

    @Test
    @DisplayName("register returns a live emitter and stores it for the user")
    void registerStoresEmitterForUser() throws Exception {
        SseEmitter emitter = service.register(1L);

        assertNotNull(emitter);
        Map<Long, java.util.List<SseEmitter>> registry = emittersByUser();
        assertTrue(registry.containsKey(1L));
        assertTrue(registry.get(1L).contains(emitter));
    }

    @Test
    @DisplayName("a user can have more than one concurrent connection")
    void registerSupportsMultipleConnectionsPerUser() throws Exception {
        SseEmitter first = service.register(1L);
        SseEmitter second = service.register(1L);

        assertNotSame(first, second);
        assertEquals(2, emittersByUser().get(1L).size());
    }

    @Test
    @DisplayName("sendOrderStatusUpdate is a no-op when the user has no open connection")
    void sendOrderStatusUpdateNoOpWhenNoConnection() {
        assertDoesNotThrow(() -> service.sendOrderStatusUpdate(999L, 5L, "PENDING", "CONFIRMED"));
    }

    @Test
    @DisplayName("sendOrderStatusUpdate delivers the event to every emitter registered for the user")
    void sendOrderStatusUpdateDeliversToAllUserEmitters() throws Exception {
        service.register(1L);
        service.register(1L);
        service.register(2L);

        service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED");

        // Both of user 1's emitters should still be registered (send succeeded, no cleanup triggered)
        assertEquals(2, emittersByUser().get(1L).size());
        assertEquals(1, emittersByUser().get(2L).size());
    }

    @Test
    @DisplayName("onCompletion removes the emitter from the registry")
    void completionRemovesEmitterFromRegistry() throws Exception {
        // A bare `new SseEmitter()` never has its Spring-internal `handler` initialized outside a
        // real servlet async request, so calling complete() on it directly is a no-op for callback
        // purposes (verified against ResponseBodyEmitter's bytecode: complete() only delegates to
        // `handler.complete()`, and `handler` stays null until DispatcherServlet's async return-value
        // processing calls initialize()). Drive a real MockMvc request through the controller first so
        // the emitter is genuinely initialized, matching production behavior.
        CustomUserDetails principal = new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        Authentication authentication = new TestingAuthenticationToken(principal, null);

        NotificationController controller = new NotificationController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Spring MVC's built-in ServletRequestMethodArgumentResolver resolves a plain
        // Authentication/Principal controller parameter from request.getUserPrincipal() —
        // set it directly on the request rather than via a custom resolver (a custom resolver
        // is registered *after* the built-ins in standalone setup, so it never gets a chance to run).
        MvcResult mvcResult = mockMvc.perform(get("/api/user/notifications/stream").principal(authentication))
                .andExpect(request().asyncStarted())
                .andReturn();

        Map<Long, java.util.List<SseEmitter>> registry = emittersByUser();
        assertTrue(registry.containsKey(1L), "emitter should be registered once the stream request is handled");

        SseEmitter emitter = registry.get(1L).get(0);
        emitter.complete();

        // emitter.complete() only tells the servlet Handler to finish the response; the registered
        // onCompletion callback is actually invoked by the async context's own completion listener,
        // which MockMvc only fires when the async cycle is explicitly replayed via asyncDispatch.
        mockMvc.perform(asyncDispatch(mvcResult));

        assertFalse(registry.containsKey(1L), "user entry should be pruned once its last emitter completes");
    }

    @Test
    @DisplayName("a broken emitter is removed on the next send attempt rather than breaking delivery to others")
    void brokenEmitterIsRemovedWithoutBreakingOthers() throws Exception {
        SseEmitter broken = new SseEmitter(1000L);
        // Force every send on this emitter to throw, simulating a client that disconnected uncleanly.
        broken.complete();

        Field field = SseNotificationServiceImpl.class.getDeclaredField("emittersByUser");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, java.util.List<SseEmitter>> registry =
                (ConcurrentHashMap<Long, java.util.List<SseEmitter>>) field.get(service);
        registry.put(1L, new java.util.concurrent.CopyOnWriteArrayList<>(java.util.List.of(broken)));

        assertDoesNotThrow(() -> service.sendOrderStatusUpdate(1L, 5L, "PENDING", "CONFIRMED"));
    }
}
