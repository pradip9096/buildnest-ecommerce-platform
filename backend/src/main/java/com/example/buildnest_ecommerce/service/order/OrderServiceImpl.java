package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.OrderPlacedEvent;
import com.example.buildnest_ecommerce.event.OrderStatusChangedEvent;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
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
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Retrieves all active (non-deleted) orders.
     *
     * @return a list of all non-deleted orders
     */
    @Override
    public List<Order> getAllOrders() {
        log.info("Fetching all non-deleted orders");
        return orderRepository.findAll().stream()
                .filter(o -> !o.getIsDeleted())
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
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    /**
     * Creates a new order.
     *
     * Sets initial status to PENDING, timestamps, and publishes OrderPlacedEvent.
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
        domainEventPublisher.publish(new OrderPlacedEvent(this, saved, saved.getUser().getId()));
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
                .filter(o -> o.getUser().getId().equals(userId) && !o.getIsDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an order.
     *
     * Validates the new status, updates the order, and publishes
     * OrderStatusChangedEvent.
     *
     * @param orderId the ID of the order to update (required)
     * @param status  the new order status (required, must be valid OrderStatus enum
     *                value)
     * @return the updated Order entity
     * @throws RuntimeException if order is not found or status is invalid
     */
    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status with id: {}, status: {}", orderId, status);
        Order order = getOrderById(orderId);
        try {
            String previousStatus = order.getStatus().toString();
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            order.setUpdatedAt(LocalDateTime.now());
            Order saved = orderRepository.save(order);
            domainEventPublisher.publish(
                    new OrderStatusChangedEvent(this, saved.getId(), previousStatus, saved.getStatus().toString()));
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
     * Verifies that the order belongs to the specified user before returning it.
     *
     * @param userId  the ID of the user (required)
     * @param orderId the ID of the order to retrieve (required)
     * @return the OrderResponseDTO for the order
     * @throws RuntimeException       if order is not found
     * @throws IllegalAccessException if order does not belong to the specified user
     */
    @Override
    public OrderResponseDTO getUserOrderById(Long userId, Long orderId) throws IllegalAccessException {
        log.info("Fetching order response for user: {}, order: {}", userId, orderId);
        Order order = getOrderById(orderId);

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            log.warn("Access denied: User {} tried to access order {} of user {}", userId, orderId,
                    order.getUser().getId());
            throw new IllegalAccessException("Access denied: This order does not belong to you");
        }

        return mapToResponseDTO(order);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getStatus().toString(),
                order.getTotalAmount().doubleValue(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
