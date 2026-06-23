package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.user.UserService;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getMyProfile(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserResponseDTO user = userService.getUserResponseById(userDetails.getId());
            return ResponseEntity.ok(new ApiResponse(true, "User profile retrieved", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", null));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody UpdateUserDTO updateDTO,
                                                      Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserResponseDTO updatedUser = userService.updateUserProfile(userDetails.getId(), updateDTO);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error updating profile: " + e.getMessage(), null));
        }
    }
}
