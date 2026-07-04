package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.model.payload.UpdateAddressRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.address.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void getAddresses_returnsOkWithList() {
        AddressService addressService = mock(AddressService.class);
        AddressResponseDTO responseDTO = new AddressResponseDTO(
                10L, "123 Main Street", "Mumbai", "Maharashtra", "400001", "India", true, "SHIPPING");
        when(addressService.getAddresses(3L)).thenReturn(List.of(responseDTO));

        AddressController controller = new AddressController(addressService);
        ResponseEntity<?> response = controller.getAddresses(userDetails(3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateAddress_returnsOkWithBody() {
        AddressService addressService = mock(AddressService.class);
        UpdateAddressRequest request = new UpdateAddressRequest(
                "456 New Street", "Pune", "Maharashtra", "411001", "India", "BILLING");
        AddressResponseDTO responseDTO = new AddressResponseDTO(
                5L, "456 New Street", "Pune", "Maharashtra", "411001", "India", true, "BILLING");

        when(addressService.updateAddress(3L, 5L, request)).thenReturn(responseDTO);

        AddressController controller = new AddressController(addressService);
        ResponseEntity<?> response = controller.updateAddress(5L, request, userDetails(3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteAddress_returnsOk_andDelegatesToService() {
        AddressService addressService = mock(AddressService.class);

        AddressController controller = new AddressController(addressService);
        ResponseEntity<?> response = controller.deleteAddress(5L, userDetails(3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService).deleteAddress(3L, 5L);
    }

    @Test
    void setDefaultAddress_returnsOkWithBody() {
        AddressService addressService = mock(AddressService.class);
        AddressResponseDTO responseDTO = new AddressResponseDTO(
                5L, "123 Main Street", "Mumbai", "Maharashtra", "400001", "India", true, "SHIPPING");

        when(addressService.setDefaultAddress(3L, 5L)).thenReturn(responseDTO);

        AddressController controller = new AddressController(addressService);
        ResponseEntity<?> response = controller.setDefaultAddress(5L, userDetails(3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
