package com.example.buildnest_ecommerce.service.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

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
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        User existing = new User();
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.deleteUser(1L);
        assertTrue(existing.getIsDeleted());
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
    }
}
