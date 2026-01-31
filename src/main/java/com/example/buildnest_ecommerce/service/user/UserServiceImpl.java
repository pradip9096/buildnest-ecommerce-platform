package com.example.buildnest_ecommerce.service.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public User getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
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
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
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
                .filter(u -> u.getUsername().equals(username) && !u.getIsDeleted())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email) && !u.getIsDeleted())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public UserResponseDTO getUserResponseById(Long userId) {
        log.info("Fetching user response with id: {}", userId);
        User user = getUserById(userId);
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUserProfile(Long userId, UpdateUserDTO updateDTO) {
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

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                null);
    }
}
