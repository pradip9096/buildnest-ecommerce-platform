package com.example.buildnest_ecommerce.service.wishlist;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.WishlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistServiceImpl tests")
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Test
    @DisplayName("Should add product to wishlist")
    void testAddProduct() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(2L);

        Wishlist wishlist = Wishlist.builder().user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        Wishlist result = wishlistService.addProduct(1L, 2L);
        assertTrue(result.containsProduct(product));
    }

    @Test
    @DisplayName("Should remove product from wishlist")
    void testRemoveProduct() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(2L);

        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlist.addProduct(product);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        Wishlist result = wishlistService.removeProduct(1L, 2L);
        assertFalse(result.containsProduct(product));
    }

    @Test
    @DisplayName("Should get wishlist and products")
    void testGetWishlistProducts() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(2L);

        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlist.addProduct(product);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        assertEquals(wishlist, wishlistService.getWishlist(1L));
        Set<Product> products = wishlistService.getWishlistProducts(1L);
        assertEquals(1, products.size());
    }

    @Test
    @DisplayName("Should check product existence and count")
    void testWishlistChecks() {
        when(wishlistRepository.existsByUserIdAndProductId(1L, 2L)).thenReturn(true);
        when(wishlistRepository.countProductsByUserId(1L)).thenReturn(3L);

        assertTrue(wishlistService.isProductInWishlist(1L, 2L));
        assertEquals(3L, wishlistService.getWishlistCount(1L));
    }

    @Test
    @DisplayName("Should clear wishlist")
    void testClearWishlist() {
        User user = new User();
        user.setId(1L);
        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlist.addProduct(new Product());

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        wishlistService.clearWishlist(1L);
        assertEquals(0, wishlist.getProductCount());
    }

    @Test
    @DisplayName("getWishlist returns an empty wishlist (not a throw) for a brand-new user with no row yet (#303)")
    void testGetWishlistNoRowReturnsEmptyNotThrow() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        Wishlist result = wishlistService.getWishlist(1L);

        assertNotNull(result);
        assertTrue(result.getProducts().isEmpty(), "a brand-new user's wishlist must be empty, not an error");
    }

    @Test
    @DisplayName("Should return existing wishlist when product already present")
    void testAddProductAlreadyInWishlist() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(2L);

        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlist.addProduct(product);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        Wishlist result = wishlistService.addProduct(1L, 2L);

        assertTrue(result.containsProduct(product));
        verify(wishlistRepository, never()).save(wishlist);
    }

    @Test
    @DisplayName("Should create wishlist when none exists for user")
    void testAddProductCreatesWishlistWhenMissing() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(2L);

        Wishlist newWishlist = Wishlist.builder().user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(newWishlist);

        Wishlist result = wishlistService.addProduct(1L, 2L);

        assertNotNull(result);
        verify(wishlistRepository, atLeast(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("Should throw when user not found on addProduct")
    void testAddProductUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.addProduct(99L, 2L));
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw when product not found on addProduct")
    void testAddProductProductNotFound() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.addProduct(1L, 99L));
    }

    @Test
    @DisplayName("Should throw when wishlist not found on removeProduct")
    void testRemoveProductWishlistNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.removeProduct(1L, 2L));
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw when product not found on removeProduct")
    void testRemoveProductProductNotFound() {
        User user = new User();
        user.setId(1L);
        Wishlist wishlist = Wishlist.builder().user(user).build();

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.removeProduct(1L, 99L));
    }

    @Test
    @DisplayName("getWishlistProducts returns an empty set (not a throw) for a brand-new user with no row yet (#303)")
    void testGetWishlistProductsNoRowReturnsEmptySetNotThrow() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        Set<Product> products = wishlistService.getWishlistProducts(1L);

        assertNotNull(products);
        assertTrue(products.isEmpty(), "a brand-new user's wishlist products must be empty, not an error");
    }

    @Test
    @DisplayName("Should throw when wishlist not found on clearWishlist")
    void testClearWishlistNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.clearWishlist(1L));
        verify(wishlistRepository, never()).save(any());
    }
}
