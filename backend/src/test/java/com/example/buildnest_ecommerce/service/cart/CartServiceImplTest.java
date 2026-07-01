package com.example.buildnest_ecommerce.service.cart;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.CartItem;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CartItemResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;
import com.example.buildnest_ecommerce.repository.CartItemRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        CartResponseDTO result = cartService.getCartByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCartId(), "cartId must be mapped from cart.getId()");
        assertEquals(1L, result.getUserId(), "userId must be mapped from user.getId()");
        assertEquals(1, result.getItems().size(), "items list must contain one item");
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        cartService.clearCart(1L);

        verify(cartItemRepository).deleteAll(any());
        assertTrue(testCart.getItems().isEmpty(), "cart.getItems() must be cleared after clearCart");
        verify(cartRepository).save(testCart);
    }

    @Test
    void testGetCartTotal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Double total = cartService.getCartTotal(1L);

        // testCartItem has quantity=2, price=100.00; getTotalPrice() = 200.0
        assertNotNull(total);
        assertEquals(200.0, total, "getCartTotal must return the cart total amount");
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

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertEquals(testUser, savedCart.getUser(), "new cart must have user set before save");
        assertNotNull(savedCart.getItems(), "new cart must have items list initialised before save");

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

        assertEquals(25.0, result.getTotalAmount(), "totalAmount must be sum of all item totals");
        assertEquals(2, result.getItems().size(), "items list size must match cart items count");

        CartItemResponseDTO dto1 = result.getItems().get(0);
        assertEquals(100L, dto1.getCartItemId(), "cartItemId must be mapped from item.getId()");
        assertEquals(11L, dto1.getProductId(), "productId must be mapped from item.getProduct().getId()");
        assertEquals("Item1", dto1.getProductName(), "productName must be mapped from item.getProduct().getName()");
        assertEquals(2, dto1.getQuantity(), "quantity must be mapped from item.getQuantity()");
        assertEquals(10.0, dto1.getPrice(), "price must be mapped from item.getPrice()");
        assertEquals(20.0, dto1.getItemTotal(), "itemTotal must be item quantity × price");

        CartItemResponseDTO dto2 = result.getItems().get(1);
        assertEquals(101L, dto2.getCartItemId(), "cartItemId must be mapped for second item");
        assertEquals(12L, dto2.getProductId(), "productId must be mapped for second item");
        assertEquals("Item2", dto2.getProductName(), "productName must be mapped for second item");
        assertEquals(1, dto2.getQuantity(), "quantity must be mapped for second item");
        assertEquals(5.0, dto2.getPrice(), "price must be mapped for second item");
        assertEquals(5.0, dto2.getItemTotal(), "itemTotal must be item quantity × price for second item");
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

    @Test
    void testAddToCart_newItem_setsAllFieldsOnCartItem() {
        Cart emptyCart = new Cart();
        emptyCart.setId(55L);
        emptyCart.setUser(testUser);
        emptyCart.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(emptyCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        cartService.addToCart(1L, 1L, 3);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem saved = captor.getValue();
        assertEquals(emptyCart, saved.getCart(), "item.cart must be set to the user's cart");
        assertEquals(testProduct, saved.getProduct(), "item.product must be set to the requested product");
        assertEquals(3, saved.getQuantity(), "item.quantity must equal the requested quantity");
        assertEquals(testProduct.getPrice(), saved.getPrice(), "item.price must be set from product.getPrice()");
    }

    @Test
    void testGetCartByUserIdEmptyCart() {
        Cart emptyCart = new Cart();
        emptyCart.setId(2L);
        emptyCart.setUser(testUser);
        emptyCart.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(emptyCart));

        CartResponseDTO result = cartService.getCartByUserId(1L);

        assertNotNull(result);
        assertEquals(0.0, result.getTotalAmount());
        assertTrue(result.getItems().isEmpty());
    }
}
