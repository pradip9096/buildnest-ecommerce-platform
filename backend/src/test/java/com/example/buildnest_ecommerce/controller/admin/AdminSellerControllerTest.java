package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.payload
        .SellerVerificationDecisionRequest;
import com.example.buildnest_ecommerce.service.seller.SellerService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminSellerControllerTest {

    @Test
    void getSellers_defaultStatus_returnsOkWithPendingPage() {
        SellerService sellerService = mock(SellerService.class);
        SellerResponseDTO dto = new SellerResponseDTO(10L, 3L, "Acme Décor",
                "REG-1", Seller.VerificationStatus.PENDING,
                LocalDateTime.now());
        when(sellerService.getSellersByVerificationStatus(
                eq(Seller.VerificationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        AdminSellerController controller =
                new AdminSellerController(sellerService);
        ResponseEntity<?> response = controller.getSellers(
                null, PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getSellers_invalidStatus_returnsBadRequest() {
        SellerService sellerService = mock(SellerService.class);

        AdminSellerController controller =
                new AdminSellerController(sellerService);
        ResponseEntity<?> response = controller.getSellers(
                "NOT_A_STATUS", PageRequest.of(0, 20));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateVerificationStatus_verified_returnsOkWithBody() {
        SellerService sellerService = mock(SellerService.class);
        SellerVerificationDecisionRequest request =
                new SellerVerificationDecisionRequest("VERIFIED", null);
        SellerResponseDTO dto = new SellerResponseDTO(10L, 3L, "Acme Décor",
                "REG-1", Seller.VerificationStatus.VERIFIED,
                LocalDateTime.now());
        when(sellerService.updateVerificationStatus(10L, "VERIFIED", null))
                .thenReturn(dto);

        AdminSellerController controller =
                new AdminSellerController(sellerService);
        ResponseEntity<?> response =
                controller.updateVerificationStatus(10L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
