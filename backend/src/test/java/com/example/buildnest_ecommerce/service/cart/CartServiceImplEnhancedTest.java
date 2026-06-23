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
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("CartServiceImpl mutation-killing tests")
class CartServiceImplEnhancedTest {

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
    }

    @Test
    @DisplayName("Should set all CartItem fields when adding new item to cart")
    void testAddToCartNewItemAllFieldsSet() {
        Cart newCart = new Cart();
        newCart.setId(2L);
        newCart.setUser(testUser);
        newCart.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(newCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.addToCart(1L, 1L, 5);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem saved = captor.getValue();

        assertEquals(newCart, saved.getCart());
        assertEquals(testProduct, saved.getProduct());
        assertEquals(5, saved.getQuantity());
        assertEquals(testProduct.getPrice(), saved.getPrice());
    }

    @Test
    @DisplayName("Should increment quantity when adding existing item to cart")
    void testAddToCartIncrementsBoundary() {
        testCart.getItems().clear();
        CartItem item = new CartItem();
        item.setQuantity(1);
        item.setProduct(testProduct);
        testCart.getItems().add(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        cartService.addToCart(1L, 1L, 1);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem saved = captor.getValue();

        assertEquals(2, saved.getQuantity());
    }

    @Test
    @DisplayName("Should calculate item total correctly in cart response")
    void testGetCartByUserIdItemTotalCalculation() {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(50L);
        item.setProduct(testProduct);
        item.setQuantity(3);
        item.setPrice(BigDecimal.valueOf(25));
        cart.getItems().add(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(cart));

        CartResponseDTO response = cartService.getCartByUserId(1L);

        assertEquals(1, response.getItems().size());
        assertEquals(75.0, response.getItems().get(0).getItemTotal());
        assertEquals(75.0, response.getTotalAmount());
    }

    @Test
    @DisplayName("Should accumulate multiple cart items correctly")
    void testGetCartByUserIdMultipleItemsAccumulation() {
        Cart cart = new Cart();
        cart.setId(6L);
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());

        CartItem item1 = new CartItem();
        item1.setId(51L);
        item1.setProduct(testProduct);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(50));
        cart.getItems().add(item1);

        CartItem item2 = new CartItem();
        item2.setId(52L);
        Product product2 = new Product();
        product2.setId(2L);
        item2.setProduct(product2);
        item2.setQuantity(3);
        item2.setPrice(BigDecimal.valueOf(30));
        cart.getItems().add(item2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(cart));

        CartResponseDTO response = cartService.getCartByUserId(1L);

        assertEquals(190.0, response.getTotalAmount());
        assertEquals(2, response.getItems().size());
    }

    @Test
    @DisplayName("Should initialize cart total with BigDecimal.ZERO")
    void testGetCartByUserIdEmptyCart() {
        Cart emptyCart = new Cart();
        emptyCart.setId(7L);
        emptyCart.setUser(testUser);
        emptyCart.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(emptyCart));

        CartResponseDTO response = cartService.getCartByUserId(1L);

        assertEquals(0.0, response.getTotalAmount());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should create new cart when none exists")
    void testAddToCartCartNullCheck() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addToCart(1L, 1L, 1);

        assertNotNull(result);
        assertNotNull(result.getItems());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should call deleteById for item removal")
    void testRemoveItemFromCartCallsDelete() {
        cartService.removeItemFromCart(42L);

        verify(cartItemRepository, times(1)).deleteById(42L);
    }

    @Test
    @DisplayName("Should get cart total by calling getCartByUserId")
    void testGetCartTotalCallsGetCartByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Double total = cartService.getCartTotal(1L);

        assertNotNull(total);
        verify(cartRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Should set user on new cart")
    void testAddToCartSetsUserOnNewCart() {
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.addToCart(1L, 1L, 1);

        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertEquals(testUser, savedCart.getUser());
    }

    @Test
    @DisplayName("Should initialize items list on new cart")
    void testAddToCartInitializeItemsList() {
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.addToCart(1L, 1L, 1);

        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertNotNull(savedCart.getItems());
    }

    @Test
    @DisplayName("Should add item to cart items list")
    void testAddToCartAddsItemToList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.addToCart(1L, 1L, 5);

        assertEquals(1, testCart.getItems().size());
    }

    @Test
    @DisplayName("Should clear cart items list")
    void testClearCartClearsItemsList() {
        testCart.getItems().add(new CartItem());
        testCart.getItems().add(new CartItem());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        cartService.clearCart(1L);

        assertTrue(testCart.getItems().isEmpty());
    }
}
