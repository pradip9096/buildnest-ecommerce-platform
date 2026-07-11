package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.notification.SseNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("NotificationController tests (NOTIF-02, #63)")
class NotificationControllerTest {

    private Authentication auth(Long userId) {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails details = new CustomUserDetails(userId, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        when(authentication.getPrincipal()).thenReturn(details);
        return authentication;
    }

    @Test
    @DisplayName("stream registers the authenticated user's id and returns the resulting emitter")
    void streamRegistersAuthenticatedUser() {
        SseNotificationService sseNotificationService = mock(SseNotificationService.class);
        SseEmitter expected = new SseEmitter();
        when(sseNotificationService.register(42L)).thenReturn(expected);

        NotificationController controller = new NotificationController(sseNotificationService);
        SseEmitter actual = controller.stream(auth(42L));

        assertSame(expected, actual);
        verify(sseNotificationService).register(42L);
    }
}
