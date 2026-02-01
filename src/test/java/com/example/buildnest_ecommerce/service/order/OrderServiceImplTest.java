package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setIsDeleted(false);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should filter deleted orders")
    void testGetAllOrdersFiltersDeleted() {
        Order deleted = new Order();
        deleted.setUser(user);
        deleted.setIsDeleted(true);

        when(orderRepository.findAll()).thenReturn(List.of(order, deleted));

        List<Order> result = orderService.getAllOrders();
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsDeleted());
    }

    @Test
    @DisplayName("Should get order by id")
    void testGetOrderById() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Order found = orderService.getOrderById(100L);
        assertEquals(order, found);
    }

    @Test
    @DisplayName("Should throw when order not found")
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(200L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.getOrderById(200L));
        assertTrue(ex.getMessage().contains("Order not found"));
    }

    @Test
    @DisplayName("Should create order and publish event")
    void testCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order created = orderService.createOrder(order);
        assertEquals(Order.OrderStatus.PENDING, created.getStatus());
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should update order fields")
    void testUpdateOrder() {
        Order update = new Order();
        update.setTotalAmount(new BigDecimal("150.00"));
        com.example.buildnest_ecommerce.model.entity.Address address = new com.example.buildnest_ecommerce.model.entity.Address();
        update.setShippingAddress(address);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order updated = orderService.updateOrder(100L, update);
        assertEquals(new BigDecimal("150.00"), updated.getTotalAmount());
        assertEquals(address, updated.getShippingAddress());
    }

    @Test
    @DisplayName("Should soft delete order")
    void testDeleteOrder() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.deleteOrder(100L);

        assertTrue(order.getIsDeleted());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should update order status and publish event")
    void testUpdateOrderStatus() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order updated = orderService.updateOrderStatus(100L, "SHIPPED");
        assertEquals(Order.OrderStatus.SHIPPED, updated.getStatus());
        verify(domainEventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw on invalid status")
    void testUpdateOrderStatusInvalid() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(100L, "bad"));
        assertTrue(ex.getMessage().contains("Invalid order status"));
    }

    @Test
    @DisplayName("Should map order responses")
    void testGetOrderResponsesByUserId() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponseDTO> responses = orderService.getOrderResponsesByUserId(7L);
        assertEquals(1, responses.size());
        assertEquals(7L, responses.get(0).getUserId());
    }

    @Test
    @DisplayName("Should enforce ownership on user order lookup")
    void testGetUserOrderByIdOwnership() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.getUserOrderById(7L, 100L);
        assertEquals(7L, response.getUserId());

        assertThrows(IllegalAccessException.class, () -> orderService.getUserOrderById(8L, 100L));
    }
}
