package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PaymentService paymentService;
    private final CheckoutSessionStore checkoutSessionStore;

    // ─── Multi-step checkout (CHK-01, #76) ───────────────────────────────────

    @Override
    @Transactional
    public CheckoutSessionDTO setAddress(Long userId, Long addressId) {
        log.info("Checkout step 1/4 — setAddress: user={}, address={}", userId, addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address not found: " + addressId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty — cannot start checkout");
        }

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId)
                .cartId(cart.getId())
                .step(CheckoutStep.PENDING_SHIPPING)
                .addressId(addressId)
                .build();
        checkoutSessionStore.save(userId, session);

        log.info("Checkout session created for user={}, cartId={}", userId, cart.getId());
        return toDTO(session);
    }

    @Override
    @Transactional
    public CheckoutSessionDTO selectShipping(Long userId, Long shippingMethodId) {
        log.info("Checkout step 2/4 — selectShipping: user={}, method={}", userId, shippingMethodId);

        CheckoutSession session = requireSession(userId, CheckoutStep.PENDING_SHIPPING);

        ShippingMethod method = shippingMethodRepository.findById(shippingMethodId)
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipping method not found or inactive: " + shippingMethodId));

        session.setShippingMethodId(shippingMethodId);
        session.setShippingCost(method.getBaseCost());
        session.setStep(CheckoutStep.PENDING_PAYMENT);
        checkoutSessionStore.save(userId, session);

        log.info("Shipping selected for user={}: method={}, cost={}", userId, method.getName(), method.getBaseCost());
        return toDTO(session);
    }

    private static final int RESERVATION_MINUTES = 15;

    @Override
    @Transactional
    public CheckoutSessionDTO initiatePayment(Long userId) {
        log.info("Checkout step 3/4 — initiatePayment: user={}", userId);

        CheckoutSession session = requireSession(userId, CheckoutStep.PENDING_PAYMENT);

        if (!validateCheckout(userId, session.getCartId())) {
            throw new IllegalArgumentException("Cart is no longer valid for checkout");
        }

        Cart cart = cartRepository.findById(session.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + session.getCartId()));

        // Reserve inventory before creating the order. If reservation fails (e.g. concurrent
        // checkout grabbed the last unit), the exception propagates and the cart is untouched.
        LocalDateTime reservationExpiry = LocalDateTime.now().plusMinutes(RESERVATION_MINUTES);
        reserveInventoryFromCart(cart, reservationExpiry);

        Order order = buildOrderFromCart(cart, session.getShippingCost());
        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
        } catch (Exception e) {
            // Roll back reservations if the order cannot be persisted
            releaseInventoryFromCart(cart);
            throw e;
        }

        Payment payment;
        try {
            payment = paymentService.initiatePayment(
                    savedOrder.getId(), savedOrder.getTotalAmount().doubleValue());
        } catch (Exception e) {
            // Payment gateway failure — release reservations so stock is available again
            log.error("Payment initiation failed for user={}, orderId={}; releasing reservations", userId, savedOrder.getId(), e);
            releaseInventoryFromCart(cart);
            throw e;
        }

        session.setOrderId(savedOrder.getId());
        session.setRazorpayOrderId(payment.getRazorpayOrderId());
        session.setStep(CheckoutStep.PENDING_CONFIRM);
        checkoutSessionStore.save(userId, session);

        log.info("Payment initiated for user={}, orderId={}", userId, savedOrder.getId());
        return toDTO(session);
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmCheckout(Long userId) {
        log.info("Checkout step 4/4 — confirmCheckout: user={}", userId);

        CheckoutSession session = requireSession(userId, CheckoutStep.PENDING_CONFIRM);

        Order order = orderRepository.findById(session.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + session.getOrderId()));

        // Retrieve the cart items to finalise the permanent deduction.
        // The cart was not cleared at initiatePayment — it is cleared here after the order is confirmed.
        Cart cart = cartRepository.findById(session.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + session.getCartId()));

        deductInventoryFromCart(cart);
        cartService.clearCart(userId);

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        Order confirmed = orderRepository.save(order);

        checkoutSessionStore.delete(userId);

        log.info("Checkout confirmed for user={}, orderId={}", userId, confirmed.getId());
        return toOrderDTO(confirmed);
    }

    private OrderResponseDTO toOrderDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getOrderNumber(),
                order.getStatus().toString(),
                order.getTotalAmount(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private CheckoutSession requireSession(Long userId, CheckoutStep expectedStep) {
        Optional<CheckoutSession> opt = checkoutSessionStore.find(userId);
        if (opt.isEmpty() || opt.get().getStep() != expectedStep) {
            String current = opt.map(s -> s.getStep().name()).orElse("NONE");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid checkout step. Expected " + expectedStep + " but session is " + current);
        }
        return opt.get();
    }

    private Order buildOrderFromCart(Cart cart, BigDecimal shippingCost) {
        BigDecimal cartTotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .map(price -> new BigDecimal(price.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = cartTotal.multiply(new BigDecimal("0.05"));
        BigDecimal shipping = shippingCost != null ? shippingCost : new BigDecimal("50");
        BigDecimal finalAmount = cartTotal.add(taxAmount).add(shipping);

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(finalAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shipping);

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal price = new BigDecimal(cartItem.getPrice().toString());
            orderItem.setPrice(price);
            orderItem.setSubtotal(price.multiply(new BigDecimal(cartItem.getQuantity())));
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        return order;
    }

    private CheckoutSessionDTO toDTO(CheckoutSession session) {
        return new CheckoutSessionDTO(
                session.getUserId(),
                session.getCartId(),
                session.getStep(),
                session.getAddressId(),
                session.getShippingMethodId(),
                session.getShippingCost(),
                session.getOrderId(),
                session.getRazorpayOrderId());
    }

    // ─── Legacy single-step checkout ─────────────────────────────────────────

    @Override
    @Transactional
    public Order checkoutCart(Long userId, Long cartId) {
        log.info("Starting checkout for user: {} with cart: {}", userId, cartId);

        // Validate checkout is possible
        if (!validateCheckout(userId, cartId)) {
            throw new IllegalArgumentException("Cart is not valid for checkout");
        }

        // Get cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        // Verify cart belongs to user
        if (!cart.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cart does not belong to the user");
        }

        // Create order from cart items
        Order order = createOrderFromCart(cart);

        // Deduct inventory
        deductInventoryFromCart(cart);

        // Clear cart after successful checkout
        cartService.clearCart(userId);

        log.info("Checkout completed for user: {}, Order ID: {}", userId, order.getId());
        return order;
    }

    @Override
    @Transactional
    public Order checkoutWithPayment(Long userId, Long cartId, CheckoutRequestDTO request) {
        log.info("Starting checkout with payment for user: {} with cart: {}", userId, cartId);

        // Validate checkout
        if (!validateCheckout(userId, cartId)) {
            throw new IllegalArgumentException("Cart is not valid for checkout");
        }

        // Get cart and user
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create order with payment details
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        // Calculate totals
        BigDecimal cartTotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .map(price -> new BigDecimal(price.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = cartTotal.multiply(new BigDecimal("0.05"));
        BigDecimal shippingAmount = new BigDecimal("50");
        BigDecimal finalAmount = cartTotal.add(taxAmount).add(shippingAmount);

        order.setTotalAmount(finalAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingAmount);

        // Convert cart items to order items
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal price = new BigDecimal(cartItem.getPrice().toString());
            orderItem.setPrice(price);
            orderItem.setSubtotal(price.multiply(new BigDecimal(cartItem.getQuantity())));
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Deduct inventory
        deductInventoryFromCart(cart);

        // Clear cart
        cartService.clearCart(userId);

        log.info("Checkout with payment completed. Order ID: {}", savedOrder.getId());
        return savedOrder;
    }

    @Override
    public boolean validateCheckout(Long userId, Long cartId) {
        log.debug("Validating checkout for user: {}, cart: {}", userId, cartId);

        try {
            // Check cart exists
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

            // Check cart belongs to user
            if (!cart.getUser().getId().equals(userId)) {
                log.warn("Cart does not belong to user: {}", userId);
                return false;
            }

            // Check cart has items
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                log.warn("Cart is empty");
                return false;
            }

            // Check all items have sufficient stock
            for (CartItem item : cart.getItems()) {
                if (!inventoryService.hasStock(item.getProduct().getId(), item.getQuantity())) {
                    log.warn("Insufficient stock for product: {}", item.getProduct().getId());
                    return false;
                }
            }

            log.debug("Cart validation successful for user: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Error validating checkout", e);
            return false;
        }
    }

    @Override
    public Double calculateFinalTotal(Long cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

            BigDecimal cartTotal = cart.getItems().stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Add tax (assume 5%)
            BigDecimal taxAmount = cartTotal.multiply(new BigDecimal("0.05"));

            // Add shipping (assume fixed 50)
            BigDecimal shippingCost = new BigDecimal("50");

            BigDecimal finalTotal = cartTotal.add(taxAmount).add(shippingCost);

            log.debug("Calculated final total for cart {}: {}", cartId, finalTotal);
            return finalTotal.doubleValue();
        } catch (Exception e) {
            log.error("Error calculating final total", e);
            return 0.0;
        }
    }

    @Transactional
    private Order createOrderFromCart(Cart cart) {
        log.debug("Creating order from cart: {}", cart.getId());

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        // Calculate totals
        BigDecimal cartTotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .map(price -> new BigDecimal(price.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = cartTotal.multiply(new BigDecimal("0.05"));
        BigDecimal shippingAmount = new BigDecimal("50");
        BigDecimal finalAmount = cartTotal.add(taxAmount).add(shippingAmount);

        order.setTotalAmount(finalAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingAmount);

        // Create order items from cart items
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal price = new BigDecimal(cartItem.getPrice().toString());
            orderItem.setPrice(price);
            orderItem.setSubtotal(price.multiply(new BigDecimal(cartItem.getQuantity())));
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created from cart. Order ID: {}", savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    private void reserveInventoryFromCart(Cart cart, LocalDateTime expiresAt) {
        log.debug("Reserving inventory for cart: {}", cart.getId());
        for (CartItem item : cart.getItems()) {
            inventoryService.reserveStock(item.getProduct().getId(), item.getQuantity(), expiresAt);
            log.debug("Reserved {} units of product {}", item.getQuantity(), item.getProduct().getId());
        }
    }

    private void releaseInventoryFromCart(Cart cart) {
        log.debug("Releasing inventory reservations for cart: {}", cart.getId());
        for (CartItem item : cart.getItems()) {
            try {
                inventoryService.releaseReservation(item.getProduct().getId(), item.getQuantity());
            } catch (Exception ex) {
                log.error("Failed to release reservation for product {}", item.getProduct().getId(), ex);
            }
        }
    }

    @Transactional
    private void deductInventoryFromCart(Cart cart) {
        log.debug("Permanently deducting inventory for cart: {}", cart.getId());

        for (CartItem cartItem : cart.getItems()) {
            try {
                inventoryService.deductStock(cartItem.getProduct().getId(), cartItem.getQuantity());
                log.debug("Deducted {} units of product {}", cartItem.getQuantity(), cartItem.getProduct().getId());
            } catch (Exception e) {
                log.error("Error deducting inventory for product: {}", cartItem.getProduct().getId(), e);
                throw new RuntimeException("Inventory deduction failed: " + e.getMessage());
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
