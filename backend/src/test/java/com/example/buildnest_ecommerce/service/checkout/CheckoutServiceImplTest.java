package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.cart.CartService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl tests")
class CheckoutServiceImplTest {

    @Mock private CartService cartService;
    @Mock private InventoryService inventoryService;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ShippingMethodRepository shippingMethodRepository;
    @Mock private PaymentService paymentService;
    @Mock private CheckoutSessionStore checkoutSessionStore;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private Cart buildCart(Long userId, Long cartId) {
        User user = new User();
        user.setId(userId);

        Product product = new Product();
        product.setId(5L);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("100"));

        java.util.List<CartItem> items = java.util.List.of(item);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUser(user);
        cart.setItems(items);
        item.setCart(cart);
        return cart;
    }

    @Test
    @DisplayName("Should validate checkout with sufficient stock")
    void testValidateCheckout() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);

        assertTrue(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should fail validation when cart missing")
    void testValidateCheckoutMissingCart() {
        when(cartRepository.findById(10L)).thenReturn(Optional.empty());

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should calculate final total")
    void testCalculateFinalTotal() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        Double total = checkoutService.calculateFinalTotal(10L);
        assertTrue(total > 0.0);
    }

    @Test
    @DisplayName("Should complete checkout cart flow")
    void testCheckoutCart() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        Order order = checkoutService.checkoutCart(1L, 10L);
        assertNotNull(order.getId());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertEquals(cart.getUser(), saved.getUser());
        assertNotNull(saved.getOrderNumber());
        assertNotNull(saved.getCreatedAt());
        assertEquals(Order.OrderStatus.PENDING, saved.getStatus());
        assertEquals(0, new BigDecimal("260.00").compareTo(saved.getTotalAmount()));
        assertEquals(0, new BigDecimal("10.00").compareTo(saved.getTaxAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(saved.getShippingAmount()));
        assertEquals(1, saved.getOrderItems().size());
        OrderItem savedItem = saved.getOrderItems().iterator().next();
        assertEquals(5L, savedItem.getProduct().getId());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(0, new BigDecimal("100").compareTo(savedItem.getPrice()));

        verify(cartService).clearCart(1L);
        verify(inventoryService).deductStock(5L, 2);
        // Kill survived mutants: setSubtotal and setOrder removal
        OrderItem savedItem2 = saved.getOrderItems().iterator().next();
        assertEquals(0, new BigDecimal("200.00").compareTo(savedItem2.getSubtotal()),
                "OrderItem.subtotal must equal price × quantity");
        assertEquals(saved, savedItem2.getOrder(), "OrderItem.order must reference the parent Order");
        // Kill survived mutant: generateOrderNumber returns ""
        assertTrue(saved.getOrderNumber().startsWith("ORD-"),
                "Order number must start with ORD-");
    }

    @Test
    @DisplayName("Should complete checkout with payment flow")
    void testCheckoutWithPayment() {
        Cart cart = buildCart(1L, 10L);
        User user = cart.getUser();

        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(200L);
            return order;
        });

        Order order = checkoutService.checkoutWithPayment(1L, 10L, new CheckoutRequestDTO());
        assertNotNull(order.getId());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertEquals(user, saved.getUser());
        assertNotNull(saved.getOrderNumber());
        assertNotNull(saved.getCreatedAt());
        assertEquals(Order.OrderStatus.PENDING, saved.getStatus());
        assertEquals(0, new BigDecimal("260.00").compareTo(saved.getTotalAmount()));
        assertEquals(0, new BigDecimal("10.00").compareTo(saved.getTaxAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(saved.getShippingAmount()));
        assertEquals(1, saved.getOrderItems().size());
        OrderItem savedItem = saved.getOrderItems().iterator().next();
        assertEquals(5L, savedItem.getProduct().getId());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(0, new BigDecimal("100").compareTo(savedItem.getPrice()));

        verify(cartService).clearCart(1L);
        verify(inventoryService).deductStock(5L, 2);
        // Kill survived mutants: setCreatedAt, setStatus, setSubtotal, setOrder removal
        assertNotNull(saved.getCreatedAt(), "Order.createdAt must be set");
        assertEquals(Order.OrderStatus.PENDING, saved.getStatus(), "Order.status must be PENDING");
        OrderItem savedItem2b = saved.getOrderItems().iterator().next();
        assertEquals(0, new BigDecimal("200.00").compareTo(savedItem2b.getSubtotal()),
                "OrderItem.subtotal must equal price × quantity");
        assertEquals(saved, savedItem2b.getOrder(), "OrderItem.order must reference the parent Order");
    }

    @Test
    @DisplayName("Should throw when cart belongs to different user")
    void testCheckoutCartWrongUser() {
        Cart cart = buildCart(2L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.checkoutCart(1L, 10L));
    }

    @Test
    @DisplayName("Should throw when cart missing")
    void testCheckoutCartMissing() {
        when(cartRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> checkoutService.checkoutCart(1L, 10L));
    }

    @Test
    @DisplayName("Should fail validation when cart has no items")
    void testValidateCheckoutEmptyCart() {
        Cart cart = buildCart(1L, 10L);
        cart.setItems(java.util.List.of());
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should fail validation when stock is insufficient")
    void testValidateCheckoutInsufficientStock() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(false);

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should fail validation when cart belongs to another user")
    void testValidateCheckoutWrongUser() {
        Cart cart = buildCart(2L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        // Must stub hasStock so the mutant (replacing equality check with false) would
        // skip the ownership check, reach hasStock returning true, and return true —
        // making the mutation detectable by this assertFalse.
        // lenient: in normal execution hasStock is never reached (ownership check returns false first);
        // PIT's mutant skips the ownership check and calls hasStock — the true return makes the mutant return true,
        // which our assertFalse detects, killing the mutant.
        lenient().when(inventoryService.hasStock(any(), anyInt())).thenReturn(true);

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should return false when validation throws exception")
    void testValidateCheckoutHandlesException() {
        when(cartRepository.findById(10L)).thenThrow(new RuntimeException("db error"));

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should calculate final total including tax and shipping")
    void testCalculateFinalTotalExpected() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        Double total = checkoutService.calculateFinalTotal(10L);
        assertEquals(260.0, total, 0.001);
    }

    // ─── Multi-step checkout: setAddress ────────────────────────────────────

    private Address buildAddress(Long addressId, Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Address address = new Address();
        address.setId(addressId);
        address.setUser(owner);
        return address;
    }

    private CheckoutSession sessionAt(Long userId, CheckoutStep step) {
        return CheckoutSession.builder().userId(userId).step(step).cartId(10L).build();
    }

    @Test
    @DisplayName("setAddress — happy path returns DTO with PENDING_SHIPPING step")
    void setAddress_happyPath_returnsPendingShipping() {
        Address address = buildAddress(20L, 1L);
        User user = address.getUser();
        Cart cart = buildCart(1L, 10L);

        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CheckoutSessionDTO dto = checkoutService.setAddress(1L, 20L);

        assertEquals(CheckoutStep.PENDING_SHIPPING, dto.getStep());
        assertEquals(20L, dto.getAddressId());
        assertEquals(1L, dto.getUserId());
        verify(checkoutSessionStore).save(eq(1L), any(CheckoutSession.class));
    }

    @Test
    @DisplayName("setAddress — address not found throws ResourceNotFoundException")
    void setAddress_addressNotFound_throws() {
        when(addressRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> checkoutService.setAddress(1L, 20L));
        verify(checkoutSessionStore, never()).save(any(), any());
    }

    @Test
    @DisplayName("setAddress — address belongs to different user throws ResourceNotFoundException")
    void setAddress_addressWrongOwner_throws() {
        Address address = buildAddress(20L, 99L); // owned by user 99, not 1
        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));

        assertThrows(RuntimeException.class, () -> checkoutService.setAddress(1L, 20L));
        verify(checkoutSessionStore, never()).save(any(), any());
    }

    @Test
    @DisplayName("setAddress — empty cart throws IllegalArgumentException")
    void setAddress_emptyCart_throws() {
        Address address = buildAddress(20L, 1L);
        User user = address.getUser();
        Cart emptyCart = new Cart();
        emptyCart.setId(10L);
        emptyCart.setUser(user);
        emptyCart.setItems(java.util.List.of());

        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(emptyCart));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.setAddress(1L, 20L));
        verify(checkoutSessionStore, never()).save(any(), any());
    }

    // ─── Multi-step checkout: selectShipping ────────────────────────────────

    @Test
    @DisplayName("selectShipping — happy path returns DTO with PENDING_PAYMENT step")
    void selectShipping_happyPath_returnsPendingPayment() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_SHIPPING);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        ShippingMethod method = new ShippingMethod();
        method.setId(5L);
        method.setName("Standard");
        method.setBaseCost(new BigDecimal("50.00"));
        method.setIsActive(true);
        when(shippingMethodRepository.findById(5L)).thenReturn(Optional.of(method));

        CheckoutSessionDTO dto = checkoutService.selectShipping(1L, 5L);

        assertEquals(CheckoutStep.PENDING_PAYMENT, dto.getStep());
        assertEquals(5L, dto.getShippingMethodId());
        assertEquals(0, new BigDecimal("50.00").compareTo(dto.getShippingCost()));
        verify(checkoutSessionStore).save(eq(1L), any(CheckoutSession.class));
    }

    @Test
    @DisplayName("selectShipping — no session throws 409 Conflict")
    void selectShipping_noSession_throwsConflict() {
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> checkoutService.selectShipping(1L, 5L));
    }

    @Test
    @DisplayName("selectShipping — session at wrong step throws 409 Conflict")
    void selectShipping_wrongStep_throwsConflict() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_PAYMENT);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        assertThrows(ResponseStatusException.class, () -> checkoutService.selectShipping(1L, 5L));
    }

    @Test
    @DisplayName("selectShipping — inactive shipping method throws ResourceNotFoundException")
    void selectShipping_inactiveMethod_throws() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_SHIPPING);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        ShippingMethod inactive = new ShippingMethod();
        inactive.setId(5L);
        inactive.setIsActive(false);
        when(shippingMethodRepository.findById(5L)).thenReturn(Optional.of(inactive));

        assertThrows(RuntimeException.class, () -> checkoutService.selectShipping(1L, 5L));
    }

    // ─── Multi-step checkout: initiatePayment ───────────────────────────────

    @Test
    @DisplayName("initiatePayment — happy path reserves inventory and returns PENDING_CONFIRM")
    void initiatePayment_happyPath_returnsPendingConfirm() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_PAYMENT)
                .cartId(10L).shippingCost(new BigDecimal("50.00")).build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L);
        // validateCheckout needs cartRepository.findById + inventoryService.hasStock
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setTotalAmount(new BigDecimal("260.00"));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Payment payment = new Payment();
        payment.setRazorpayOrderId("rzp_order_123");
        when(paymentService.initiatePayment(100L, 260.0)).thenReturn(payment);

        CheckoutSessionDTO dto = checkoutService.initiatePayment(1L);

        assertEquals(CheckoutStep.PENDING_CONFIRM, dto.getStep());
        assertEquals(100L, dto.getOrderId());
        assertEquals("rzp_order_123", dto.getRazorpayOrderId());
        verify(inventoryService).reserveStock(eq(5L), eq(2), any());
        verify(checkoutSessionStore).save(eq(1L), any(CheckoutSession.class));
    }

    @Test
    @DisplayName("initiatePayment — invalid cart releases nothing and throws")
    void initiatePayment_invalidCart_throws() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_PAYMENT).cartId(10L).build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));
        // validateCheckout → cartRepository returns empty → returns false
        when(cartRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> checkoutService.initiatePayment(1L));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("initiatePayment — order save failure releases inventory reservations")
    void initiatePayment_orderSaveFails_releasesReservations() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_PAYMENT)
                .cartId(10L).shippingCost(new BigDecimal("50.00")).build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> checkoutService.initiatePayment(1L));
        verify(inventoryService).releaseReservation(5L, 2);
    }

    // ─── Multi-step checkout: confirmCheckout ───────────────────────────────

    @Test
    @DisplayName("confirmCheckout — happy path confirms order, clears cart, deletes session")
    void confirmCheckout_happyPath_returnsOrderResponseDTO() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_CONFIRM)
                .orderId(100L).cartId(10L).build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        User user = new User();
        user.setId(1L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setOrderNumber("ORD-ABCD1234");
        order.setTotalAmount(new BigDecimal("260.00"));
        order.setTaxAmount(new BigDecimal("10.00"));
        order.setShippingAmount(new BigDecimal("50.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO dto = checkoutService.confirmCheckout(1L);

        assertEquals(100L, dto.getId());
        assertEquals("CONFIRMED", dto.getStatus());
        verify(inventoryService).deductStock(5L, 2);
        verify(cartService).clearCart(1L);
        verify(checkoutSessionStore).delete(1L);
    }

    @Test
    @DisplayName("confirmCheckout — wrong step throws 409 Conflict")
    void confirmCheckout_wrongStep_throwsConflict() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_PAYMENT);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        assertThrows(ResponseStatusException.class, () -> checkoutService.confirmCheckout(1L));
        verify(orderRepository, never()).findById(any());
    }
}
