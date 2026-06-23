package com.example.buildnest_ecommerce.service.wishlist;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Wishlist Service Implementation
 *
 * Manages user wishlist operations including adding/removing products,
 * retrieving wishlist details, and checking product availability.
 * All operations are transaction-aware and ensure data consistency.
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

        private final WishlistRepository wishlistRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;

        /**
         * Adds a product to user's wishlist.
         *
         * Creates a new wishlist if one doesn't exist for the user. If the product
         * is already in the wishlist, it logs a message and returns existing wishlist.
         *
         * @param userId    the unique user identifier - required
         * @param productId the unique product identifier - required
         * @return the Wishlist with the added product
         * @throws ResourceNotFoundException if user or product is not found
         */
        @Override
        @Transactional
        public Wishlist addProduct(Long userId, Long productId) {
                log.info("Adding product {} to wishlist for user {}", productId, userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                Wishlist wishlist = wishlistRepository.findByUserId(userId)
                                .orElseGet(() -> {
                                        Wishlist newWishlist = Wishlist.builder()
                                                        .user(user)
                                                        .build();
                                        return wishlistRepository.save(newWishlist);
                                });

                if (wishlist.containsProduct(product)) {
                        log.info("Product {} already in wishlist for user {}", productId, userId);
                        return wishlist;
                }

                wishlist.addProduct(product);
                wishlistRepository.save(wishlist);

                log.info("Product {} added to wishlist for user {}. Total items: {}",
                                productId, userId, wishlist.getProductCount());

                return wishlist;
        }

        @Override
        @Transactional
        public Wishlist removeProduct(Long userId, Long productId) {
                log.info("Removing product {} from wishlist for user {}", productId, userId);

                Wishlist wishlist = wishlistRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Wishlist not found for user: " + userId));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                wishlist.removeProduct(product);
                wishlistRepository.save(wishlist);

                log.info("Product {} removed from wishlist for user {}. Remaining items: {}",
                                productId, userId, wishlist.getProductCount());

                return wishlist;
        }

        @Override
        public Wishlist getWishlist(Long userId) {
                log.debug("Fetching wishlist for user {}", userId);
                return wishlistRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Wishlist not found for user: " + userId));
        }

        @Override
        public Set<Product> getWishlistProducts(Long userId) {
                log.debug("Fetching wishlist products for user {}", userId);
                Wishlist wishlist = wishlistRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Wishlist not found for user: " + userId));
                return wishlist.getProducts();
        }

        @Override
        public boolean isProductInWishlist(Long userId, Long productId) {
                return wishlistRepository.existsByUserIdAndProductId(userId, productId);
        }

        @Override
        @Transactional
        public void clearWishlist(Long userId) {
                log.info("Clearing wishlist for user {}", userId);
                Wishlist wishlist = wishlistRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Wishlist not found for user: " + userId));
                wishlist.clearProducts();
                wishlistRepository.save(wishlist);
                log.info("Wishlist cleared for user {}", userId);
        }

        @Override
        public long getWishlistCount(Long userId) {
                return wishlistRepository.countProductsByUserId(userId);
        }
}
