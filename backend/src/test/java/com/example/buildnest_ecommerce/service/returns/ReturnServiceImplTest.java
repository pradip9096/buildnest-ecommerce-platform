package com.example.buildnest_ecommerce.service.returns;

import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.model.entity.OrderItem;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ReturnRequestRepository;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnServiceImpl tests")
class ReturnServiceImplTest {

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ReturnServiceImpl returnService;

    private Order order;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);

        product = new Product();
        product.setId(500L);

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(2);

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setOrderNumber("ORD-100");
        order.setStatus(OrderStatus.DELIVERED);
        order.setTotalAmount(new BigDecimal("199.99"));
        order.setDeliveredAt(LocalDateTime.now().minusDays(5));
        order.setOrderItems(Set.of(item));
    }

    @Test
    @DisplayName("createReturnRequest succeeds within the 30-day window")
    void createReturnRequest_withinWindow_succeeds() {
        when(orderRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(order));
        when(returnRequestRepository
                .findByOrderIdAndStatusIn(eq(100L), any()))
                .thenReturn(Optional.empty());
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(inv -> {
                    ReturnRequest rr = inv.getArgument(0);
                    rr.setId(1L);
                    return rr;
                });

        ReturnRequestDTO result = returnService
                .createReturnRequest(7L, 100L, "Wrong size");

        assertEquals(ReturnStatus.PENDING.name(), result.getStatus());
        assertEquals(100L, result.getOrderId());
        assertEquals("Wrong size", result.getReason());
        assertEquals(7L, result.getUserId());
    }

    @Test
    @DisplayName(
            "createReturnRequest throws when order belongs to another user")
    void createReturnRequest_wrongOwner_throwsAccessDenied() {
        when(orderRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
                () -> returnService.createReturnRequest(
                        999L, 100L, "Wrong size"));
    }

    @Test
    @DisplayName(
            "createReturnRequest throws when order is not yet delivered")
    void createReturnRequest_notDelivered_throwsValidation() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(order));

        assertThrows(ValidationException.class,
                () -> returnService.createReturnRequest(
                        7L, 100L, "Wrong size"));
    }

    @Test
    @DisplayName(
            "createReturnRequest throws when the 30-day window has "
                    + "expired (boundary: day 31)")
    void createReturnRequest_windowExpired_throwsValidation() {
        order.setDeliveredAt(LocalDateTime.now().minusDays(31));
        when(orderRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(order));

        assertThrows(ValidationException.class,
                () -> returnService.createReturnRequest(
                        7L, 100L, "Wrong size"));
    }

    @Test
    @DisplayName(
            "createReturnRequest throws when an active return already "
                    + "exists for the order")
    void createReturnRequest_activeReturnExists_throwsValidation() {
        when(orderRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(order));
        when(returnRequestRepository
                .findByOrderIdAndStatusIn(eq(100L), any()))
                .thenReturn(Optional.of(new ReturnRequest()));

        assertThrows(ValidationException.class,
                () -> returnService.createReturnRequest(
                        7L, 100L, "Wrong size"));
    }

    @Test
    @DisplayName(
            "updateReturnStatus(APPROVED) triggers refund and restores "
                    + "inventory")
    void updateReturnStatus_approved_triggersRefundAndRestocksInventory() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.PENDING);

        when(returnRequestRepository.findById(1L))
                .thenReturn(Optional.of(returnRequest));
        when(paymentService.processRefund(
                eq(100L), any(Double.class), any(String.class)))
                .thenReturn(new Payment());
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReturnRequestDTO result = returnService
                .updateReturnStatus(1L, "APPROVED", "Approved by admin");

        assertEquals(ReturnStatus.REFUNDED.name(), result.getStatus());
        assertEquals(0, new BigDecimal("199.99")
                .compareTo(result.getRefundAmount()));
        assertEquals("Approved by admin", result.getAdminNotes());
        assertNotNull(result.getResolvedAt());
        verify(paymentService, times(1)).processRefund(
                eq(100L), any(Double.class), any(String.class));
        verify(inventoryService, times(1))
                .adjustStock(eq(500L), eq(2), any(String.class), eq(null));
    }

    @Test
    @DisplayName(
            "updateReturnStatus(REJECTED) does not trigger refund or "
                    + "inventory restoration")
    void updateReturnStatus_rejected_doesNotTriggerRefundOrRestock() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.PENDING);

        when(returnRequestRepository.findById(1L))
                .thenReturn(Optional.of(returnRequest));
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReturnRequestDTO result = returnService
                .updateReturnStatus(1L, "REJECTED", "Item damaged");

        assertEquals(ReturnStatus.REJECTED.name(), result.getStatus());
        assertEquals("Item damaged", result.getAdminNotes());
        assertNotNull(result.getResolvedAt());
        verify(paymentService, never())
                .processRefund(anyLong(), any(Double.class), any());
        verify(inventoryService, never())
                .adjustStock(anyLong(), any(Integer.class), any(), any());
    }

    @Test
    @DisplayName(
            "updateReturnStatus throws when newStatus is a valid enum "
                    + "value but neither APPROVED nor REJECTED")
    void updateReturnStatus_validButUnsupportedStatus_throwsValidation() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.PENDING);

        when(returnRequestRepository.findById(1L))
                .thenReturn(Optional.of(returnRequest));

        assertThrows(ValidationException.class,
                () -> returnService.updateReturnStatus(
                        1L, "PENDING", "no-op"));
        verify(paymentService, never())
                .processRefund(anyLong(), any(Double.class), any());
    }

    @Test
    @DisplayName("updateReturnStatus throws when newStatus is null")
    void updateReturnStatus_nullStatus_throwsValidation() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.PENDING);

        when(returnRequestRepository.findById(1L))
                .thenReturn(Optional.of(returnRequest));

        assertThrows(ValidationException.class,
                () -> returnService.updateReturnStatus(1L, null, "notes"));
    }

    @Test
    @DisplayName(
            "updateReturnStatus throws when the return is not PENDING")
    void updateReturnStatus_notPending_throwsValidation() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.REJECTED);

        when(returnRequestRepository.findById(1L))
                .thenReturn(Optional.of(returnRequest));

        assertThrows(ValidationException.class,
                () -> returnService.updateReturnStatus(
                        1L, "APPROVED", "Approved by admin"));
    }

    @Test
    @DisplayName("createReturnRequest throws when order does not exist")
    void createReturnRequest_orderNotFound_throwsResourceNotFound() {
        when(orderRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> returnService.createReturnRequest(
                        7L, 999L, "Wrong size"));
    }

    @Test
    @DisplayName("getAdminReturnRequests delegates to repository with "
            + "a status filter")
    void getAdminReturnRequests_withStatusFilter_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReturnRequest> page = new PageImpl<>(List.of());
        when(returnRequestRepository.findAll(
                any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<ReturnRequestDTO> result = returnService
                .getAdminReturnRequests(ReturnStatus.PENDING, pageable);

        assertEquals(0, result.getTotalElements());
    }
}
