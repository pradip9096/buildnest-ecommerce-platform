package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.AdminOrderDetailDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.RefundRequest;
import com.example.buildnest_ecommerce.model.payload.UpdateOrderStatusRequest;
import com.example.buildnest_ecommerce.service.order.OrderService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAdminOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Order.OrderStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid status value: " + status, null));
            }
        }

        Page<AdminOrderDetailDTO> page = orderService.getAdminOrders(parsedStatus, userId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAdminOrderDetail(@PathVariable Long id) {
        AdminOrderDetailDTO detail = orderService.getAdminOrderDetail(id);
        return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", detail));
    }

    @PatchMapping("/{id}/status")
    @Auditable(action = "ADMIN_UPDATE_ORDER_STATUS", entityType = "Order")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Order updated = orderService.adminUpdateOrderStatus(id, request.getStatus(), request.getCancellationReason());
        return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updated.getStatus().name()));
    }

    @PostMapping("/{id}/refund")
    @Auditable(action = "ADMIN_REFUND_PAYMENT", entityType = "Payment")
    public ResponseEntity<ApiResponse> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        Payment payment = paymentService.processRefund(id, request.getAmount(), request.getReason());
        return ResponseEntity.ok(new ApiResponse(true, "Refund processed successfully", payment));
    }
}
