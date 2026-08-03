package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.dto.UserDataExportDTO;
import com.example.buildnest_ecommerce.model.dto.UserResponseDTO;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private Authentication auth() {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails details = new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        when(authentication.getPrincipal()).thenReturn(details);
        return authentication;
    }

    @Test
    void getAndUpdateProfile() {
        UserService userService = mock(UserService.class);
        when(userService.getUserResponseById(1L)).thenReturn(new UserResponseDTO());
        when(userService.updateUserProfile(eq(1L), any(UpdateUserDTO.class))).thenReturn(new UserResponseDTO());

        UserController controller = new UserController(userService);
        assertEquals(HttpStatus.OK, controller.getMyProfile(auth()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateProfile(new UpdateUserDTO(), auth()).getStatusCode());
    }

    @Test
    void handlesErrors() {
        UserService userService = mock(UserService.class);
        when(userService.getUserResponseById(1L)).thenThrow(new RuntimeException("not found"));

        UserController controller = new UserController(userService);
        assertEquals(HttpStatus.NOT_FOUND, controller.getMyProfile(auth()).getStatusCode());
    }

    @Test
    void updateProfileHandlesErrors() {
        UserService userService = mock(UserService.class);
        when(userService.updateUserProfile(eq(1L), any(UpdateUserDTO.class)))
                .thenThrow(new RuntimeException("bad"));

        UserController controller = new UserController(userService);
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.updateProfile(new UpdateUserDTO(), auth()).getStatusCode());
    }

    @Test
    void exportMyData_returnsOwnDataOnly() {
        UserService userService = mock(UserService.class);
        UserDataExportDTO export = new UserDataExportDTO(
                null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        when(userService.exportUserData(1L)).thenReturn(export);

        UserController controller = new UserController(userService);
        var response = controller.exportMyData(auth());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).exportUserData(1L);
    }

    @Test
    void exportMyData_handlesErrors() {
        UserService userService = mock(UserService.class);
        when(userService.exportUserData(1L)).thenThrow(new RuntimeException("not found"));

        UserController controller = new UserController(userService);
        assertEquals(HttpStatus.NOT_FOUND, controller.exportMyData(auth()).getStatusCode());
    }

    @Test
    void deleteMyAccount_deletesOwnAccountOnly() {
        UserService userService = mock(UserService.class);

        UserController controller = new UserController(userService);
        var response = controller.deleteMyAccount(auth());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteMyAccount_handlesErrors() {
        UserService userService = mock(UserService.class);
        doThrow(new RuntimeException("bad")).when(userService).deleteUser(1L);

        UserController controller = new UserController(userService);
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteMyAccount(auth()).getStatusCode());
    }
}
