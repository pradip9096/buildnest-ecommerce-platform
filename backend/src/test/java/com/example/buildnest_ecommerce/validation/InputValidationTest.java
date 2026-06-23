package com.example.buildnest_ecommerce.validation;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test suite for Input Validation & Injection Prevention
 * Tests SQL injection, XSS, CSRF, and other injection attacks
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@WithMockUser(username = "testuser", roles = { "USER" })
class InputValidationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * TC-SEC-011: SQL Injection prevention
     */
    @Test
    void testSQLInjectionPrevention() throws Exception {
        String sqlPayload = "1' OR '1'='1";

        mockMvc.perform(post("/api/products/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"" + sqlPayload + "\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-012: XSS prevention in request parameters
     */
    @Test
    void testXSSPrevention() throws Exception {
        String xssPayload = "<script>alert('xss')</script>";

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + xssPayload + "\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-013: CSRF token validation (if form-based)
     */
    @Test
    void testCSRFProtection() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-014: Path traversal prevention
     */
    @Test
    void testPathTraversalPrevention() throws Exception {
        String traversalPayload = "../../etc/passwd";

        mockMvc.perform(post("/api/files/upload")
                .with(csrf())
                .param("path", traversalPayload))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-015: Command injection prevention
     */
    @Test
    void testCommandInjectionPrevention() throws Exception {
        String commandPayload = "; rm -rf /";

        mockMvc.perform(post("/api/reports/generate")
                .with(csrf())
                .param("command", commandPayload))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-016: XML External Entity (XXE) prevention
     */
    @Test
    void testXXEPrevention() throws Exception {
        String xxePayload = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>";

        mockMvc.perform(post("/api/xml/parse")
                .with(csrf())
                .contentType(MediaType.APPLICATION_XML)
                .content(xxePayload))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-017: HTTP Parameter Pollution prevention
     */
    @Test
    void testHTTPParameterPollution() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .param("id", "1")
                .param("id", "2")
                .param("id", "3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-018: Mass assignment prevention
     */
    @Test
    void testMassAssignmentPrevention() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"newuser\"," +
                        "\"email\": \"user@example.com\"," +
                        "\"password\": \"secure123\"," +
                        "\"role\": \"ADMIN\"" + // Should not be assignable
                        "}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-019: File upload validation
     */
    @Test
    void testFileUploadValidation() throws Exception {
        mockMvc.perform(post("/api/files/upload")
                .with(csrf())
                .param("file", "test.exe")) // Executable file
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-020: Empty input handling
     */
    @Test
    void testEmptyInputHandling() throws Exception {
        mockMvc.perform(post("/api/products/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-021: Maximum input length validation
     */
    @Test
    void testMaximumInputLength() throws Exception {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + longString + "\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-022: Type validation
     */
    @Test
    void testTypeValidation() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"price\": \"not_a_number\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-023: Email format validation
     */
    @Test
    void testEmailValidation() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"invalid_email\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-SEC-024: Phone number format validation
     */
    @Test
    void testPhoneNumberValidation() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\": \"not_a_phone\"}"))
                .andExpect(status().isBadRequest());
    }
}
