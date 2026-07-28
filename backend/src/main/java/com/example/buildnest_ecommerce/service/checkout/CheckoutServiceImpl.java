package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderGroupRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.analytics.UserEventService;
import com.example.buildnest_ecommerce.service.cart.CartService;
import com.example.buildnest_ecommerce.service.coupon.CouponService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final OrderGroupRepository orderGroupRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PaymentService paymentService;
    private final CheckoutSessionStore checkoutSessionStore;
    private final CouponService couponService;
    private final Optional<UserEventService> userEventService;

    // ─── Multi-step checkout (CHK-01, #76) ─────────────────────────────

    @Override
    @Transactional
    public CheckoutSessionDTO setAddress(Long userId, Long addressId) {
        log.info("Checkout step 1/4 — setAddress: user={}, address={}",
                userId, addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found: " + addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Address not found: " + addressId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart is empty — cannot start checkout");
        }

        CheckoutSession session = CheckoutSession.builder()
                .userId(userId)
                .cartId(cart.getId())
                .step(CheckoutStep.PENDING_SHIPPING)
                .addressId(addressId)
                .build();
        checkoutSessionStore.save(userId, session);
        userEventService.ifPresent(
                service -> service.recordCheckoutStarted(userId));

        log.info("Checkout session created for user={}, cartId={}",
                userId, cart.getId());
        return toDTO(session);
    }

    @Override
    @Transactional
    public CheckoutSessionDTO applyCoupon(Long userId, String couponCode) {
        log.info("Applying coupon for user={}, code={}", userId, couponCode);

        Optional<CheckoutSession> opt = checkoutSessionStore.find(userId);
        CheckoutStep curStep = opt.map(CheckoutSession::getStep).orElse(null);
        if (opt.isEmpty() || (curStep != CheckoutStep.PENDING_SHIPPING
                && curStep != CheckoutStep.PENDING_PAYMENT)) {
            String current = opt.map(s -> s.getStep().name()).orElse("NONE");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A coupon can only be applied before payment is "
                            + "initiated; session is " + current);
        }
        CheckoutSession session = opt.get();

        Cart cart = cartRepository.findById(session.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found: " + session.getCartId()));
        BigDecimal subtotal = cartSubtotal(cart);

        Coupon coupon = couponService.validateCoupon(couponCode, subtotal);
        BigDecimal discount = couponService.calculateDiscount(
                coupon, subtotal);

        session.setCouponId(coupon.getId());
        session.setCouponCode(coupon.getCode());
        session.setDiscountAmount(discount);
        checkoutSessionStore.save(userId, session);

        log.info("Coupon {} applied for user={}: discount={}",
                coupon.getCode(), userId, discount);
        return toDTO(session);
    }

    @Override
    @Transactional
    public CheckoutSessionDTO selectShipping(
            Long userId, Long shippingMethodId) {
        log.info("Checkout step 2/4 — selectShipping: user={}, method={}",
                userId, shippingMethodId);

        CheckoutSession session = requireSession(
                userId, CheckoutStep.PENDING_SHIPPING);

        ShippingMethod method = shippingMethodRepository
                .findById(shippingMethodId)
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipping method not found or inactive: "
                                + shippingMethodId));

        session.setShippingMethodId(shippingMethodId);
        session.setShippingCost(method.getBaseCost());
        session.setStep(CheckoutStep.PENDING_PAYMENT);
        checkoutSessionStore.save(userId, session);

        log.info("Shipping selected for user={}: method={}, cost={}",
                userId, method.getName(), method.getBaseCost());
        return toDTO(session);
    }

    private static final int RESERVATION_MINUTES = 15;

    @Override
    @Transactional
    public CheckoutSessionDTO initiatePayment(Long userId) {
        log.info("Checkout step 3/4 — initiatePayment: user={}", userId);

        CheckoutSession session = requireSession(
                userId, CheckoutStep.PENDING_PAYMENT);

        if (!validateCheckout(userId, session.getCartId())) {
            throw new IllegalArgumentException(
                    "Cart is no longer valid for checkout");
        }

        Cart cart = cartRepository.findById(session.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found: " + session.getCartId()));

        // Reserve inventory before creating the order(s). If reservation
        // fails (e.g. concurrent checkout grabbed the last unit), the
        // exception propagates and the cart is untouched.
        LocalDateTime reservationExpiry = LocalDateTime.now()
                .plusMinutes(RESERVATION_MINUTES);
        reserveInventoryFromCart(cart, reservationExpiry);

        // FR-SEL-06 (#579): a cart spanning multiple sellers is split into
        // one Order per seller, linked via OrderGroup (#578's schema). The
        // first order in seller-grouping order is treated as "primary" for
        // this session's single-order fields; payment is a single combined
        // charge against the whole group's total — see buildOrdersFromCart
        // javadoc for the apportionment/trade-off rationale.
        List<Order> orders = buildOrdersFromCart(
                cart, session.getShippingCost(), session.getDiscountAmount());
        List<Order> savedOrders;
        try {
            savedOrders = orderRepository.saveAll(orders);
        } catch (Exception e) {
            // Roll back reservations if the order(s) cannot be persisted
            releaseInventoryFromCart(cart);
            throw e;
        }
        Order primaryOrder = savedOrders.get(0);
        BigDecimal combinedTotal = savedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Payment payment;
        try {
            payment = paymentService.initiatePayment(
                    primaryOrder.getId(), combinedTotal.doubleValue());
        } catch (Exception e) {
            // Payment gateway failure — release reservations so stock is
            // available again
            log.error("Payment initiation failed for user={}, "
                            + "orderId={}; releasing reservations",
                    userId, primaryOrder.getId(), e);
            releaseInventoryFromCart(cart);
            throw e;
        }

        // Only the primary order gets its own Payment row (payment gateway
        // is charged once, against the combined total); sibling orders are
        // linked to the same charge only via the shared OrderGroup, not a
        // per-order razorpayOrderId field (Order has none — only Payment
        // and CheckoutSession track it).
        session.setOrderId(primaryOrder.getId());
        session.setRazorpayOrderId(payment.getRazorpayOrderId());
        session.setStep(CheckoutStep.PENDING_CONFIRM);
        checkoutSessionStore.save(userId, session);

        log.info("Payment initiated for user={}, orderId={}, "
                        + "sellerOrderCount={}",
                userId, primaryOrder.getId(), savedOrders.size());
        return toDTO(session);
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmCheckout(Long userId) {
        log.info("Checkout step 4/4 — confirmCheckout: user={}", userId);

        CheckoutSession session = requireSession(
                userId, CheckoutStep.PENDING_CONFIRM);

        Order primaryOrder = orderRepository.findById(session.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + session.getOrderId()));

        // FR-SEL-06 (#579): confirm every sibling order in the group (if
        // any) as one unit, not just the session's primary order.
        List<Order> ordersToConfirm = primaryOrder.getOrderGroup() != null
                ? orderRepository.findByOrderGroupId(
                        primaryOrder.getOrderGroup().getId())
                : List.of(primaryOrder);

        // Retrieve the cart items to finalise the permanent deduction.
        // The cart was not cleared at initiatePayment — it is cleared
        // here after the order is confirmed.
        Cart cart = cartRepository.findById(session.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found: " + session.getCartId()));

        deductInventoryFromCart(cart);
        cartService.clearCart(userId);

        LocalDateTime now = LocalDateTime.now();
        for (Order order : ordersToConfirm) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setUpdatedAt(now);
        }
        List<Order> confirmedOrders = orderRepository.saveAll(ordersToConfirm);
        Order confirmed = confirmedOrders.stream()
                .filter(o -> o.getId().equals(primaryOrder.getId()))
                .findFirst()
                .orElse(confirmedOrders.get(0));

        // Usage is only consumed on final confirmation, not at apply-time,
        // so an abandoned checkout session never permanently uses up a
        // limited-use code.
        if (session.getCouponId() != null) {
            couponService.incrementUsage(session.getCouponId());
        }

        checkoutSessionStore.delete(userId);

        log.info("Checkout confirmed for user={}, orderId={}, "
                        + "sellerOrderCount={}",
                userId, confirmed.getId(), confirmedOrders.size());
        return toOrderDTO(confirmed);
    }

    private OrderResponseDTO toOrderDTO(Order order) {
        Long sellerId = order.getOrderItems() == null
                || order.getOrderItems().isEmpty() ? null
                        : order.getOrderItems().iterator().next()
                                .getProduct().getSeller().getId();

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getOrderNumber(),
                order.getOrderGroup() != null
                        ? order.getOrderGroup().getId() : null,
                sellerId,
                order.getStatus().toString(),
                order.getTotalAmount(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private CheckoutSession requireSession(
            Long userId, CheckoutStep expectedStep) {
        Optional<CheckoutSession> opt = checkoutSessionStore.find(userId);
        if (opt.isEmpty() || opt.get().getStep() != expectedStep) {
            String current = opt.map(s -> s.getStep().name()).orElse("NONE");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid checkout step. Expected " + expectedStep
                            + " but session is " + current);
        }
        return opt.get();
    }

    private BigDecimal cartSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(this::cartItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal cartItemSubtotal(CartItem cartItem) {
        BigDecimal price = new BigDecimal(cartItem.getPrice().toString());
        return price.multiply(new BigDecimal(cartItem.getQuantity()));
    }

    /**
     * Marker key for cart items whose product has no owning seller
     * (admin-created catalog items, FR-SEL-03) — grouped together as one
     * synthetic "seller" bucket rather than one order per null.
     */
    private static final Long PLATFORM_SELLER_KEY = -1L;

    private Map<Long, List<CartItem>> groupItemsBySeller(Cart cart) {
        Map<Long, List<CartItem>> grouped = new LinkedHashMap<>();
        for (CartItem item : cart.getItems()) {
            User seller = item.getProduct().getSeller();
            Long sellerId = seller != null
                    ? seller.getId() : PLATFORM_SELLER_KEY;
            grouped.computeIfAbsent(sellerId, k -> new ArrayList<>())
                    .add(item);
        }
        return grouped;
    }

    /**
     * Splits a cart into one {@link Order} per distinct seller
     * (FR-SEL-06, #579). A single-seller cart (the common case) produces
     * exactly one order and never creates an {@link OrderGroup} — matching
     * {@link OrderGroup}'s own javadoc contract from #578. When a cart
     * spans more than one seller, shipping and the session-level discount
     * are apportioned across the per-seller orders proportionally to each
     * seller's share of the cart subtotal (a deliberate, documented
     * trade-off — precise seller-negotiated shipping/discount splitting is
     * out of scope for #579, which only covers order-creation splitting;
     * see the sibling seller-scoped order API, #580, for how these are
     * surfaced back to sellers). Tax is then computed per seller on that
     * seller's own discounted subtotal, matching the single-seller
     * behaviour this replaces.
     */
    private List<Order> buildOrdersFromCart(
            Cart cart, BigDecimal shippingCost, BigDecimal discountAmount) {
        Map<Long, List<CartItem>> grouped = groupItemsBySeller(cart);
        BigDecimal cartTotal = cartSubtotal(cart);
        BigDecimal discount = discountAmount != null
                ? discountAmount : BigDecimal.ZERO;
        BigDecimal shipping = shippingCost != null
                ? shippingCost : new BigDecimal("50");

        List<Order> orders = new ArrayList<>();
        for (List<CartItem> items : grouped.values()) {
            orders.add(buildOrderForSellerGroup(
                    cart.getUser(), items, cartTotal, discount, shipping));
        }
        linkOrderGroupIfSplit(cart.getUser(), orders);
        return orders;
    }

    private Order buildOrderForSellerGroup(User user, List<CartItem> items,
            BigDecimal cartTotal, BigDecimal discount, BigDecimal shipping) {
        BigDecimal groupSubtotal = items.stream()
                .map(this::cartItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal share = cartTotal.compareTo(BigDecimal.ZERO) > 0
                ? groupSubtotal.divide(cartTotal, 10, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal groupDiscount = discount.multiply(share);
        BigDecimal groupShipping = shipping.multiply(share);
        // Tax is computed on the discounted subtotal, matching standard
        // order-of-operations (discount applied first, then tax, then
        // shipping added on top).
        BigDecimal discountedSubtotal = groupSubtotal.subtract(groupDiscount)
                .max(BigDecimal.ZERO);
        BigDecimal taxAmount = discountedSubtotal.multiply(
                new BigDecimal("0.05"));
        BigDecimal finalAmount = discountedSubtotal.add(taxAmount)
                .add(groupShipping);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(finalAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(groupShipping);
        order.setDiscountAmount(groupDiscount);

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : items) {
            orderItems.add(buildOrderItem(order, cartItem));
        }
        order.setOrderItems(orderItems);

        return order;
    }

    private OrderItem buildOrderItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(new BigDecimal(cartItem.getPrice().toString()));
        orderItem.setSubtotal(cartItemSubtotal(cartItem));
        return orderItem;
    }

    /**
     * Creates and persists an {@link OrderGroup} linking every order in
     * {@code orders} when the cart was split across more than one seller.
     * A single-order (single-seller) checkout leaves every order's
     * {@code orderGroup} null, per {@link OrderGroup}'s own javadoc.
     */
    private void linkOrderGroupIfSplit(User user, List<Order> orders) {
        if (orders.size() <= 1) {
            return;
        }
        OrderGroup group = new OrderGroup();
        group.setUser(user);
        group.setCreatedAt(LocalDateTime.now());
        OrderGroup savedGroup = orderGroupRepository.save(group);
        for (Order order : orders) {
            order.setOrderGroup(savedGroup);
        }
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
                session.getRazorpayOrderId(),
                session.getCouponCode(),
                session.getDiscountAmount());
    }

    // ─── Legacy single-step checkout ───────────────────────────────────

    @Override
    @Transactional
    public Order checkoutCart(Long userId, Long cartId) {
        log.info("Starting checkout for user: {} with cart: {}",
                userId, cartId);

        // Validate checkout is possible
        if (!validateCheckout(userId, cartId)) {
            throw new IllegalArgumentException(
                    "Cart is not valid for checkout");
        }

        // Get cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found with id: " + cartId));

        // Verify cart belongs to user
        if (!cart.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Cart does not belong to the user");
        }

        // FR-SEL-06 (#579): split into one order per seller; the primary
        // (first) order is returned here for interface-compatibility with
        // existing callers — see #580 for retrieving every sibling order.
        List<Order> orders = createOrdersFromCart(cart);

        // Deduct inventory
        deductInventoryFromCart(cart);

        // Clear cart after successful checkout
        cartService.clearCart(userId);

        Order primary = orders.get(0);
        log.info("Checkout completed for user: {}, Order ID: {}, "
                        + "sellerOrderCount={}",
                userId, primary.getId(), orders.size());
        return primary;
    }

    @Override
    @Transactional
    public Order checkoutWithPayment(
            Long userId, Long cartId, CheckoutRequestDTO request) {
        log.info("Starting checkout with payment for user: {} with "
                + "cart: {}", userId, cartId);

        // Validate checkout
        if (!validateCheckout(userId, cartId)) {
            throw new IllegalArgumentException(
                    "Cart is not valid for checkout");
        }

        // Get cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found"));

        // FR-SEL-06 (#579): split into one order per seller, same as
        // checkoutCart() above.
        List<Order> orders = buildOrdersFromCart(
                cart, new BigDecimal("50"), BigDecimal.ZERO);
        List<Order> savedOrders = orderRepository.saveAll(orders);

        // Deduct inventory
        deductInventoryFromCart(cart);

        // Clear cart
        cartService.clearCart(userId);

        Order primary = savedOrders.get(0);
        log.info("Checkout with payment completed. Order ID: {}, "
                        + "sellerOrderCount={}",
                primary.getId(), savedOrders.size());
        return primary;
    }

    @Override
    public boolean validateCheckout(Long userId, Long cartId) {
        log.debug("Validating checkout for user: {}, cart: {}",
                userId, cartId);

        try {
            // Check cart exists
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cart not found"));

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
                if (!inventoryService.hasStock(
                        item.getProduct().getId(), item.getQuantity())) {
                    log.warn("Insufficient stock for product: {}",
                            item.getProduct().getId());
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
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cart not found"));

            BigDecimal cartTotal = cart.getItems().stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Add tax (assume 5%)
            BigDecimal taxAmount = cartTotal.multiply(
                    new BigDecimal("0.05"));

            // Add shipping (assume fixed 50)
            BigDecimal shippingCost = new BigDecimal("50");

            BigDecimal finalTotal = cartTotal.add(taxAmount)
                    .add(shippingCost);

            log.debug("Calculated final total for cart {}: {}",
                    cartId, finalTotal);
            return finalTotal.doubleValue();
        } catch (Exception e) {
            log.error("Error calculating final total", e);
            return 0.0;
        }
    }

    // No @Transactional here: this private method is only ever called via
    // `this` from an already-@Transactional public method — Spring's proxy
    // never intercepts self-invocation, so an annotation here would be
    // dead code (SonarCloud java:S6809). The outer method's transaction
    // already covers this call.
    private List<Order> createOrdersFromCart(Cart cart) {
        log.debug("Creating order(s) from cart: {}", cart.getId());

        List<Order> orders = buildOrdersFromCart(
                cart, new BigDecimal("50"), BigDecimal.ZERO);
        List<Order> savedOrders = orderRepository.saveAll(orders);
        log.info("Order(s) created from cart. Count={}, primaryId={}",
                savedOrders.size(), savedOrders.get(0).getId());
        return savedOrders;
    }

    // No @Transactional — see createOrdersFromCart's comment above;
    // same self-invocation reasoning applies here.
    private void reserveInventoryFromCart(
            Cart cart, LocalDateTime expiresAt) {
        log.debug("Reserving inventory for cart: {}", cart.getId());
        for (CartItem item : cart.getItems()) {
            inventoryService.reserveStock(
                    item.getProduct().getId(), item.getQuantity(),
                    expiresAt);
            log.debug("Reserved {} units of product {}",
                    item.getQuantity(), item.getProduct().getId());
        }
    }

    private void releaseInventoryFromCart(Cart cart) {
        log.debug("Releasing inventory reservations for cart: {}",
                cart.getId());
        for (CartItem item : cart.getItems()) {
            try {
                inventoryService.releaseReservation(
                        item.getProduct().getId(), item.getQuantity());
            } catch (Exception ex) {
                log.error("Failed to release reservation for product {}",
                        item.getProduct().getId(), ex);
            }
        }
    }

    // No @Transactional — see createOrdersFromCart's comment above;
    // same self-invocation reasoning applies here.
    private void deductInventoryFromCart(Cart cart) {
        log.debug("Permanently deducting inventory for cart: {}",
                cart.getId());

        for (CartItem cartItem : cart.getItems()) {
            try {
                inventoryService.deductStock(
                        cartItem.getProduct().getId(),
                        cartItem.getQuantity());
                log.debug("Deducted {} units of product {}",
                        cartItem.getQuantity(),
                        cartItem.getProduct().getId());
            } catch (Exception e) {
                log.error("Error deducting inventory for product: {}",
                        cartItem.getProduct().getId(), e);
                throw new RuntimeException(
                        "Inventory deduction failed: " + e.getMessage());
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
    }
}
