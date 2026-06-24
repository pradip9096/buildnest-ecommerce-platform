package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.dto.AdminOrderDetailDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
    Order createOrder(Order order);
    Order updateOrder(Long orderId, Order order);
    void deleteOrder(Long orderId);
    List<Order> getOrdersByUserId(Long userId);
    Order updateOrderStatus(Long orderId, String status);
    List<OrderResponseDTO> getOrderResponsesByUserId(Long userId);
    OrderResponseDTO getUserOrderById(Long userId, Long orderId) throws IllegalAccessException;

    Page<AdminOrderDetailDTO> getAdminOrders(Order.OrderStatus status, Long userId,
            LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    AdminOrderDetailDTO getAdminOrderDetail(Long orderId);

    Order adminUpdateOrderStatus(Long orderId, String newStatus, String cancellationReason);
}
