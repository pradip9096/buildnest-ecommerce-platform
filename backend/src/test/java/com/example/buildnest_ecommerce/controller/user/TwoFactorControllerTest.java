package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RecoveryCodesResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorSetupResponse;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.auth.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
@SuppressWarnings({ "null", "removal" })
class TwoFactorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TwoFactorService twoFactorService;

    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails userDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
                1L, "testuser", "test@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void enable_returnsSetupResponse() throws Exception {
        when(twoFactorService.generateSecret(testUser))
                .thenReturn(new TwoFactorSetupResponse("SECRET123", "data:image/png;base64,abc"));

        mockMvc.perform(post("/api/user/2fa/enable").with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.secret").value("SECRET123"));
    }

    @Test
    void verify_validCode_returnsRecoveryCodes() throws Exception {
        when(twoFactorService.verifyAndEnable(eq(testUser), eq("123456")))
                .thenReturn(new RecoveryCodesResponse(List.of("A1B2-C3D4")));

        mockMvc.perform(post("/api/user/2fa/verify").with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recoveryCodes[0]").value("A1B2-C3D4"));
    }

    @Test
    void verify_invalidCode_returns400() throws Exception {
        when(twoFactorService.verifyAndEnable(eq(testUser), eq("000000")))
                .thenThrow(new RuntimeException("Invalid TOTP code"));

        mockMvc.perform(post("/api/user/2fa/verify").with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void verify_blankCode_returns400ValidationError() throws Exception {
        mockMvc.perform(post("/api/user/2fa/verify").with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disable_validCode_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/user/2fa/disable").with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void disable_invalidCode_returns400() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Invalid TOTP code"))
                .when(twoFactorService).disable(eq(testUser), eq("000000"));

        mockMvc.perform(post("/api/user/2fa/disable").with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
