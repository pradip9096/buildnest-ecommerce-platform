package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.RecoveryCodesResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorSetupResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorVerifyRequest;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.auth.TwoFactorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * TOTP-based 2FA management (#91, AUTH-02). Reachable under {@code
 * /api/user/2fa/**} -- following the established {@code /api/user/**}
 * convention (see #88's sibling precedent), not the issue's own
 * unmatched {@code /api/v1/users/2fa/...} path. Already authenticated
 * and role-checked by SecurityConfig's existing {@code /api/user/**}
 * rule; no security config change needed.
 */
@Slf4j
@RestController
@RequestMapping("/api/user/2fa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication",
        description = "TOTP 2FA enable/verify/disable")
public class TwoFactorController {
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;

    @PostMapping("/enable")
    @Auditable(action = "2FA_ENABLE_INITIATE", entityType = "AUTH")
    @Operation(summary = "Begin 2FA setup",
            description = "Generates a TOTP secret and QR code; 2FA is not "
                    + "active until confirmed via /verify")
    public ResponseEntity<ApiResponse> enable(
            Authentication authentication) {
        User user = currentUser(authentication);
        TwoFactorSetupResponse response =
                twoFactorService.generateSecret(user);
        return ResponseEntity.ok(
                new ApiResponse(true, "2FA setup initiated", response));
    }

    @PostMapping("/verify")
    @Auditable(action = "2FA_ENABLE_CONFIRM", entityType = "AUTH")
    @Operation(summary = "Confirm 2FA setup",
            description = "Verifies the TOTP code and activates 2FA, "
                    + "returning 8 one-time recovery codes")
    public ResponseEntity<ApiResponse> verify(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        try {
            RecoveryCodesResponse response = twoFactorService
                    .verifyAndEnable(user, request.getCode());
            return ResponseEntity.ok(
                    new ApiResponse(true, "2FA enabled", response));
        } catch (Exception e) {
            log.warn("2FA verify failed for user {}: {}",
                    user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid TOTP code", null));
        }
    }

    @PostMapping("/disable")
    @Auditable(action = "2FA_DISABLE", entityType = "AUTH")
    @Operation(summary = "Disable 2FA",
            description = "Requires a valid TOTP code")
    public ResponseEntity<ApiResponse> disable(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        try {
            twoFactorService.disable(user, request.getCode());
            return ResponseEntity.ok(
                    new ApiResponse(true, "2FA disabled", null));
        } catch (Exception e) {
            log.warn("2FA disable failed for user {}: {}",
                    user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid TOTP code", null));
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
