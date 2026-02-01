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
    @DisplayName("Should throw when wishlist missing")
    void testMissingWishlistThrows() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.getWishlist(1L));
    }
}
