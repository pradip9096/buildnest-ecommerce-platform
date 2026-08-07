package com.example.buildnest_ecommerce.service.returns;

import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnService {

    /**
     * Create a return request for an order owned by the given user,
     * within the 30-day return window of delivery (RET-01, #88).
     */
    ReturnRequestDTO createReturnRequest(
            Long userId, Long orderId, String reason);

    /**
     * Admin-scoped paginated return-request listing with optional
     * status filter (RET-02, #88).
     */
    Page<ReturnRequestDTO> getAdminReturnRequests(
            ReturnStatus status, Pageable pageable);

    /**
     * Admin approve/reject a return request (RET-03, #88). Approval
     * triggers a payment refund and restores inventory for the
     * order's items.
     */
    ReturnRequestDTO updateReturnStatus(
            Long returnRequestId, String newStatus, String adminNotes);
}
