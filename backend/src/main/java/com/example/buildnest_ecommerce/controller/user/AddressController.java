package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.model.payload.UpdateAddressRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.address.AddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User address book — full CRUD plus set-default (FE-11), backing the
 * checkout address step (FE-07).
 */
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Address", description = "Endpoints for managing a user's saved addresses")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAddresses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<AddressResponseDTO> addresses = addressService.getAddresses(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Addresses retrieved successfully", addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        AddressResponseDTO address = addressService.createAddress(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Address created successfully", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        AddressResponseDTO address = addressService.updateAddress(currentUser.getId(), id, request);
        return ResponseEntity.ok(new ApiResponse(true, "Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ResponseEntity.ok(new ApiResponse(true, "Address deleted successfully", null));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        AddressResponseDTO address = addressService.setDefaultAddress(currentUser.getId(), id);
        return ResponseEntity.ok(new ApiResponse(true, "Default address updated successfully", address));
    }
}
