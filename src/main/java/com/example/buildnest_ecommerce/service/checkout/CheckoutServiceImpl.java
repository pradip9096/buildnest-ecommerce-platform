package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.cart.CartService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
// import com.example.buildnest_ecommerce.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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
            orderItem.setPrice(new BigDecimal(cartItem.getPrice().toString()));
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
            orderItem.setPrice(new BigDecimal(cartItem.getPrice().toString()));
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created from cart. Order ID: {}", savedOrder.getId());
        return savedOrder;
    }
    
    @Transactional
    private void deductInventoryFromCart(Cart cart) {
        log.debug("Deducting inventory for cart: {}", cart.getId());
        
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
