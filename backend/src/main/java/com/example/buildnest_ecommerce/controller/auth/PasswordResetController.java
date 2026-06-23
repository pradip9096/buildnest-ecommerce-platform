package com.example.buildnest_ecommerce.controller.auth;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.password.PasswordResetService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final RateLimitUtil rateLimitUtil;
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email, HttpServletRequest request) {
        if (!rateLimitUtil.isAllowed(request, "password-forgot")) {
            long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "password-forgot", null);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfter))
                    .body(new ApiResponse(false, "Too many password reset requests. Please try again later.", null));
        }
        try {
            passwordResetService.initiatePasswordReset(email);
            return ResponseEntity.ok(new ApiResponse(true, "Password reset link sent to email", null));
        } catch (Exception e) {
            log.error("Error in forgot password", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error processing request", null));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String token, @RequestParam String newPassword,
            HttpServletRequest request) {
        if (!rateLimitUtil.isAllowed(request, "password-reset")) {
            long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "password-reset", null);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfter))
                    .body(new ApiResponse(false, "Too many password reset attempts. Please try again later.", null));
        }
        try {
            passwordResetService.resetPasswordWithToken(token, newPassword);
            return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully", null));
        } catch (UnsupportedOperationException e) {
            log.warn("Token-based password reset not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(new ApiResponse(false, "Feature not yet implemented", null));
        } catch (Exception e) {
            log.error("Error in password reset", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid or expired token", null));
        }
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse> changePassword(@RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        if (!rateLimitUtil.isAllowed(request, "password-change", userId)) {
            long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "password-change", userId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfter))
                    .body(new ApiResponse(false, "Too many password change attempts. Please try again later.", null));
        }
        try {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            passwordResetService.changePassword(userId, oldPassword, newPassword, ipAddress, userAgent);

            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error changing password", null));
        }
    }
}
