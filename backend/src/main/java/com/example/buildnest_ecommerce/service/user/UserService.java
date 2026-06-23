package com.example.buildnest_ecommerce.service.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import java.util.List;

public interface UserService {
    User getUserById(Long userId);
    User updateUser(Long userId, User user);
    void deleteUser(Long userId);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    UserResponseDTO getUserResponseById(Long userId);
    UserResponseDTO updateUserProfile(Long userId, UpdateUserDTO updateDTO);
}
