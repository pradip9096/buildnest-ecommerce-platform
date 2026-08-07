package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.ReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDTO {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String reason;
    private String status;
    private BigDecimal refundAmount;
    private String adminNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    public static ReturnRequestDTO from(ReturnRequest returnRequest) {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setId(returnRequest.getId());
        dto.setOrderId(returnRequest.getOrder().getId());
        dto.setOrderNumber(returnRequest.getOrder().getOrderNumber());
        dto.setUserId(returnRequest.getUser().getId());
        dto.setReason(returnRequest.getReason());
        dto.setStatus(returnRequest.getStatus().name());
        dto.setRefundAmount(returnRequest.getRefundAmount());
        dto.setAdminNotes(returnRequest.getAdminNotes());
        dto.setRequestedAt(returnRequest.getRequestedAt());
        dto.setResolvedAt(returnRequest.getResolvedAt());
        return dto;
    }
}
