package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        service = new AdminServiceImpl(userRepository, orderRepository, productRepository);
    }

    @Test
    void getAllUsers_filtersDeletedUsers_returnsOnlyActiveUser() {
        User active = new User();
        active.setId(1L);
        active.setUsername("active_user");
        active.setIsDeleted(false);

        User deleted = new User();
        deleted.setId(2L);
        deleted.setIsDeleted(true);

        when(userRepository.findAll()).thenReturn(List.of(active, deleted));

        List<AdminUserDto> users = service.getAllUsers();

        assertEquals(1, users.size(), "only non-deleted users must be returned");
        assertEquals(1L, users.get(0).getId(), "active user id must be preserved — filter negation must not pass deleted user");
    }

    @Test
    void getUserById_found_returnsMappedDto() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 0);
        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setId(42L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("+441234567890");
        user.setIsActive(true);
        user.setCreatedAt(createdAt);
        user.setRoles(Set.of(role));

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        AdminUserDto dto = service.getUserById(42L);

        assertEquals(42L, dto.getId(), "id must be mapped");
        assertEquals("john", dto.getUsername(), "username must be mapped");
        assertEquals("john@example.com", dto.getEmail(), "email must be mapped");
        assertEquals("John", dto.getFirstName(), "firstName must be mapped");
        assertEquals("Doe", dto.getLastName(), "lastName must be mapped");
        assertEquals("+441234567890", dto.getPhoneNumber(), "phoneNumber must be mapped");
        assertTrue(dto.getIsActive(), "isActive must be mapped");
        assertEquals(createdAt, dto.getCreatedAt(), "createdAt must be mapped");
        assertTrue(dto.getRoles().contains("USER"), "role name must be mapped");
    }

    @Test
    void getUserById_notFound_throwsRuntimeExceptionWithMessage() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getUserById(99L));
        assertEquals("User not found", ex.getMessage(), "exception message must identify the cause");
    }

    @Test
    void updateUser_updatesAllFieldsAndReturnsDto() {
        User existing = new User();
        existing.setId(1L);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setIsDeleted(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User patch = new User();
        patch.setEmail("updated@example.com");
        patch.setFirstName("Updated");
        patch.setLastName("Name");

        AdminUserDto dto = service.updateUser(1L, patch);

        assertEquals("updated@example.com", dto.getEmail(), "email must be updated");
        assertEquals("Updated", dto.getFirstName(), "firstName must be updated");
        assertEquals("Name", dto.getLastName(), "lastName must be updated");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt must be set before save");
    }

    @Test
    void updateUser_notFound_throwsRuntimeExceptionWithMessage() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateUser(99L, new User()));
        assertEquals("User not found", ex.getMessage(), "exception message must identify the cause");
    }

    @Test
    void updateUserByAdmin_updatesAllFieldsAndReturnsDto() {
        User existing = new User();
        existing.setId(1L);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setIsDeleted(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserDTO updateDTO = new UpdateUserDTO("First", "Last", "email@example.com", "+123456789", "Address");
        AdminUserDto dto = service.updateUserByAdmin(1L, updateDTO);

        assertEquals("email@example.com", dto.getEmail(), "email must be updated");
        assertEquals("First", dto.getFirstName(), "firstName must be updated");
        assertEquals("Last", dto.getLastName(), "lastName must be updated");
        assertEquals("+123456789", dto.getPhoneNumber(), "phoneNumber must be updated");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt must be set before save");
    }

    @Test
    void updateUserByAdmin_notFound_throwsRuntimeExceptionWithMessage() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.updateUserByAdmin(99L, new UpdateUserDTO("A", "B", "a@b.com", "+1234567890", "10 Main St")));
        assertEquals("User not found", ex.getMessage(), "exception message must identify the cause");
    }

    @Test
    void deleteUser_setsIsDeletedTrueAndDeletedAt() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteUser(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.getIsDeleted(), "isDeleted must be set to true");
        assertNotNull(saved.getDeletedAt(), "deletedAt must be set");
    }

    @Test
    void deleteUser_notFound_throwsRuntimeExceptionWithMessage() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteUser(99L));
        assertEquals("User not found", ex.getMessage(), "exception message must identify the cause");
    }

    @Test
    void getTotalUsers_countsOnlyNonDeletedUsers() {
        User active = new User();
        active.setIsDeleted(false);
        User deleted = new User();
        deleted.setIsDeleted(true);

        when(userRepository.findAll()).thenReturn(List.of(active, deleted));

        assertEquals(1L, service.getTotalUsers(), "only non-deleted users must be counted");
    }

    @Test
    void getTotalUsers_nullIsDeleted_doesNotThrowAndCountsAsNotDeleted() {
        // Regression test for #306: a legacy row with is_deleted = NULL must not NPE on
        // !user.getIsDeleted() auto-unboxing; a null flag is treated as "not deleted".
        User nullFlagUser = new User();
        nullFlagUser.setIsDeleted(null);

        when(userRepository.findAll()).thenReturn(List.of(nullFlagUser));

        assertEquals(1L, service.getTotalUsers(), "a null is_deleted flag must count as not-deleted, not throw");
    }

    @Test
    void getAllUsers_nullIsDeleted_doesNotThrowAndIncludesUser() {
        User nullFlagUser = new User();
        nullFlagUser.setId(1L);
        nullFlagUser.setIsDeleted(null);

        when(userRepository.findAll()).thenReturn(List.of(nullFlagUser));

        List<AdminUserDto> users = service.getAllUsers();

        assertEquals(1, users.size(), "a null is_deleted flag must not exclude the user or throw");
    }

    @Test
    void getTotalProducts_delegatesToRepository() {
        when(productRepository.count()).thenReturn(7L);

        assertEquals(7L, service.getTotalProducts(), "product count must come from repository");
    }

    @Test
    void getTotalOrders_countsOnlyNonDeletedOrders() {
        Order activeOrder = new Order();
        activeOrder.setIsDeleted(false);
        Order deletedOrder = new Order();
        deletedOrder.setIsDeleted(true);

        when(orderRepository.findAll()).thenReturn(List.of(activeOrder, deletedOrder));

        assertEquals(1L, service.getTotalOrders(), "only non-deleted orders must be counted");
    }

    @Test
    void getTotalOrders_nullIsDeleted_doesNotThrowAndCountsAsNotDeleted() {
        Order nullFlagOrder = new Order();
        nullFlagOrder.setIsDeleted(null);

        when(orderRepository.findAll()).thenReturn(List.of(nullFlagOrder));

        assertEquals(1L, service.getTotalOrders(), "a null is_deleted flag must count as not-deleted, not throw");
    }

    @Test
    void getTotalRevenue_returnsZeroPlaceholder() {
        assertEquals(0.0, service.getTotalRevenue(), "revenue placeholder must be 0.0");
    }

    @Test
    void convertToDto_nullRoles_doesNotPopulateRoles() {
        User user = new User();
        user.setId(1L);
        user.setRoles(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDto dto = service.getUserById(1L);
        assertNull(dto.getRoles(), "roles must be null when user has no roles");
    }
}
