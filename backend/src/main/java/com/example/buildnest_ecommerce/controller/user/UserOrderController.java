package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.CreateReturnRequestPayload;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.example.buildnest_ecommerce.service.returns.ReturnService;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserOrderController {

    private final OrderService orderService;
    private final ReturnService returnService;

    @GetMapping
    public ResponseEntity<ApiResponse> getUserOrders(
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            List<OrderResponseDTO> orders =
                    orderService.getOrderResponsesByUserId(userId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "User orders retrieved", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(
                            false, "Error retrieving orders", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderDetails(
            @PathVariable Long id, Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            OrderResponseDTO order =
                    orderService.getUserOrderById(userId, id);
            return ResponseEntity.ok(new ApiResponse(
                    true, "Order details retrieved", order));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false,
                            "Access denied: This order does not "
                                    + "belong to you",
                            null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Order not found", null));
        }
    }

    @PostMapping("/{id}/returns")
    public ResponseEntity<ApiResponse> createReturnRequest(
            @PathVariable Long id,
            @Valid @RequestBody CreateReturnRequestPayload payload,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ReturnRequestDTO returnRequest = returnService
                    .createReturnRequest(userId, id, payload.getReason());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(
                            true, "Return request created", returnRequest));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
