package com.example.buildnest_ecommerce.service.notification;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that the four email Thymeleaf templates render
 * correctly with expected variable substitution (NOTIF-01, #62).
 *
 * Uses {@link SpringTemplateEngine} directly — no Spring context required.
 */
@DisplayName("Email template rendering tests")
class EmailTemplateRenderingTest {

    private static SpringTemplateEngine engine;

    @BeforeAll
    static void setUpEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Test
    @DisplayName("TC-TMPL-01: order-confirmation template renders order details")
    void orderConfirmationTemplate_rendersOrderDetails() {
        Context ctx = new Context();
        ctx.setVariable("customerName", "Ravi Kumar");
        ctx.setVariable("orderNumber", "ORD-1001");
        ctx.setVariable("orderDate", "29 Jun 2026");
        ctx.setVariable("totalAmount", "2500.00");
        ctx.setVariable("shippingAddress", "42 MG Road, Pune, 411001");

        String html = engine.process("email/order-confirmation", ctx);

        assertThat(html).contains("Order Confirmed");
        assertThat(html).contains("Ravi Kumar");
        assertThat(html).contains("ORD-1001");
        assertThat(html).contains("₹2500.00");
        assertThat(html).contains("42 MG Road");
    }

    @Test
    @DisplayName("TC-TMPL-02: shipping-update template renders tracking details")
    void shippingUpdateTemplate_rendersTrackingDetails() {
        Context ctx = new Context();
        ctx.setVariable("customerName", "Priya Sharma");
        ctx.setVariable("orderNumber", "ORD-1002");
        ctx.setVariable("trackingNumber", "TRK-9988776655");
        ctx.setVariable("estimatedDelivery", "3–5 business days");

        String html = engine.process("email/shipping-update", ctx);

        assertThat(html).contains("Your Order Has Shipped");
        assertThat(html).contains("Priya Sharma");
        assertThat(html).contains("ORD-1002");
        assertThat(html).contains("TRK-9988776655");
        assertThat(html).contains("3–5 business days");
    }

    @Test
    @DisplayName("TC-TMPL-03: password-reset template renders reset URL")
    void passwordResetTemplate_rendersResetUrl() {
        Context ctx = new Context();
        ctx.setVariable("email", "user@example.com");
        ctx.setVariable("resetUrl", "https://buildnest.com/reset-password?token=abc123");
        ctx.setVariable("expiryMinutes", 60);

        String html = engine.process("email/password-reset", ctx);

        assertThat(html).contains("Password Reset Request");
        assertThat(html).contains("user@example.com");
        assertThat(html).contains("https://buildnest.com/reset-password?token=abc123");
        assertThat(html).contains("60");
    }

    @Test
    @DisplayName("TC-TMPL-04: registration-welcome template renders verification URL")
    void registrationWelcomeTemplate_rendersVerificationUrl() {
        Context ctx = new Context();
        ctx.setVariable("customerName", "Amit Patel");
        ctx.setVariable("verificationUrl", "https://buildnest.com/verify?token=xyz789");

        String html = engine.process("email/registration-welcome", ctx);

        assertThat(html).contains("Welcome to BuildNest");
        assertThat(html).contains("Amit Patel");
        assertThat(html).contains("https://buildnest.com/verify?token=xyz789");
        assertThat(html).contains("Verify Email Address");
    }
}
