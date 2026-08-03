package com.example.buildnest_ecommerce.service.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserDataExportDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Address;
import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.ProductReview;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.SellerReview;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductReviewRepository;
import com.example.buildnest_ecommerce.repository.SellerReviewRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.WishlistRepository;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public User getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId));
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User user) {
        log.info("Updating user with id: {}", userId);
        User existingUser = getUserById(userId);
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Soft deleting user with id: {}", userId);
        User user = getUserById(userId);
        user.setIsDeleted(true);
        user.setIsActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        // Immediately invalidates the account for future auth --
        // CustomUserDetailsService.loadUserByUsername already wires
        // isActive into UserDetails#isEnabled().
        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Fetching all non-deleted users");
        return userRepository.findAll();
    }

    @Override
    public User getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username)
                        && !Boolean.TRUE.equals(u.getIsDeleted()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email)
                        && !Boolean.TRUE.equals(u.getIsDeleted()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + email));
    }

    @Override
    public UserResponseDTO getUserResponseById(Long userId) {
        log.info("Fetching user response with id: {}", userId);
        User user = getUserById(userId);
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUserProfile(
            Long userId, UpdateUserDTO updateDTO) {
        log.info("Updating user profile with id: {}", userId);
        User existingUser = getUserById(userId);
        existingUser.setFirstName(updateDTO.getFirstName());
        existingUser.setLastName(updateDTO.getLastName());
        existingUser.setEmail(updateDTO.getEmail());
        existingUser.setPhoneNumber(updateDTO.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(existingUser);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    public UserDataExportDTO exportUserData(Long userId) {
        log.info("Exporting GDPR data for user id: {}", userId);
        User user = getUserById(userId);

        var profile = new UserDataExportDTO.Profile(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(),
                user.getPhoneNumber(), user.getCreatedAt(),
                user.getLastLogin(), user.getConsentGiven(),
                user.getConsentAt());

        List<UserDataExportDTO.AddressRow> addresses =
                addressRepository.findAllByUser_Id(userId).stream()
                        .map(this::toAddressRow)
                        .toList();

        List<UserDataExportDTO.OrderRow> orders =
                orderRepository.findByUserId(userId).stream()
                        .map(this::toOrderRow)
                        .toList();

        List<UserDataExportDTO.ProductReviewRow> productReviews =
                productReviewRepository
                        .findByUserId(userId, Pageable.unpaged())
                        .stream()
                        .map(this::toProductReviewRow)
                        .toList();

        List<UserDataExportDTO.SellerReviewRow> sellerReviews =
                sellerReviewRepository
                        .findByUserId(userId, Pageable.unpaged())
                        .stream()
                        .map(this::toSellerReviewRow)
                        .toList();

        List<String> wishlistProductNames = wishlistRepository
                .findByUserIdWithProducts(userId)
                .map(Wishlist::getProducts)
                .map(products -> products.stream()
                        .map(com.example.buildnest_ecommerce.model.entity
                                .Product::getName)
                        .toList())
                .orElse(List.of());

        List<String> cartItemProductNames = cartRepository
                .findByUser(user)
                .map(Cart::getItems)
                .map(items -> items.stream()
                        .map(item -> item.getProduct().getName())
                        .toList())
                .orElse(List.of());

        return new UserDataExportDTO(profile, addresses, orders,
                productReviews, sellerReviews, wishlistProductNames,
                cartItemProductNames);
    }

    private UserDataExportDTO.AddressRow toAddressRow(Address a) {
        return new UserDataExportDTO.AddressRow(
                a.getStreetAddress(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.getAddressType());
    }

    private UserDataExportDTO.OrderRow toOrderRow(Order o) {
        return new UserDataExportDTO.OrderRow(
                o.getOrderNumber(), o.getStatus().name(),
                o.getTotalAmount(), o.getCreatedAt());
    }

    private UserDataExportDTO.ProductReviewRow toProductReviewRow(
            ProductReview r) {
        return new UserDataExportDTO.ProductReviewRow(
                r.getProduct().getId(), r.getRating(), r.getComment(),
                r.getCreatedAt());
    }

    private UserDataExportDTO.SellerReviewRow toSellerReviewRow(
            SellerReview r) {
        return new UserDataExportDTO.SellerReviewRow(
                r.getSeller().getId(), r.getRating(), r.getComment(),
                r.getCreatedAt());
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        List<String> roleNames = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                null,
                roleNames);
    }
}
