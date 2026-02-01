package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.cart.CartService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl tests")
class CheckoutServiceImplTest {

    @Mock
    private CartService cartService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

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
        verify(cartService).clearCart(1L);
        verify(inventoryService).deductStock(5L, 2);
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
        verify(cartService).clearCart(1L);
        verify(inventoryService).deductStock(5L, 2);
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
}
