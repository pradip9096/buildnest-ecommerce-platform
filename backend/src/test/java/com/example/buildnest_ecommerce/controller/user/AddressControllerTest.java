package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.address.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressControllerTest {

    private static CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(id, "testuser", "test@example.com", "hash",
                Collections.emptyList(), true, true, true, true);
    }

    @Test
    void createAddress_returnsCreatedWithBody() {
        AddressService addressService = mock(AddressService.class);
        CreateAddressRequest request = new CreateAddressRequest(
                "123 Main Street", "Mumbai", "Maharashtra", "400001", "India", "SHIPPING");
        AddressResponseDTO responseDTO = new AddressResponseDTO(
                10L, "123 Main Street", "Mumbai", "Maharashtra", "400001", "India", true, "SHIPPING");

        when(addressService.createAddress(3L, request)).thenReturn(responseDTO);

        AddressController controller = new AddressController(addressService);
        ResponseEntity<?> response = controller.createAddress(request, userDetails(3L));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
