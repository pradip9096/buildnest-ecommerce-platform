package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.payload.UpdateReturnStatusRequest;
import com.example.buildnest_ecommerce.service.returns.ReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AdminReturnController unit tests")
class AdminReturnControllerTest {

    private ReturnService returnService;
    private AdminReturnController controller;

    @BeforeEach
    void setUp() {
        returnService = mock(ReturnService.class);
        controller = new AdminReturnController(returnService);
    }

    @Test
    @DisplayName("getAdminReturnRequests – no filter – returns 200 with page")
    void getAdminReturnRequests_noFilter_returns200() {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setId(1L);
        Page<ReturnRequestDTO> page = new PageImpl<>(List.of(dto));
        when(returnService.getAdminReturnRequests(isNull(), any()))
                .thenReturn(page);

        var response = controller.getAdminReturnRequests(
                null, PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("getAdminReturnRequests – invalid status string – returns "
            + "400")
    void getAdminReturnRequests_invalidStatus_returns400() {
        var response = controller.getAdminReturnRequests(
                "FLYING", PageRequest.of(0, 20));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("getAdminReturnRequests – valid status – passes parsed "
            + "enum to service")
    void getAdminReturnRequests_validStatus_delegatesToService() {
        when(returnService.getAdminReturnRequests(
                eq(com.example.buildnest_ecommerce.model.entity
                        .ReturnRequest.ReturnStatus.PENDING), any()))
                .thenReturn(Page.empty());

        var response = controller.getAdminReturnRequests(
                "PENDING", PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(returnService).getAdminReturnRequests(
                eq(com.example.buildnest_ecommerce.model.entity
                        .ReturnRequest.ReturnStatus.PENDING), any());
    }

    @Test
    @DisplayName("updateReturnStatus – valid request – returns 200")
    void updateReturnStatus_valid_returns200() {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setId(1L);
        dto.setStatus("REFUNDED");
        when(returnService.updateReturnStatus(
                eq(1L), eq("APPROVED"), eq("Approved")))
                .thenReturn(dto);

        var request = new UpdateReturnStatusRequest("APPROVED", "Approved");
        var response = controller.updateReturnStatus(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("updateReturnStatus – service throws ValidationException "
            + "– returns 400")
    void updateReturnStatus_serviceThrowsValidation_returns400() {
        when(returnService.updateReturnStatus(
                eq(1L), eq("APPROVED"), any()))
                .thenThrow(new ValidationException("Not PENDING"));

        var request = new UpdateReturnStatusRequest("APPROVED", "Approved");
        var response = controller.updateReturnStatus(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }
}
