package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.seller.SellerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SellerControllerTest {

    private static CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(id, "shopowner", "s@example.com",
                "hash", Collections.emptyList(), true, true, true, true);
    }

    @Test
    void registerSeller_returnsCreatedWithBody() {
        SellerService sellerService = mock(SellerService.class);
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", "REG-1");
        SellerResponseDTO dto = new SellerResponseDTO(10L, 3L, "Acme Décor",
                "REG-1", Seller.VerificationStatus.PENDING, List.of(),
                LocalDateTime.now());
        when(sellerService.registerSeller(3L, request)).thenReturn(dto);

        SellerController controller = new SellerController(sellerService);
        ResponseEntity<?> response =
                controller.registerSeller(userDetails(3L), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void getSellerProfile_returnsOkWithBody() {
        SellerService sellerService = mock(SellerService.class);
        SellerResponseDTO dto = new SellerResponseDTO(10L, 3L, "Acme Décor",
                null, Seller.VerificationStatus.PENDING, List.of(),
                LocalDateTime.now());
        when(sellerService.getSellerProfile(3L)).thenReturn(dto);

        SellerController controller = new SellerController(sellerService);
        ResponseEntity<?> response =
                controller.getSellerProfile(userDetails(3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
