package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.dto.AdminOrderDetailDTO;
import com.example.buildnest_ecommerce.model.dto.OrderItemDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.OrderPlacedEvent;
import com.example.buildnest_ecommerce.event.OrderStatusChangedEvent;
import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.service.notification
        .INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Order Service Implementation
 *
 * Manages order operations including creation, retrieval, updates, and status
 * management.
 * Handles order lifecycle from placement to completion and publishes domain
 * events.
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>>
            VALID_TRANSITIONS = new java.util.EnumMap<>(Map.of(
            OrderStatus.PENDING,
                    EnumSet.of(OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED,
                    EnumSet.of(OrderStatus.SHIPPED,
                            OrderStatus.CANCELLED),
            OrderStatus.SHIPPED,
                    EnumSet.of(OrderStatus.DELIVERED,
                            OrderStatus.CANCELLED),
            OrderStatus.DELIVERED,      Collections.emptySet(),
            OrderStatus.CANCELLED,      Collections.emptySet(),
            OrderStatus.PAID,
                    EnumSet.of(OrderStatus.SHIPPED,
                            OrderStatus.CANCELLED),
            OrderStatus.PAYMENT_FAILED,
                    EnumSet.of(OrderStatus.CANCELLED)
    ));

    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final INotificationService notificationService;

    /**
     * Retrieves all active (non-deleted) orders.
     *
     * @return a list of all non-deleted orders
     */
    @Override
    public List<Order> getAllOrders() {
        log.info("Fetching all non-deleted orders");
        return orderRepository.findAll().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve (required)
     * @return the Order entity
     * @throws RuntimeException if order is not found
     */
    @Override
    public Order getOrderById(Long orderId) {
        log.info("Fetching order with id: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found with id: " + orderId));
    }

    /**
     * Creates a new order.
     *
     * Sets initial status to PENDING, timestamps, and publishes
     * OrderPlacedEvent.
     *
     * @param order the Order entity to create (required)
     * @return the created Order with auto-generated ID
     */
    @Override
    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order for user: {}", order.getUser().getId());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setIsDeleted(false);
        Order saved = orderRepository.save(order);
        domainEventPublisher.publish(
                new OrderPlacedEvent(this, saved, saved.getUser().getId()));
        return saved;
    }

    /**
     * Updates an existing order.
     *
     * @param orderId the ID of the order to update (required)
     * @param order   the Order entity with updated values (required)
     * @return the updated Order entity
     * @throws RuntimeException if order is not found
     */
    @Override
    @Transactional
    public Order updateOrder(Long orderId, Order order) {
        log.info("Updating order with id: {}", orderId);
        Order existingOrder = getOrderById(orderId);
        existingOrder.setTotalAmount(order.getTotalAmount());
        existingOrder.setShippingAddress(order.getShippingAddress());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }

    /**
     * Soft-deletes an order by marking it as deleted.
     *
     * @param orderId the ID of the order to delete (required)
     * @throws RuntimeException if order is not found
     */
    @Override
    public void deleteOrder(Long orderId) {
        log.info("Soft deleting order with id: {}", orderId);
        Order order = getOrderById(orderId);
        order.setIsDeleted(true);
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * Retrieves all active orders for a specific user.
     *
     * @param userId the ID of the user (required)
     * @return a list of non-deleted orders for the user
     */
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        log.info("Fetching non-deleted orders for user: {}", userId);
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(userId)
                        && !Boolean.TRUE.equals(o.getIsDeleted()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an order.
     *
     * Validates the new status, updates the order, and publishes
     * OrderStatusChangedEvent.
     *
     * @param orderId the ID of the order to update (required)
     * @param status  the new order status (required, must be valid
     *                OrderStatus enum value)
     * @return the updated Order entity
     * @throws RuntimeException if order is not found or status is invalid
     */
    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status with id: {}, status: {}",
                orderId, status);
        Order order = getOrderById(orderId);
        try {
            String previousStatus = order.getStatus().toString();
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            order.setUpdatedAt(LocalDateTime.now());
            Order saved = orderRepository.save(order);
            domainEventPublisher.publish(
                    new OrderStatusChangedEvent(this, saved.getId(),
                            saved.getUser().getId(), previousStatus,
                            saved.getStatus().toString()));
            return saved;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }

    /**
     * Retrieves order response DTOs for a specific user.
     *
     * @param userId the ID of the user (required)
     * @return a list of OrderResponseDTOs for the user's non-deleted orders
     */
    @Override
    public List<OrderResponseDTO> getOrderResponsesByUserId(Long userId) {
        log.info("Fetching order responses for user: {}", userId);
        return getOrdersByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific order for a user (with ownership verification).
     *
     * Verifies that the order belongs to the specified user before
     * returning it.
     *
     * @param userId  the ID of the user (required)
     * @param orderId the ID of the order to retrieve (required)
     * @return the OrderResponseDTO for the order
     * @throws RuntimeException       if order is not found
     * @throws IllegalAccessException if order does not belong to the
     *                                specified user
     */
    @Override
    public OrderResponseDTO getUserOrderById(Long userId, Long orderId)
            throws IllegalAccessException {
        log.info("Fetching order response for user: {}, order: {}",
                userId, orderId);
        Order order = getOrderById(orderId);

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            log.warn("Access denied: User {} tried to access order {} "
                    + "of user {}", userId, orderId,
                    order.getUser().getId());
            throw new IllegalAccessException(
                    "Access denied: This order does not belong to you");
        }

        return mapToResponseDTO(order);
    }

    @Override
    public Page<AdminOrderDetailDTO> getAdminOrders(OrderStatus status,
            Long userId, LocalDateTime dateFrom, LocalDateTime dateTo,
            Pageable pageable) {
        log.info("Admin: listing orders status={}, userId={}, "
                + "dateFrom={}, dateTo={}",
                status, userId, dateFrom, dateTo);
        return orderRepository
                .findAll(OrderSpecification.withFilters(
                        status, userId, dateFrom, dateTo), pageable)
                .map(this::mapToAdminDetailDTO);
    }

    @Override
    public AdminOrderDetailDTO getAdminOrderDetail(Long orderId) {
        log.info("Admin: fetching order detail for id={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
        return mapToAdminDetailDTO(order);
    }

    @Override
    @Transactional
    public Order adminUpdateOrderStatus(
            Long orderId, String newStatus, String cancellationReason) {
        log.info("Admin: updating order id={} to status={}",
                orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        OrderStatus targetStatus = parseStatus(newStatus);
        validateTransition(order.getStatus(), targetStatus);

        String previousStatus = order.getStatus().name();
        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        domainEventPublisher.publish(
                new OrderStatusChangedEvent(this, saved.getId(),
                        saved.getUser().getId(), previousStatus,
                        saved.getStatus().name()));

        switch (targetStatus) {
            case CONFIRMED ->
                    notificationService.sendOrderConfirmation(saved);
            case SHIPPED -> notificationService.sendShipmentNotification(
                    saved.getId(), saved.getTrackingNumber());
            case DELIVERED ->
                    notificationService.sendDeliveryNotification(
                            saved.getId());
            case CANCELLED -> log.info("Order {} cancelled. Reason: {}",
                    saved.getId(), cancellationReason);
            default -> { /* no notification for other transitions */ }
        }

        return saved;
    }

    /**
     * Seller-scoped paginated order listing (FR-SEL-06, #580).
     *
     * @param sellerId the seller's own user ID (required)
     * @param pageable pagination/sort parameters
     * @return a page of OrderResponseDTOs for orders owned by the seller
     */
    @Override
    public Page<OrderResponseDTO> getSellerOrders(
            Long sellerId, Pageable pageable) {
        log.info("Fetching orders for seller: {}", sellerId);
        return orderRepository.findBySellerId(sellerId, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Seller-scoped single-order detail lookup, with ownership enforced
     * inside the repository query itself (FR-SEL-06, #580).
     *
     * @param sellerId the seller's own user ID (required)
     * @param orderId  the ID of the order to retrieve (required)
     * @return the OrderResponseDTO for the order
     * @throws AccessDeniedException if the order does not exist or does
     *                                not belong to this seller
     */
    @Override
    public OrderResponseDTO getSellerOrderById(Long sellerId, Long orderId) {
        log.info("Fetching order {} for seller {}", orderId, sellerId);
        Order order = orderRepository
                .findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Order not found or does not belong to "
                                + "seller with id: " + sellerId));
        return mapToResponseDTO(order);
    }

    /**
     * Seller-scoped order status update (FR-SEL-06, #580). Reuses the
     * same {@link #VALID_TRANSITIONS} state machine as the admin
     * transition — PAID/PAYMENT_FAILED never appear as reachable targets
     * there, so a seller can never set a payment-webhook-only status
     * through this path without any extra restriction logic needed.
     *
     * @param sellerId  the seller's own user ID (required)
     * @param orderId   the ID of the order to update (required)
     * @param newStatus the requested target status (required)
     * @return the updated OrderResponseDTO
     * @throws AccessDeniedException   if the order does not belong to
     *                                  this seller
     * @throws IllegalArgumentException if newStatus is invalid or the
     *                                   transition is not allowed
     */
    @Override
    @Transactional
    public OrderResponseDTO updateSellerOrderStatus(
            Long sellerId, Long orderId, String newStatus) {
        log.info("Seller {} updating order {} to status {}",
                sellerId, orderId, newStatus);
        Order order = orderRepository
                .findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Order not found or does not belong to "
                                + "seller with id: " + sellerId));

        OrderStatus targetStatus = parseStatus(newStatus);
        validateTransition(order.getStatus(), targetStatus);

        String previousStatus = order.getStatus().name();
        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        domainEventPublisher.publish(
                new OrderStatusChangedEvent(this, saved.getId(),
                        saved.getUser().getId(), previousStatus,
                        saved.getStatus().name()));

        switch (targetStatus) {
            case SHIPPED -> notificationService.sendShipmentNotification(
                    saved.getId(), saved.getTrackingNumber());
            case DELIVERED ->
                    notificationService.sendDeliveryNotification(
                            saved.getId());
            case CANCELLED -> log.info(
                    "Order {} cancelled by seller {}",
                    saved.getId(), sellerId);
            default -> { /* no notification for other transitions */ }
        }

        return mapToResponseDTO(saved);
    }

    private OrderStatus parseStatus(String newStatus) {
        try {
            return OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid order status: " + newStatus);
        }
    }

    private void validateTransition(
            OrderStatus currentStatus, OrderStatus targetStatus) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (!allowed.contains(targetStatus)) {
            throw new IllegalArgumentException(
                    "Cannot transition order from " + currentStatus
                            + " to " + targetStatus);
        }
    }

    private AdminOrderDetailDTO mapToAdminDetailDTO(Order order) {
        List<OrderItemDTO> items = order.getOrderItems() == null
                ? Collections.emptyList()
                : order.getOrderItems().stream()
                        .map(item -> new OrderItemDTO(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getDiscountAmount(),
                                item.getSubtotal()))
                        .collect(Collectors.toList());

        return new AdminOrderDetailDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getTrackingNumber(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getOrderNumber(),
                order.getOrderGroup() != null
                        ? order.getOrderGroup().getId() : null,
                order.getStatus().toString(),
                order.getTotalAmount(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
