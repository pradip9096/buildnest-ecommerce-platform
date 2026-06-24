package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.repository.*;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.checkout.CheckoutSession;
import com.example.buildnest_ecommerce.service.checkout.CheckoutSessionStore;
import com.example.buildnest_ecommerce.service.checkout.CheckoutStep;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the multi-step checkout flow (CHK-01, #76).
 *
 * <p>Redis (CheckoutSessionStore) and Razorpay (PaymentService) are mocked.
 * All other interactions use the real H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
class CheckoutFlowIntegrationTest {

    private static final String BASE = "/api/v1/checkout";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired ShippingMethodRepository shippingMethodRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean CheckoutSessionStore checkoutSessionStore;
    @MockBean PaymentService paymentService;
    @MockBean RateLimitUtil rateLimitUtil;

    private Long userId;
    private String userToken;
    private User regularUser;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);

        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        regularUser = new User();
        regularUser.setUsername("chkuser_" + System.nanoTime());
        regularUser.setEmail("chkuser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Chk");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userId = regularUser.getId();
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());
    }

    // ─── Helper factories ─────────────────────────────────────────────────────

    private Address savedAddress() {
        Address a = new Address();
        a.setUser(regularUser);
        a.setStreetAddress("123 Main St");
        a.setCity("Testville");
        a.setState("TS");
        a.setPostalCode("12345");
        a.setCountry("IN");
        a.setAddressType("SHIPPING");
        a.setIsDefault(true);
        return addressRepository.save(a);
    }

    private ShippingMethod savedShippingMethod() {
        ShippingMethod m = new ShippingMethod();
        m.setName("Standard Delivery");
        m.setDescription("3-5 business days");
        m.setBaseCost(new BigDecimal("50.00"));
        m.setCostPerKg(BigDecimal.ZERO);
        m.setEstimatedDaysMin(3);
        m.setEstimatedDaysMax(5);
        m.setIsActive(true);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return shippingMethodRepository.save(m);
    }

    private Cart savedCartWithItem() {
        Category cat = new Category();
        cat.setName("ChkCat_" + System.nanoTime());
        cat.setIsActive(true);
        cat = categoryRepository.save(cat);

        Product product = new Product();
        product.setName("ChkProduct_" + System.nanoTime());
        product.setPrice(new BigDecimal("200.00"));
        product.setCategory(cat);
        product.setIsActive(true);
        product = productRepository.save(product);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setQuantityInStock(100);
        inv.setQuantityReserved(0);
        inv.setMinimumStockLevel(5);
        inv.setStatus(InventoryStatus.IN_STOCK);
        inventoryRepository.save(inv);

        Cart cart = new Cart();
        cart.setUser(regularUser);
        cart.setItems(new ArrayList<>());
        cart = cartRepository.save(cart);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("200.00"));
        item = cartItemRepository.save(item);

        cart.getItems().add(item);
        return cart;
    }

    private Order savedPendingOrder() {
        Cart cart = savedCartWithItem();
        Order order = new Order();
        order.setUser(regularUser);
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("460.00"));
        order.setTaxAmount(new BigDecimal("20.00"));
        order.setShippingAmount(new BigDecimal("50.00"));
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // ─── Step 1: setAddress ──────────────────────────────────────────────────

    @Test
    @DisplayName("TC-CHK-001: POST /address — valid address → 200, session step=PENDING_SHIPPING")
    void setAddress_valid_returns200() throws Exception {
        Address address = savedAddress();
        savedCartWithItem();

        mockMvc.perform(post(BASE + "/address")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId": %d}
                                """.formatted(address.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.step", is("PENDING_SHIPPING")))
                .andExpect(jsonPath("$.data.addressId", is(address.getId().intValue())));

        verify(checkoutSessionStore).save(eq(userId), argThat(s -> s.getStep() == CheckoutStep.PENDING_SHIPPING));
    }

    @Test
    @DisplayName("TC-CHK-002: POST /address — address belonging to another user → 404")
    void setAddress_otherUsersAddress_returns404() throws Exception {
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName())).findFirst().orElseThrow();
        User other = new User();
        other.setUsername("other_" + System.nanoTime());
        other.setEmail("other_" + System.nanoTime() + "@test.com");
        other.setPassword(passwordEncoder.encode("Pass@1234!"));
        other.setFirstName("Other"); other.setLastName("User");
        other.setRoles(Set.of(userRole)); other.setIsActive(true);
        other = userRepository.save(other);

        Address otherAddr = new Address();
        otherAddr.setUser(other);
        otherAddr.setStreetAddress("999 Other St");
        otherAddr.setCity("City"); otherAddr.setState("ST");
        otherAddr.setPostalCode("00000"); otherAddr.setCountry("IN");
        otherAddr.setAddressType("SHIPPING"); otherAddr.setIsDefault(false);
        otherAddr = addressRepository.save(otherAddr);

        mockMvc.perform(post(BASE + "/address")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId": %d}
                                """.formatted(otherAddr.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-CHK-003: POST /address — unauthenticated → 401")
    void setAddress_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\": 1}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Step 2: selectShipping ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-CHK-004: POST /shipping — no prior session → 409 Conflict")
    void selectShipping_noSession_returns409() throws Exception {
        ShippingMethod method = savedShippingMethod();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.empty());

        mockMvc.perform(post(BASE + "/shipping")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingMethodId": %d}
                                """.formatted(method.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-CHK-005: POST /shipping — session in wrong step → 409 Conflict")
    void selectShipping_wrongStep_returns409() throws Exception {
        ShippingMethod method = savedShippingMethod();
        Address address = savedAddress();
        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).step(CheckoutStep.PENDING_PAYMENT).addressId(address.getId()).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/shipping")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingMethodId": %d}
                                """.formatted(method.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-CHK-006: POST /shipping — valid method → 200, step=PENDING_PAYMENT")
    void selectShipping_valid_returns200() throws Exception {
        ShippingMethod method = savedShippingMethod();
        Address address = savedAddress();
        Cart cart = savedCartWithItem();

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).cartId(cart.getId()).step(CheckoutStep.PENDING_SHIPPING)
                .addressId(address.getId()).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/shipping")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingMethodId": %d}
                                """.formatted(method.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.step", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.data.shippingCost", notNullValue()));

        verify(checkoutSessionStore).save(eq(userId), argThat(s -> s.getStep() == CheckoutStep.PENDING_PAYMENT));
    }

    @Test
    @DisplayName("TC-CHK-007: POST /shipping — inactive method → 404")
    void selectShipping_inactiveMethod_returns404() throws Exception {
        ShippingMethod method = savedShippingMethod();
        method.setIsActive(false);
        shippingMethodRepository.save(method);
        Address address = savedAddress();
        Cart cart = savedCartWithItem();

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).cartId(cart.getId()).step(CheckoutStep.PENDING_SHIPPING)
                .addressId(address.getId()).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/shipping")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingMethodId": %d}
                                """.formatted(method.getId())))
                .andExpect(status().isNotFound());
    }

    // ─── Step 3: initiatePayment ─────────────────────────────────────────────

    @Test
    @DisplayName("TC-CHK-008: POST /payment — wrong step → 409 Conflict")
    void initiatePayment_wrongStep_returns409() throws Exception {
        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).step(CheckoutStep.PENDING_SHIPPING).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/payment")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-CHK-009: POST /payment — valid session → 200, order created, step=PENDING_CONFIRM")
    void initiatePayment_valid_returns200() throws Exception {
        Cart cart = savedCartWithItem();

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).cartId(cart.getId()).step(CheckoutStep.PENDING_PAYMENT)
                .addressId(1L).shippingMethodId(1L).shippingCost(new BigDecimal("50.00")).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        Payment mockPayment = new Payment();
        mockPayment.setId(1L);
        mockPayment.setRazorpayOrderId("rzp_order_test_123");
        mockPayment.setStatus("PENDING");
        when(paymentService.initiatePayment(anyLong(), anyDouble())).thenReturn(mockPayment);

        mockMvc.perform(post(BASE + "/payment")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.step", is("PENDING_CONFIRM")))
                .andExpect(jsonPath("$.data.orderId", notNullValue()))
                .andExpect(jsonPath("$.data.razorpayOrderId", is("rzp_order_test_123")));

        verify(checkoutSessionStore).save(eq(userId), argThat(s -> s.getStep() == CheckoutStep.PENDING_CONFIRM));
    }

    // ─── Step 4: confirmCheckout ─────────────────────────────────────────────

    @Test
    @DisplayName("TC-CHK-010: POST /confirm — wrong step → 409 Conflict")
    void confirmCheckout_wrongStep_returns409() throws Exception {
        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).step(CheckoutStep.PENDING_PAYMENT).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/confirm")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-CHK-011: POST /confirm — valid session → 200, order CONFIRMED, session deleted")
    void confirmCheckout_valid_returns200() throws Exception {
        Order order = savedPendingOrder();

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId).step(CheckoutStep.PENDING_CONFIRM).orderId(order.getId()).build();
        when(checkoutSessionStore.find(userId)).thenReturn(Optional.of(session));

        mockMvc.perform(post(BASE + "/confirm")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(checkoutSessionStore).delete(userId);

        Order confirmed = orderRepository.findById(order.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());
    }
}
