package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
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
}
