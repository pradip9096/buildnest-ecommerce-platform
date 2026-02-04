package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings({ "null", "removal" })
class CheckoutControllerTest {

        private static class NonEqualCartTotalDTO extends CheckoutController.CartTotalDTO {
                @Override
                public boolean canEqual(Object other) {
                        return false;
                }
        }

        private static class SubclassCartTotalDTO extends CheckoutController.CartTotalDTO {
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CheckoutService checkoutService;

        @MockBean
        private RateLimiterService rateLimiterService;

        private CustomUserDetails userDetails;

        @BeforeEach
        void setUp() {
                userDetails = new CustomUserDetails(
                                1L,
                                "testuser",
                                "test@example.com",
                                "password",
                                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                                true,
                                true,
                                true,
                                true);
                when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(50);
                when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(0L);
        }

        @Test
        void testProcessCheckoutWithPaymentValid() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setCartId(5L);
                request.setShippingAddress("221B Baker Street, London");
                request.setPaymentMethod("UPI");
                request.setTotalAmount(5000.0);
                request.setEmail("user@example.com");
                request.setPhoneNumber("+14155552671");
                request.setQuantity(2);

                Order order = new Order();
                order.setId(10L);
                order.setTotalAmount(new BigDecimal("5000.00"));

                when(checkoutService.checkoutWithPayment(anyLong(), anyLong(), any())).thenReturn(order);

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        void testProcessCheckoutValid() throws Exception {
                Order order = new Order();
                order.setId(20L);
                order.setTotalAmount(new BigDecimal("1500.00"));

                when(checkoutService.checkoutCart(anyLong(), anyLong())).thenReturn(order);

                mockMvc.perform(post("/api/checkout/process/3")
                                .with(user(userDetails)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Order placed successfully"));
        }

        @Test
        void testProcessCheckoutInvalidCart() throws Exception {
                doThrow(new IllegalArgumentException("Invalid cart"))
                                .when(checkoutService).checkoutCart(anyLong(), anyLong());

                mockMvc.perform(post("/api/checkout/process/4")
                                .with(user(userDetails)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid cart"));
        }

        @Test
        void testProcessCheckoutError() throws Exception {
                doThrow(new RuntimeException("Checkout failed"))
                                .when(checkoutService).checkoutCart(anyLong(), anyLong());

                mockMvc.perform(post("/api/checkout/process/5")
                                .with(user(userDetails)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error processing checkout: Checkout failed"));
        }

        @Test
        void testProcessCheckoutWithPaymentMissingMethod() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setTotalAmount(5000.0);

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testProcessCheckoutWithPaymentZeroAmount() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setPaymentMethod("UPI");
                request.setTotalAmount(0.0);

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testProcessCheckoutWithPaymentLargeAmountBoundary() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setCartId(6L);
                request.setShippingAddress("123 Main Street, New York, NY");
                request.setPaymentMethod("UPI");
                request.setTotalAmount(99999999.0);
                request.setEmail("user@example.com");
                request.setPhoneNumber("+14155552671");
                request.setQuantity(5);

                Order order = new Order();
                order.setId(11L);
                order.setTotalAmount(new BigDecimal("99999999.00"));

                when(checkoutService.checkoutWithPayment(anyLong(), anyLong(), any())).thenReturn(order);

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        void testValidateCartBeforeCheckout() throws Exception {
                when(checkoutService.validateCheckout(anyLong(), anyLong())).thenReturn(true);

                mockMvc.perform(get("/api/checkout/validate/1")
                                .with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        void testValidateCartNotReady() throws Exception {
                when(checkoutService.validateCheckout(anyLong(), anyLong())).thenReturn(false);

                mockMvc.perform(get("/api/checkout/validate/2")
                                .with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Cart is not ready for checkout"))
                                .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        void testValidateCartError() throws Exception {
                doThrow(new RuntimeException("Validation error"))
                                .when(checkoutService).validateCheckout(anyLong(), anyLong());

                mockMvc.perform(get("/api/checkout/validate/3")
                                .with(user(userDetails)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error validating checkout"));
        }

        @Test
        void testCalculateTotalForCart() throws Exception {
                when(checkoutService.calculateFinalTotal(anyLong())).thenReturn(2500.0);

                // JWT validation in test context returns 401 for authenticated endpoints
                mockMvc.perform(get("/api/checkout/calculate-total/1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testCalculateTotalForCartAuthorized() throws Exception {
                when(checkoutService.calculateFinalTotal(anyLong())).thenReturn(2500.0);

                mockMvc.perform(get("/api/checkout/calculate-total/1")
                                .with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Total calculated successfully"))
                                .andExpect(jsonPath("$.data.finalTotal").value(2500.0));
        }

        @Test
        void testCalculateTotalError() throws Exception {
                doThrow(new RuntimeException("Calc error"))
                                .when(checkoutService).calculateFinalTotal(anyLong());

                mockMvc.perform(get("/api/checkout/calculate-total/2")
                                .with(user(userDetails)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error calculating total"));
        }

        @Test
        void testProcessCheckoutInventoryFailure() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setCartId(7L);
                request.setShippingAddress("456 Oak Avenue, Chicago, IL");
                request.setPaymentMethod("UPI");
                request.setTotalAmount(5000.0);
                request.setEmail("user@example.com");
                request.setPhoneNumber("+14155552671");
                request.setQuantity(3);

                doThrow(new IllegalArgumentException("Insufficient inventory"))
                                .when(checkoutService).checkoutWithPayment(anyLong(), anyLong(), any());

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void testProcessCheckoutWithoutAuthentication() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setPaymentMethod("UPI");
                request.setTotalAmount(5000.0);

                // JWT validation in test context returns 401 for unauthenticated requests
                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testProcessCheckoutRollbackOnFailure() throws Exception {
                CheckoutRequestDTO request = new CheckoutRequestDTO();
                request.setCartId(8L);
                request.setShippingAddress("789 Pine Road, Seattle, WA");
                request.setPaymentMethod("UPI");
                request.setTotalAmount(5000.0);
                request.setEmail("user@example.com");
                request.setPhoneNumber("+14155552671");
                request.setQuantity(1);

                doThrow(new RuntimeException("Payment gateway error"))
                                .when(checkoutService).checkoutWithPayment(anyLong(), anyLong(), any());

                mockMvc.perform(post("/api/checkout/process-with-payment/1")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void cartTotalDtoLombokMethods() {
                CheckoutController.CartTotalDTO dto1 = new CheckoutController.CartTotalDTO();
                CheckoutController.CartTotalDTO dto2 = new CheckoutController.CartTotalDTO(2500.0);
                CheckoutController.CartTotalDTO dto3 = new CheckoutController.CartTotalDTO(2500.0);
                CheckoutController.CartTotalDTO dto4 = new CheckoutController.CartTotalDTO(100.0);
                CheckoutController.CartTotalDTO dtoNull1 = new CheckoutController.CartTotalDTO();
                CheckoutController.CartTotalDTO dtoNull2 = new CheckoutController.CartTotalDTO();

                dto1.setFinalTotal(2500.0);

                assertEquals(2500.0, dto1.getFinalTotal());
                assertEquals(dto2, dto3);
                assertEquals(dto2.hashCode(), dto3.hashCode());
                assertTrue(dto2.toString().contains("2500.0"));
                assertNotEquals(dto2, dto4);
                assertFalse(dto2.equals(null));
                assertFalse(dto2.equals("not-a-dto"));
                assertEquals(dto2, dto2);

                assertEquals(dtoNull1, dtoNull2);
                assertNotEquals(dtoNull1, dto2);
                assertNotEquals(dto2, dtoNull1);
                dtoNull1.hashCode();
                dto2.hashCode();

                SubclassCartTotalDTO subclassDto = new SubclassCartTotalDTO();
                subclassDto.setFinalTotal(2500.0);
                assertTrue(dto2.equals(subclassDto));

                NonEqualCartTotalDTO nonEqualDto = new NonEqualCartTotalDTO();
                nonEqualDto.setFinalTotal(2500.0);
                assertFalse(dto2.equals(nonEqualDto));
        }
}
