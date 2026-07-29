package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderGroupRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.SellerDistrictRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.entity.Coupon;
import com.example.buildnest_ecommerce.service.cart.CartService;
import com.example.buildnest_ecommerce.service.coupon.CouponService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl tests")
class CheckoutServiceImplTest {

    @Mock private CartService cartService;
    @Mock private InventoryService inventoryService;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderGroupRepository orderGroupRepository;
    @Mock private SellerDistrictRepository sellerDistrictRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ShippingMethodRepository shippingMethodRepository;
    @Mock private PaymentService paymentService;
    @Mock private CheckoutSessionStore checkoutSessionStore;
    @Mock private CouponService couponService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @BeforeEach
    void setUp() {
        // @InjectMocks leaves an unmocked Optional<T> constructor param as null (Spring itself
        // injects Optional.empty() correctly at runtime; this is purely a Mockito test-construction
        // gap) — set explicitly so userEventService.ifPresent(...) doesn't NPE.
        ReflectionTestUtils.setField(checkoutService, "userEventService", Optional.empty());
    }

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
        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Order> orders = invocation.getArgument(0);
            orders.get(0).setId(100L);
            return orders;
        });

        Order order = checkoutService.checkoutCart(1L, 10L);
        assertNotNull(order.getId());

        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orderCaptor.capture());
        Order saved = orderCaptor.getValue().get(0);
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
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Order> orders = invocation.getArgument(0);
            orders.get(0).setId(200L);
            return orders;
        });

        Order order = checkoutService.checkoutWithPayment(1L, 10L, new CheckoutRequestDTO());
        assertNotNull(order.getId());

        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orderCaptor.capture());
        Order saved = orderCaptor.getValue().get(0);
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

    // ─── District-scoped checkout restriction (FR-LOC-04, #564) ────────

    private Cart buildCartWithSeller(Long buyerId, Long cartId,
            Long sellerId, District buyerDistrict) {
        User buyer = new User();
        buyer.setId(buyerId);
        buyer.setDistrict(buyerDistrict);

        User seller = new User();
        seller.setId(sellerId);

        Product product = new Product();
        product.setId(5L);
        product.setSeller(seller);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("100"));

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUser(buyer);
        cart.setItems(java.util.List.of(item));
        item.setCart(cart);
        return cart;
    }

    private SellerDistrict declaredDistrict(Long districtId) {
        District district = new District();
        district.setId(districtId);
        SellerDistrict sellerDistrict = new SellerDistrict();
        sellerDistrict.setDistrict(district);
        return sellerDistrict;
    }

    @Test
    @DisplayName("Should allow checkout when seller has no declared "
            + "district restrictions")
    void testValidateCheckoutSellerUnrestricted() {
        District buyerDistrict = new District();
        buyerDistrict.setId(100L);
        Cart cart = buildCartWithSeller(1L, 10L, 20L, buyerDistrict);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(sellerDistrictRepository.findAllBySeller_User_Id(20L))
                .thenReturn(List.of());

        assertTrue(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should allow checkout when buyer's district is among "
            + "the seller's declared districts")
    void testValidateCheckoutBuyerWithinSellerDistrict() {
        District buyerDistrict = new District();
        buyerDistrict.setId(100L);
        Cart cart = buildCartWithSeller(1L, 10L, 20L, buyerDistrict);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(sellerDistrictRepository.findAllBySeller_User_Id(20L))
                .thenReturn(List.of(
                        declaredDistrict(99L), declaredDistrict(100L)));

        assertTrue(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should block checkout when buyer's district is not "
            + "among the seller's declared districts")
    void testValidateCheckoutBuyerOutsideSellerDistrict() {
        District buyerDistrict = new District();
        buyerDistrict.setId(100L);
        Cart cart = buildCartWithSeller(1L, 10L, 20L, buyerDistrict);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        // lenient: stubbed so a mutant skipping the district check still
        // reaches hasStock=true and would otherwise incorrectly return
        // true, letting assertFalse catch it (mirrors
        // testValidateCheckoutWrongUser's PIT-survival pattern).
        lenient().when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(sellerDistrictRepository.findAllBySeller_User_Id(20L))
                .thenReturn(List.of(declaredDistrict(99L)));

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should fail-closed and block checkout when buyer's "
            + "district cannot be determined and seller is restricted")
    void testValidateCheckoutBuyerDistrictUnknownFailsClosed() {
        Cart cart = buildCartWithSeller(1L, 10L, 20L, null);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        lenient().when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(sellerDistrictRepository.findAllBySeller_User_Id(20L))
                .thenReturn(List.of(declaredDistrict(99L)));

        assertFalse(checkoutService.validateCheckout(1L, 10L));
    }

    @Test
    @DisplayName("Should not query district repository for platform "
            + "items with no owning seller")
    void testValidateCheckoutPlatformItemSkipsDistrictCheck() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);

        assertTrue(checkoutService.validateCheckout(1L, 10L));
        verify(sellerDistrictRepository, never())
                .findAllBySeller_User_Id(any());
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
    @DisplayName("setAddress — records a CHECKOUT_STARTED event when UserEventService is present")
    void setAddress_recordsCheckoutStartedEventWhenUserEventServicePresent() {
        com.example.buildnest_ecommerce.service.analytics.UserEventService userEventService = mock(
                com.example.buildnest_ecommerce.service.analytics.UserEventService.class);
        ReflectionTestUtils.setField(checkoutService, "userEventService", Optional.of(userEventService));

        Address address = buildAddress(20L, 1L);
        User user = address.getUser();
        Cart cart = buildCart(1L, 10L);

        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        checkoutService.setAddress(1L, 20L);

        verify(userEventService).recordCheckoutStarted(1L);
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

        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Order> orders = invocation.getArgument(0);
            orders.get(0).setId(100L);
            return orders;
        });

        Payment payment = new Payment();
        payment.setRazorpayOrderId("rzp_order_123");
        when(paymentService.initiatePayment(eq(100L), any(Double.class)))
                .thenReturn(payment);

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
        verify(orderRepository, never()).saveAll(any());
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
        when(orderRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("DB error"));

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
        when(orderRepository.saveAll(anyList())).thenReturn(List.of(order));

        OrderResponseDTO dto = checkoutService.confirmCheckout(1L);

        assertEquals(100L, dto.getId());
        assertEquals("CONFIRMED", dto.getStatus());
        assertNull(dto.getOrderGroupId(),
                "single-seller order has no OrderGroup, so orderGroupId "
                        + "must be null, not 0 or omitted");
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

    // ===== applyCoupon (CHK-02, #77) =====

    private Coupon buildCoupon(Long id, String code, BigDecimal discountValue) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setCode(code);
        coupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        coupon.setDiscountValue(discountValue);
        return coupon;
    }

    @Test
    @DisplayName("applyCoupon — happy path stores couponId/code/discountAmount on the session at PENDING_SHIPPING")
    void applyCoupon_happyPathAtPendingShipping_storesDiscountOnSession() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_SHIPPING);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        Coupon coupon = buildCoupon(7L, "SAVE10", BigDecimal.TEN);
        when(couponService.validateCoupon(eq("SAVE10"), any(BigDecimal.class))).thenReturn(coupon);
        when(couponService.calculateDiscount(eq(coupon), any(BigDecimal.class))).thenReturn(new BigDecimal("20.00"));

        CheckoutSessionDTO dto = checkoutService.applyCoupon(1L, "SAVE10");

        assertEquals("SAVE10", dto.getCouponCode());
        assertEquals(0, new BigDecimal("20.00").compareTo(dto.getDiscountAmount()));

        ArgumentCaptor<CheckoutSession> captor = ArgumentCaptor.forClass(CheckoutSession.class);
        verify(checkoutSessionStore).save(eq(1L), captor.capture());
        assertEquals(7L, captor.getValue().getCouponId());
    }

    @Test
    @DisplayName("applyCoupon — also allowed at PENDING_PAYMENT (before payment is initiated)")
    void applyCoupon_allowedAtPendingPayment() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_PAYMENT);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        Coupon coupon = buildCoupon(7L, "SAVE10", BigDecimal.TEN);
        when(couponService.validateCoupon(eq("SAVE10"), any(BigDecimal.class))).thenReturn(coupon);
        when(couponService.calculateDiscount(eq(coupon), any(BigDecimal.class))).thenReturn(new BigDecimal("20.00"));

        assertDoesNotThrow(() -> checkoutService.applyCoupon(1L, "SAVE10"));
    }

    @Test
    @DisplayName("applyCoupon — rejected once payment has been initiated (PENDING_CONFIRM)")
    void applyCoupon_rejectedAtPendingConfirm_throwsConflict() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_CONFIRM);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        assertThrows(ResponseStatusException.class, () -> checkoutService.applyCoupon(1L, "SAVE10"));
        verifyNoInteractions(couponService);
    }

    @Test
    @DisplayName("applyCoupon — rejected when there is no active session")
    void applyCoupon_noSession_throwsConflict() {
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> checkoutService.applyCoupon(1L, "SAVE10"));
    }

    @Test
    @DisplayName("applyCoupon — an invalid coupon's ValidationException propagates without touching the session")
    void applyCoupon_invalidCoupon_propagatesValidationException() {
        CheckoutSession session = sessionAt(1L, CheckoutStep.PENDING_SHIPPING);
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(couponService.validateCoupon(eq("EXPIRED"), any(BigDecimal.class)))
                .thenThrow(new ValidationException("Coupon has expired: EXPIRED"));

        assertThrows(ValidationException.class, () -> checkoutService.applyCoupon(1L, "EXPIRED"));
        verify(checkoutSessionStore, never()).save(any(), any());
    }

    // ===== Discount applied through initiatePayment → order.discountAmount =====

    @Test
    @DisplayName("initiatePayment — applies the session's discountAmount to the created order's total")
    void initiatePayment_appliesDiscountToOrderTotal() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_PAYMENT).cartId(10L)
                .shippingCost(new BigDecimal("50"))
                .couponId(7L).couponCode("SAVE10").discountAmount(new BigDecimal("20.00"))
                .build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        Cart cart = buildCart(1L, 10L); // subtotal = price(100) * qty(2) = 200
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Order> orders = inv.getArgument(0);
            orders.get(0).setId(100L);
            return orders;
        });

        Payment payment = new Payment();
        payment.setRazorpayOrderId("razorpay_test");
        when(paymentService.initiatePayment(eq(100L), any(Double.class))).thenReturn(payment);

        checkoutService.initiatePayment(1L);

        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue().get(0);

        // subtotal 200 - discount 20 = 180; tax = 180 * 0.05 = 9.00; total = 180 + 9 + 50 = 239.00
        assertEquals(0, new BigDecimal("20.00").compareTo(savedOrder.getDiscountAmount()));
        assertEquals(0, new BigDecimal("9.00").compareTo(savedOrder.getTaxAmount()));
        assertEquals(0, new BigDecimal("239.00").compareTo(savedOrder.getTotalAmount()));
    }

    @Test
    @DisplayName("confirmCheckout — increments the applied coupon's usage count")
    void confirmCheckout_incrementsCouponUsageWhenCouponWasApplied() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_CONFIRM)
                .orderId(100L).cartId(10L).couponId(7L).couponCode("SAVE10")
                .discountAmount(new BigDecimal("20.00"))
                .build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        User user = new User();
        user.setId(1L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setOrderNumber("ORD-ABCD1234");
        order.setTotalAmount(new BigDecimal("239.00"));
        order.setDiscountAmount(new BigDecimal("20.00"));
        order.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(orderRepository.saveAll(anyList())).thenReturn(List.of(order));

        checkoutService.confirmCheckout(1L);

        verify(couponService).incrementUsage(7L);
    }

    @Test
    @DisplayName("confirmCheckout — does not touch CouponService when no coupon was applied")
    void confirmCheckout_noCouponApplied_doesNotIncrementUsage() {
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
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(orderRepository.saveAll(anyList())).thenReturn(List.of(order));

        checkoutService.confirmCheckout(1L);

        verifyNoInteractions(couponService);
    }

    // ===== Multi-seller order splitting (FR-SEL-06, #579) =====

    private Cart buildMultiSellerCart(Long userId, Long cartId) {
        User user = new User();
        user.setId(userId);

        User sellerA = new User();
        sellerA.setId(50L);
        User sellerB = new User();
        sellerB.setId(60L);

        Product productA = new Product();
        productA.setId(5L);
        productA.setSeller(sellerA);
        Product productB = new Product();
        productB.setId(6L);
        productB.setSeller(sellerB);

        CartItem itemA = new CartItem();
        itemA.setProduct(productA);
        itemA.setQuantity(2);
        itemA.setPrice(new BigDecimal("100"));

        CartItem itemB = new CartItem();
        itemB.setProduct(productB);
        itemB.setQuantity(1);
        itemB.setPrice(new BigDecimal("200"));

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUser(user);
        cart.setItems(java.util.List.of(itemA, itemB));
        itemA.setCart(cart);
        itemB.setCart(cart);
        return cart;
    }

    @Test
    @DisplayName("checkoutCart — single-seller cart creates one order with no OrderGroup")
    void checkoutCart_singleSeller_createsOneOrderNoGroup() {
        Cart cart = buildCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Order> orders = invocation.getArgument(0);
            orders.get(0).setId(100L);
            return orders;
        });

        checkoutService.checkoutCart(1L, 10L);

        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orderCaptor.capture());
        List<Order> saved = orderCaptor.getValue();
        assertEquals(1, saved.size());
        assertNull(saved.get(0).getOrderGroup(),
                "single-seller checkout must never create an OrderGroup");
        verifyNoInteractions(orderGroupRepository);
    }

    @Test
    @DisplayName("checkoutCart — multi-seller cart splits into one order per "
            + "seller, linked via a shared OrderGroup")
    void checkoutCart_multiSeller_splitsIntoOrdersLinkedByGroup() {
        Cart cart = buildMultiSellerCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(inventoryService.hasStock(5L, 2)).thenReturn(true);
        when(inventoryService.hasStock(6L, 1)).thenReturn(true);

        OrderGroup savedGroup = new OrderGroup();
        savedGroup.setId(900L);
        when(orderGroupRepository.save(any(OrderGroup.class)))
                .thenReturn(savedGroup);
        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Order> orders = invocation.getArgument(0);
            long nextId = 100L;
            for (Order order : orders) {
                order.setId(nextId++);
            }
            return orders;
        });

        Order primary = checkoutService.checkoutCart(1L, 10L);

        verify(orderGroupRepository).save(any(OrderGroup.class));
        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orderCaptor.capture());
        List<Order> saved = orderCaptor.getValue();

        assertEquals(2, saved.size(), "one order per seller");
        assertEquals(saved.get(0), primary,
                "primary order returned is the first seller-group order");
        for (Order order : saved) {
            assertEquals(savedGroup, order.getOrderGroup(),
                    "every sibling order must link to the same OrderGroup");
        }

        // subtotal split: seller A = 200 (2*100), seller B = 200 (1*200);
        // shipping (50) splits 50/50 by subtotal share.
        BigDecimal totalShipping = saved.stream()
                .map(Order::getShippingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("50.00").compareTo(totalShipping),
                "apportioned shipping across sibling orders must sum to the "
                        + "original shipping cost");
    }

    @Test
    @DisplayName("confirmCheckout — multi-seller group confirms every "
            + "sibling order as one unit")
    void confirmCheckout_multiSellerGroup_confirmsAllSiblingOrders() {
        CheckoutSession session = CheckoutSession.builder()
                .userId(1L).step(CheckoutStep.PENDING_CONFIRM)
                .orderId(100L).cartId(10L).build();
        when(checkoutSessionStore.find(1L)).thenReturn(Optional.of(session));

        User user = new User();
        user.setId(1L);
        OrderGroup group = new OrderGroup();
        group.setId(900L);

        Order primary = new Order();
        primary.setId(100L);
        primary.setUser(user);
        primary.setOrderNumber("ORD-AAAA1111");
        primary.setTotalAmount(new BigDecimal("139.00"));
        primary.setDiscountAmount(BigDecimal.ZERO);
        primary.setStatus(Order.OrderStatus.PENDING);
        primary.setOrderGroup(group);

        Order sibling = new Order();
        sibling.setId(101L);
        sibling.setUser(user);
        sibling.setOrderNumber("ORD-BBBB2222");
        sibling.setTotalAmount(new BigDecimal("229.00"));
        sibling.setDiscountAmount(BigDecimal.ZERO);
        sibling.setStatus(Order.OrderStatus.PENDING);
        sibling.setOrderGroup(group);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(primary));
        when(orderRepository.findByOrderGroupId(900L))
                .thenReturn(List.of(primary, sibling));
        when(orderRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart cart = buildMultiSellerCart(1L, 10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        OrderResponseDTO dto = checkoutService.confirmCheckout(1L);

        assertEquals(100L, dto.getId());
        assertEquals(900L, dto.getOrderGroupId(),
                "multi-seller confirmed order must expose its OrderGroup "
                        + "id so the buyer-facing UI can group sibling "
                        + "orders from one checkout (FR-SEL-06)");
        ArgumentCaptor<List<Order>> confirmCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(confirmCaptor.capture());
        List<Order> confirmed = confirmCaptor.getValue();
        assertEquals(2, confirmed.size());
        assertTrue(confirmed.stream()
                .allMatch(o -> o.getStatus() == Order.OrderStatus.CONFIRMED),
                "every sibling order in the group must be confirmed");
    }
}
