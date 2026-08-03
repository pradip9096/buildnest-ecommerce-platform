package com.example.buildnest_ecommerce.service.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserDataExportDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Address;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductReviewRepository;
import com.example.buildnest_ecommerce.repository.SellerReviewRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.WishlistRepository;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductReviewRepository productReviewRepository;
    @Mock
    private SellerReviewRepository sellerReviewRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should get user by id")
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertEquals(user, userService.getUserById(1L));
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser() {
        User existing = new User();
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        User update = new User();
        update.setEmail("new@example.com");
        update.setFirstName("New");
        update.setLastName("Name");
        update.setPhoneNumber("123");

        User updated = userService.updateUser(1L, update);
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("New", updated.getFirstName(), "firstName must be copied from update object");
        assertEquals("Name", updated.getLastName(), "lastName must be copied from update object");
        assertEquals("123", updated.getPhoneNumber(), "phoneNumber must be copied from update object");
        assertNotNull(updated.getUpdatedAt(), "updatedAt must be set on save");
    }

    @Test
    @DisplayName("Should soft-delete, deactivate, and revoke tokens for the user (#128, COMP-01)")
    void testDeleteUser() {
        User existing = new User();
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.deleteUser(1L);

        assertTrue(existing.getIsDeleted());
        assertFalse(existing.getIsActive(), "isActive must be false so login is blocked immediately");
        assertNotNull(existing.getDeletedAt(), "deletedAt must be set on soft delete");
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    @DisplayName("Should get user by username")
    void testGetUserByUsername() {
        User user = new User();
        user.setUsername("alice");
        user.setIsDeleted(false);
        when(userRepository.findAll()).thenReturn(List.of(user));

        User found = userService.getUserByUsername("alice");
        assertEquals("alice", found.getUsername());
    }

    @Test
    @DisplayName("Should get user by email")
    void testGetUserByEmail() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setIsDeleted(false);
        when(userRepository.findAll()).thenReturn(List.of(user));

        User found = userService.getUserByEmail("a@b.com");
        assertEquals("a@b.com", found.getEmail());
    }

    @Test
    @DisplayName("getUserByUsername does not NPE and finds the user when is_deleted is null (#306)")
    void testGetUserByUsernameNullIsDeletedDoesNotThrow() {
        User user = new User();
        user.setUsername("legacyuser");
        user.setIsDeleted(null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        User found = userService.getUserByUsername("legacyuser");
        assertEquals("legacyuser", found.getUsername());
    }

    @Test
    @DisplayName("getUserByEmail does not NPE and finds the user when is_deleted is null (#306)")
    void testGetUserByEmailNullIsDeletedDoesNotThrow() {
        User user = new User();
        user.setEmail("legacy@b.com");
        user.setIsDeleted(null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        User found = userService.getUserByEmail("legacy@b.com");
        assertEquals("legacy@b.com", found.getEmail());
    }

    @Test
    @DisplayName("Should map response dto")
    void testGetUserResponseById() {
        User user = new User();
        user.setId(2L);
        user.setUsername("bob");
        user.setEmail("bob@b.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserResponseById(2L);
        assertEquals("bob", response.getUsername());
    }

    @Test
    @DisplayName("Should include role names in response dto")
    void testGetUserResponseById_includesRoles() {
        com.example.buildnest_ecommerce.model.entity.Role adminRole =
                new com.example.buildnest_ecommerce.model.entity.Role();
        adminRole.setName("ROLE_ADMIN");
        User user = new User();
        user.setId(4L);
        user.setUsername("admin");
        user.setRoles(java.util.Set.of(adminRole));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserResponseById(4L);

        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());
    }

    @Test
    @DisplayName("Should return empty roles list when user has no roles")
    void testGetUserResponseById_nullRoles_returnsEmptyList() {
        User user = new User();
        user.setId(5L);
        user.setUsername("norole");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserResponseById(5L);

        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should update user profile")
    void testUpdateUserProfile() {
        User user = new User();
        user.setId(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setFirstName("F");
        dto.setLastName("L");
        dto.setEmail("f@l.com");
        dto.setPhone("123");

        UserResponseDTO response = userService.updateUserProfile(3L, dto);
        assertEquals("f@l.com", response.getEmail());
        assertEquals("F", response.getFirstName(), "firstName must be updated in profile");
        assertEquals("L", response.getLastName(), "lastName must be updated in profile");
        assertEquals("123", response.getPhone(), "phone must be updated in profile");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt must be set before save");
    }

    @Test
    @DisplayName("exportUserData aggregates profile, addresses, and orders (#128, COMP-01)")
    void testExportUserData() {
        User user = new User();
        user.setId(6L);
        user.setUsername("exportme");
        user.setEmail("exportme@b.com");
        user.setConsentGiven(true);
        user.setConsentAt(LocalDateTime.now());
        when(userRepository.findById(6L)).thenReturn(Optional.of(user));

        Address address = new Address();
        address.setStreetAddress("123 Main St");
        address.setCity("Metropolis");
        address.setState("State");
        address.setPostalCode("00000");
        address.setCountry("Country");
        address.setAddressType("SHIPPING");
        when(addressRepository.findAllByUser_Id(6L)).thenReturn(List.of(address));

        Order order = new Order();
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        when(orderRepository.findByUserId(6L)).thenReturn(List.of(order));

        when(productReviewRepository.findByUserId(eq(6L), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(sellerReviewRepository.findByUserId(eq(6L), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(wishlistRepository.findByUserIdWithProducts(6L))
                .thenReturn(Optional.empty());
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        UserDataExportDTO export = userService.exportUserData(6L);

        assertEquals("exportme", export.profile().username());
        assertTrue(export.profile().consentGiven());
        assertEquals(1, export.addresses().size());
        assertEquals("123 Main St", export.addresses().get(0).streetAddress());
        assertEquals(1, export.orders().size());
        assertEquals("ORD-1", export.orders().get(0).orderNumber());
        assertTrue(export.wishlistProductNames().isEmpty());
        assertTrue(export.cartItemProductNames().isEmpty());
    }
}
