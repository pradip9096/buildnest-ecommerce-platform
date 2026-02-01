package com.example.buildnest_ecommerce.service.cart;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.CartItem;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;
import com.example.buildnest_ecommerce.repository.CartItemRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(BigDecimal.valueOf(100.00));
        testCart.getItems().add(testCartItem);
    }

    @Test
    void testGetCartByUserId() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act
        CartResponseDTO result = cartService.getCartByUserId(1L);

        // Assert
        assertNotNull(result);
        verify(cartRepository).findByUser(testUser);
    }

    @Test
    void testAddToCart() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Cart result = cartService.addToCart(1L, 1L, 2);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(cartRepository).findByUser(testUser);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testRemoveItemFromCart() {
        // Act
        cartService.removeItemFromCart(1L);

        // Assert
        verify(cartItemRepository).deleteById(1L);
    }

    @Test
    void testClearCart() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act
        cartService.clearCart(1L);

        // Assert
        verify(cartItemRepository).deleteAll(testCart.getItems());
        verify(cartRepository).save(testCart);
    }

    @Test
    void testGetCartTotal() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act
        Double total = cartService.getCartTotal(1L);

        // Assert
        assertNotNull(total);
        assertTrue(total >= 0);
    }

    @Test
    void testAddToCartCreatesCartWhenMissing() {
        Cart newCart = new Cart();
        newCart.setId(99L);
        newCart.setUser(testUser);
        newCart.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Cart result = cartService.addToCart(1L, 1L, 2);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddToCartExistingItemIncrementsQuantity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Cart result = cartService.addToCart(1L, 1L, 3);

        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    void testAddToCartUserNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.addToCart(1L, 1L, 1));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testAddToCartProductNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.addToCart(1L, 1L, 1));
        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void testGetCartByUserIdUserNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.getCartByUserId(1L));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testGetCartByUserIdCartNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.getCartByUserId(1L));
        assertTrue(ex.getMessage().contains("Cart not found"));
    }

    @Test
    void testGetCartByUserIdTotals() {
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());

        Product product1 = new Product();
        product1.setId(11L);
        product1.setName("Item1");
        product1.setPrice(BigDecimal.valueOf(10));

        CartItem item1 = new CartItem();
        item1.setId(100L);
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(10));

        Product product2 = new Product();
        product2.setId(12L);
        product2.setName("Item2");
        product2.setPrice(BigDecimal.valueOf(5));

        CartItem item2 = new CartItem();
        item2.setId(101L);
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(1);
        item2.setPrice(BigDecimal.valueOf(5));

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(cart));

        CartResponseDTO result = cartService.getCartByUserId(1L);

        assertEquals(25.0, result.getTotalAmount());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void testClearCartUserNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.clearCart(1L));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void testClearCartCartNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.clearCart(1L));
        assertTrue(ex.getMessage().contains("Cart not found"));
        verify(cartItemRepository, never()).deleteAll(any());
    }
}
