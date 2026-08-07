package com.example.buildnest_ecommerce.service.returns;

import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.model.entity.OrderItem;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ReturnRequestRepository;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Return requests are whole-order only (RET-01..03, #88) — a single
 * PENDING/APPROVED/REFUNDED request per order is enforced, so
 * approval always refunds the full order total and restores every
 * line item's full quantity. Line-item-level partial returns are out
 * of scope for #88.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReturnServiceImpl implements ReturnService {

    private static final int RETURN_WINDOW_DAYS = 30;
    private static final Set<OrderStatus> RETURNABLE_ORDER_STATUSES =
            Set.of(OrderStatus.DELIVERED);
    private static final Set<ReturnStatus> ACTIVE_RETURN_STATUSES =
            Set.of(ReturnStatus.PENDING, ReturnStatus.APPROVED,
                    ReturnStatus.REFUNDED);

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public ReturnRequestDTO createReturnRequest(
            Long userId, Long orderId, String reason) {
        log.info("User {} requesting return for order {}", userId, orderId);

        // Pessimistic write lock on the order row serializes concurrent
        // create calls for the same order, closing the TOCTOU window
        // between the active-return existence check below and the
        // insert (#88 review finding).
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "Order does not belong to this user");
        }

        if (!RETURNABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new ValidationException(
                    "Order must be DELIVERED before a return can be "
                            + "requested; current status: "
                            + order.getStatus());
        }

        if (order.getDeliveredAt() == null
                || order.getDeliveredAt().plusDays(RETURN_WINDOW_DAYS)
                        .isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    "Return window of " + RETURN_WINDOW_DAYS
                            + " days from delivery has expired");
        }

        returnRequestRepository
                .findByOrderIdAndStatusIn(orderId, ACTIVE_RETURN_STATUSES)
                .ifPresent(existing -> {
                    throw new ValidationException(
                            "An active return request already exists "
                                    + "for this order");
                });

        User user = order.getUser();
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setReason(reason);
        returnRequest.setStatus(ReturnStatus.PENDING);
        returnRequest.setRequestedAt(LocalDateTime.now());
        returnRequest.setUpdatedAt(LocalDateTime.now());

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return ReturnRequestDTO.from(saved);
    }

    @Override
    public Page<ReturnRequestDTO> getAdminReturnRequests(
            ReturnStatus status, Pageable pageable) {
        Specification<ReturnRequest> spec = (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
        return returnRequestRepository.findAll(spec, pageable)
                .map(ReturnRequestDTO::from);
    }

    @Override
    @Transactional
    public ReturnRequestDTO updateReturnStatus(
            Long returnRequestId, String newStatus, String adminNotes) {
        log.info("Admin updating return request {} to status {}",
                returnRequestId, newStatus);

        ReturnRequest returnRequest = returnRequestRepository
                .findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReturnRequest", returnRequestId));

        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            throw new ValidationException(
                    "Only a PENDING return request can be approved or "
                            + "rejected; current status: "
                            + returnRequest.getStatus());
        }

        ReturnStatus targetStatus = parseStatus(newStatus);
        if (targetStatus != ReturnStatus.APPROVED
                && targetStatus != ReturnStatus.REJECTED) {
            throw new ValidationException(
                    "newStatus must be APPROVED or REJECTED");
        }

        returnRequest.setAdminNotes(adminNotes);
        returnRequest.setResolvedAt(LocalDateTime.now());
        returnRequest.setUpdatedAt(LocalDateTime.now());

        if (targetStatus == ReturnStatus.APPROVED) {
            Order order = returnRequest.getOrder();
            BigDecimal refundAmount = order.getTotalAmount();

            // Restore inventory (DB-only, rolls back cleanly) before
            // calling the irreversible external refund gateway, so a
            // failure here never leaves a real refund issued with no
            // corresponding DB record (#88 review finding, CRITICAL).
            restoreInventory(order, returnRequest.getId());
            paymentService.processRefund(order.getId(),
                    refundAmount.doubleValue(),
                    "Return approved: RET-" + returnRequest.getId());

            returnRequest.setRefundAmount(refundAmount);
            returnRequest.setStatus(ReturnStatus.REFUNDED);
        } else {
            returnRequest.setStatus(ReturnStatus.REJECTED);
        }

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return ReturnRequestDTO.from(saved);
    }

    private void restoreInventory(Order order, Long returnRequestId) {
        List<OrderItem> items = List.copyOf(order.getOrderItems());
        for (OrderItem item : items) {
            inventoryService.adjustStock(
                    item.getProduct().getId(), item.getQuantity(),
                    "Return approved: RET-" + returnRequestId, null);
        }
    }

    private ReturnStatus parseStatus(String status) {
        if (status == null) {
            throw new ValidationException("status is required");
        }
        try {
            return ReturnStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Invalid return status: " + status);
        }
    }
}
