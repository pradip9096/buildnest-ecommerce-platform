package com.example.buildnest_ecommerce.controller.auth;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.password.PasswordResetService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private RateLimitUtil rateLimitUtil;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void forgotPasswordShouldReturnSuccessWhenEmailValid() {
        when(rateLimitUtil.isAllowed(any(), eq("password-forgot"))).thenReturn(true);
        doNothing().when(passwordResetService).initiatePasswordReset(anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.forgotPassword("test@example.com", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password reset link sent to email", response.getBody().getMessage());
        verify(passwordResetService).initiatePasswordReset("test@example.com");
    }

    @Test
    void forgotPasswordShouldReturnTooManyRequestsWhenRateLimited() {
        when(rateLimitUtil.isAllowed(any(), eq("password-forgot"))).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), eq("password-forgot"), isNull())).thenReturn(60L);

        ResponseEntity<ApiResponse> response = passwordResetController.forgotPassword("test@example.com", request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Too many password reset requests. Please try again later.", response.getBody().getMessage());
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
        verify(passwordResetService, never()).initiatePasswordReset(anyString());
    }

    @Test
    void forgotPasswordShouldReturnBadRequestWhenServiceThrowsException() {
        when(rateLimitUtil.isAllowed(any(), eq("password-forgot"))).thenReturn(true);
        doThrow(new RuntimeException("Service error")).when(passwordResetService).initiatePasswordReset(anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.forgotPassword("test@example.com", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Error processing request", response.getBody().getMessage());
    }

    @Test
    void resetPasswordShouldReturnSuccessWhenTokenValid() {
        when(rateLimitUtil.isAllowed(any(), eq("password-reset"))).thenReturn(true);
        doNothing().when(passwordResetService).resetPasswordWithToken(anyString(), anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.resetPassword("valid-token", "newPassword123",
                request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password reset successfully", response.getBody().getMessage());
        verify(passwordResetService).resetPasswordWithToken("valid-token", "newPassword123");
    }

    @Test
    void resetPasswordShouldReturnTooManyRequestsWhenRateLimited() {
        when(rateLimitUtil.isAllowed(any(), eq("password-reset"))).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), eq("password-reset"), isNull())).thenReturn(120L);

        ResponseEntity<ApiResponse> response = passwordResetController.resetPassword("token", "newPassword", request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Too many password reset attempts. Please try again later.", response.getBody().getMessage());
        assertEquals("120", response.getHeaders().getFirst("Retry-After"));
        verify(passwordResetService, never()).resetPasswordWithToken(anyString(), anyString());
    }

    @Test
    void resetPasswordShouldReturnNotImplementedWhenFeatureNotAvailable() {
        when(rateLimitUtil.isAllowed(any(), eq("password-reset"))).thenReturn(true);
        doThrow(new UnsupportedOperationException()).when(passwordResetService).resetPasswordWithToken(anyString(),
                anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.resetPassword("token", "newPassword", request);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Feature not yet implemented", response.getBody().getMessage());
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenTokenInvalid() {
        when(rateLimitUtil.isAllowed(any(), eq("password-reset"))).thenReturn(true);
        doThrow(new RuntimeException("Invalid token")).when(passwordResetService).resetPasswordWithToken(anyString(),
                anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.resetPassword("invalid-token", "newPassword",
                request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid or expired token", response.getBody().getMessage());
    }

    @Test
    void changePasswordShouldReturnSuccessWhenPasswordChanged() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0");

        when(rateLimitUtil.isAllowed(any(), eq("password-change"), eq(1L))).thenReturn(true);
        doNothing().when(passwordResetService).changePassword(eq(1L), eq("oldPass"), eq("newPass"), anyString(),
                anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.changePassword(1L, "oldPass", "newPass",
                mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password changed successfully", response.getBody().getMessage());
        verify(passwordResetService).changePassword(eq(1L), eq("oldPass"), eq("newPass"), eq("127.0.0.1"),
                eq("Mozilla/5.0"));
    }

    @Test
    void changePasswordShouldReturnTooManyRequestsWhenRateLimited() {
        when(rateLimitUtil.isAllowed(any(), eq("password-change"), eq(1L))).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), eq("password-change"), eq(1L))).thenReturn(300L);

        ResponseEntity<ApiResponse> response = passwordResetController.changePassword(1L, "oldPass", "newPass",
                request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Too many password change attempts. Please try again later.", response.getBody().getMessage());
        assertEquals("300", response.getHeaders().getFirst("Retry-After"));
        verify(passwordResetService, never()).changePassword(anyLong(), anyString(), anyString(), anyString(),
                anyString());
    }

    @Test
    void changePasswordShouldReturnBadRequestWhenOldPasswordIncorrect() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0");

        when(rateLimitUtil.isAllowed(any(), eq("password-change"), eq(1L))).thenReturn(true);
        doThrow(new IllegalArgumentException("Incorrect old password")).when(passwordResetService)
                .changePassword(anyLong(), anyString(), anyString(), anyString(), anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.changePassword(1L, "wrongPass", "newPass",
                mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Incorrect old password", response.getBody().getMessage());
    }

    @Test
    void changePasswordShouldReturnBadRequestOnGeneralException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0");

        when(rateLimitUtil.isAllowed(any(), eq("password-change"), eq(1L))).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(passwordResetService)
                .changePassword(anyLong(), anyString(), anyString(), anyString(), anyString());

        ResponseEntity<ApiResponse> response = passwordResetController.changePassword(1L, "oldPass", "newPass",
                mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Error changing password", response.getBody().getMessage());
    }
}
