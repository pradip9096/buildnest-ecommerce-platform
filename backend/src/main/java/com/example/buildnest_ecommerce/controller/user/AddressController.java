package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
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

/**
 * User address book — currently create-only, backing the checkout address
 * step (FE-07). Listing, updating, deleting, and setting a default address
 * are tracked separately under FE-11.
 */
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Address", description = "Endpoints for managing a user's saved addresses")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        AddressResponseDTO address = addressService.createAddress(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Address created successfully", address));
    }
}
