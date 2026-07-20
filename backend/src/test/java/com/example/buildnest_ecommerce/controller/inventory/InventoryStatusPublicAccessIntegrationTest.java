package com.example.buildnest_ecommerce.controller.inventory;

import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms InventoryStatusController's GET endpoints are reachable without
 * authentication (#443) -- the controller's own javadoc calls it "Public",
 * but SecurityConfig never actually listed these paths in permitAll() until
 * this issue, meaning every anonymous product-page visitor would have hit a
 * 401. Asserts "not blocked by security" (no 401/403), not a specific
 * success status, since the product id used may or may not exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
class InventoryStatusPublicAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/inventory/{id}/status is reachable without authentication")
    void statusEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/inventory/1/status"))
                .andExpect(status().is(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)))));
    }

    @Test
    @DisplayName("GET /api/inventory/{id}/details is reachable without authentication")
    void detailsEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/inventory/1/details"))
                .andExpect(status().is(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)))));
    }

    @Test
    @DisplayName("GET /api/inventory/{id}/available is reachable without authentication")
    void availableEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/inventory/1/available"))
                .andExpect(status().is(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)))));
    }
}
