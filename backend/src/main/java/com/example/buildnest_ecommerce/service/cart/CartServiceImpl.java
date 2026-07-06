package com.example.buildnest_ecommerce.service.cart;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.CartItem;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CartItemResponseDTO;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.CartItemRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductVariantRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import java.util.Objects;
import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        return addToCart(userId, productId, null, quantity);
    }

    @Override
    @Transactional
    public Cart addToCart(Long userId, Long productId, Long variantId, Integer quantity) {
        log.info("Adding product {} (variant {}) to cart for user {}", productId, variantId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + variantId));
            if (!variant.getProduct().getId().equals(productId)) {
                throw new RuntimeException("Variant " + variantId + " does not belong to product " + productId);
            }
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart = cartRepository.save(cart);
        }

        // Check if this exact product+variant combination already exists in cart
        final Long finalVariantId = variantId;
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId)
                        && Objects.equals(item.getVariant() != null ? item.getVariant().getId() : null, finalVariantId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(quantity);
            item.setPrice(variant != null ? variant.getEffectivePrice()
                    : (product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice()));
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        log.info("Product added to cart successfully");
        return cart;
    }

    @Override
    public CartResponseDTO getCartByUserId(Long userId) {
        log.info("Fetching cart for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            log.debug("No cart row yet for user {}; returning empty cart", userId);
            CartResponseDTO emptyResponse = new CartResponseDTO();
            emptyResponse.setCartId(null);
            emptyResponse.setUserId(user.getId());
            emptyResponse.setItems(new ArrayList<>());
            emptyResponse.setTotalAmount(0.0);
            return emptyResponse;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItemResponseDTO> itemDTOs = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            CartItemResponseDTO dto = new CartItemResponseDTO();
            dto.setCartItemId(item.getId());
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            if (item.getVariant() != null) {
                dto.setVariantId(item.getVariant().getId());
                dto.setVariantSku(item.getVariant().getSku());
            }
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getPrice().doubleValue());

            BigDecimal itemTotal = item.getTotalPrice();
            dto.setItemTotal(itemTotal.doubleValue());
            totalAmount = totalAmount.add(itemTotal);
            itemDTOs.add(dto);
        }

        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());
        response.setUserId(user.getId());
        response.setItems(itemDTOs);
        response.setTotalAmount(totalAmount.doubleValue());

        return response;
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long cartItemId, Long requestingUserId) {
        log.info("Removing item {} from cart", cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Cart item does not belong to the requesting user");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public Double getCartTotal(Long userId) {
        log.info("Getting cart total for user: {}", userId);
        return getCartByUserId(userId).getTotalAmount();
    }
}
