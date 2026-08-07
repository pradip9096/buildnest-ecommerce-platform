package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.UpdateReturnStatusRequest;
import com.example.buildnest_ecommerce.service.returns.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/returns")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReturnController {

    private final ReturnService returnService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAdminReturnRequests(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "requestedAt")
                    Pageable pageable) {

        ReturnStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = ReturnStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false, "Invalid status value: " + status, null));
            }
        }

        Page<ReturnRequestDTO> page = returnService
                .getAdminReturnRequests(parsedStatus, pageable);
        return ResponseEntity.ok(new ApiResponse(
                true, "Return requests retrieved successfully", page));
    }

    @PatchMapping("/{id}/status")
    @Auditable(action = "ADMIN_UPDATE_RETURN_STATUS",
            entityType = "ReturnRequest")
    public ResponseEntity<ApiResponse> updateReturnStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnStatusRequest request) {
        try {
            ReturnRequestDTO updated = returnService.updateReturnStatus(
                    id, request.getStatus(), request.getAdminNotes());
            return ResponseEntity.ok(new ApiResponse(
                    true, "Return request status updated", updated));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
