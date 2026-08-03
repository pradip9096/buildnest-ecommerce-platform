package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserDataExportDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.user.UserService;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getMyProfile(
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            UserResponseDTO user = userService
                    .getUserResponseById(userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "User profile retrieved", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", null));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @Valid @RequestBody UpdateUserDTO updateDTO,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            UserResponseDTO updatedUser = userService.updateUserProfile(
                    userDetails.getId(), updateDTO);
            return ResponseEntity.ok(new ApiResponse(
                    true, "Profile updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,
                            "Error updating profile: " + e.getMessage(),
                            null));
        }
    }

    /**
     * GDPR right-to-access export (#128, COMP-01). Returns every piece
     * of the caller's own data -- never accepts a target user id, so
     * this can only ever export the authenticated caller's own record.
     */
    @GetMapping("/data-export")
    public ResponseEntity<ApiResponse> exportMyData(
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            UserDataExportDTO export = userService
                    .exportUserData(userDetails.getId());
            return ResponseEntity.ok(new ApiResponse(
                    true, "User data exported", export));
        } catch (Exception e) {
            log.error("Data export failed", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", null));
        }
    }

    /**
     * GDPR right-to-erasure (#128, COMP-01). Soft-deletes the caller's
     * own account -- never accepts a target user id (same IDOR-safe
     * shape as the other endpoints in this controller). PII is
     * irreversibly anonymized 30 days later by the scheduled
     * {@code AccountAnonymizationScheduler} job.
     */
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse> deleteMyAccount(
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            userService.deleteUser(userDetails.getId());
            return ResponseEntity.ok(new ApiResponse(
                    true, "Account scheduled for deletion", null));
        } catch (Exception e) {
            log.error("Account deletion failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,
                            "Error deleting account: " + e.getMessage(),
                            null));
        }
    }
}
