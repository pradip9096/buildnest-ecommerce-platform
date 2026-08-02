package com.example.buildnest_ecommerce.loadtest;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling load test simulation for BuildNest E-Commerce platform.
 * Tests TC-LOAD-001 through TC-LOAD-004.
 * 
 * Run with: mvn gatling:test
 * -Dgatling.simulationClass=com.example.buildnest_ecommerce.loadtest.LoadTestSimulation
 * 
 * Test Scenarios:
 * - Ramp-up load testing
 * - Sustained load testing
 * - Peak load testing
 * - Stress testing
 */
public class LoadTestSimulation extends Simulation {

        // Configuration
        private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");
        private static final int USERS_RAMP = Integer.parseInt(System.getProperty("users.ramp", "50"));
        private static final Duration RAMP_DURATION = Duration.ofSeconds(30);

        // HTTP Protocol Configuration
        HttpProtocolBuilder httpProtocol = http
                        .baseUrl(BASE_URL)
                        .acceptHeader("application/json")
                        .acceptEncodingHeader("gzip, deflate")
                        .userAgentHeader("Gatling Load Test");

        // Scenario: Browse Products (TC-LOAD-001)
        // #631: /api/products and /api/products/{id} don't exist in this codebase — the real
        // unauthenticated product-browsing routes are under /api/public/products (HomeController).
        ChainBuilder browseProductsChain = exec(
                        http("Get Products List")
                                        .get("/api/public/products")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(2))
                        .exec(
                                        http("Get Product Details")
                                                        .get("/api/public/products/1")
                                                        .check(status().in(200, 404)))
                        .pause(Duration.ofSeconds(1));

        ScenarioBuilder browseProductsScenario = scenario("Browse Products")
                        .exec(browseProductsChain);

        // Scenario: Search Products (TC-LOAD-002)
        // #631: query param is `keyword`, not `q` (see HomeController#searchProducts).
        ChainBuilder searchProductsChain = exec(
                        http("Search Products")
                                        .get("/api/public/products/search?keyword=cement")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(2));

        ScenarioBuilder searchProductsScenario = scenario("Search Products")
                        .exec(searchProductsChain);

        // Scenario: Authentication Flow (TC-LOAD-003)
        // #631: LoginRequest's field is `username` (see LoginRequest.java), not `email`; an
        // invalid-credentials login returns 400 (AuthController), not 401 — verified locally.
        ChainBuilder authenticationChain = exec(
                        http("Login Request")
                                        .post("/api/auth/login")
                                        .header("Content-Type", "application/json")
                                        .body(StringBody("""
                                                        {
                                                            "username": "test@example.com",
                                                            "password": "password123"
                                                        }
                                                        """))
                                        .check(status().in(200, 400)))
                        .pause(Duration.ofSeconds(3));

        ScenarioBuilder authenticationScenario = scenario("User Authentication")
                        .exec(authenticationChain);

        // Scenario: Add to Cart (TC-LOAD-004)
        // #631: real route is /api/user/cart/add (see CartController); /api/cart/items doesn't
        // exist. An unauthenticated request here is rejected by @PreAuthorize (anonymous
        // principal fails the role/ownership check) as 403, not the 401 an AuthenticationEntryPoint
        // would return — verified locally against the real filter chain.
        // #118: addToCart also requires `userId` as a @RequestParam (CartController#addToCart) —
        // the prior version omitted it entirely, which Spring rejects with 400 before @PreAuthorize
        // even runs; added here so the request reaches the same auth-rejection path as every other
        // authenticated chain in this file.
        // #118: extracted as a shared step (reused by checkoutChain below) so the two chains
        // can't drift out of sync on the same endpoint's request shape / expected-status list —
        // a java-reviewer pass on this change flagged the pre-extraction duplication as exactly
        // that drift risk.
        ChainBuilder addToCartStep = exec(
                        http("Add to Cart")
                                        .post("/api/user/cart/add?userId=1")
                                        .header("Content-Type", "application/json")
                                        .body(StringBody("""
                                                        {
                                                            "productId": 1,
                                                            "quantity": 1
                                                        }
                                                        """))
                                        .check(status().in(200, 201, 403)));

        ChainBuilder addToCartChain = exec(
                        http("View Products")
                                        .get("/api/public/products")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(1))
                        .exec(addToCartStep)
                        .pause(Duration.ofSeconds(2));

        ScenarioBuilder addToCartScenario = scenario("Add to Cart")
                        .exec(addToCartChain);

        // Scenario: Checkout Flow (TC-LOAD-005, #118)
        // Mirrors addToCartChain's tolerant-status convention: no seed data exists in CI's
        // fresh H2 schema (create-drop, empty), so every authenticated call here is expected to
        // be rejected rather than succeed — same reasoning already applied to
        // authenticationChain/addToCartChain. The point of this scenario is exercising the
        // checkout endpoints' latency under load, not asserting business-logic success against
        // data that doesn't exist in this environment.
        // Verified locally (#118): /api/checkout/** is NOT covered by SecurityConfig's
        // /api/user/** ROLE_USER rule (see spring-security.md's URL Authorization Rules), so an
        // anonymous request falls through to the catch-all anyRequest().authenticated() and gets
        // 401 (AuthenticationEntryPoint) — unlike /api/user/cart/add, which matches /api/user/**
        // and gets 403 (AccessDeniedHandler) via @PreAuthorize's ownership check instead.
        ChainBuilder checkoutChain = exec(
                        http("View Products")
                                        .get("/api/public/products")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(1))
                        .exec(addToCartStep)
                        .pause(Duration.ofSeconds(1))
                        .exec(
                                        http("Calculate Checkout Total")
                                                        .get("/api/checkout/calculate-total/1")
                                                        .check(status().in(200, 401, 404)))
                        .pause(Duration.ofSeconds(1))
                        .exec(
                                        http("Process Checkout")
                                                        .post("/api/checkout/process/1")
                                                        .header("Content-Type", "application/json")
                                                        .body(StringBody("""
                                                                        {
                                                                            "shippingAddressId": 1,
                                                                            "paymentMethod": "COD"
                                                                        }
                                                                        """))
                                                        .check(status().in(200, 201, 400, 403, 404)))
                        .pause(Duration.ofSeconds(2));

        ScenarioBuilder checkoutScenario = scenario("Checkout Flow")
                        .exec(checkoutChain);

        // Test 1: Ramp-Up Load Test
        // Gradually increase load to identify breaking point.
        // #118: added checkoutScenario alongside browseProductsScenario so the checkout path
        // (TC-LOAD-005) is exercised by the same CI-run setUp(), and added a global P95
        // assertion matching RTM PR-01 (P95 < 500ms). USERS_RAMP stays at its existing CI-safe
        // default (50, overridable via -Dusers.ramp) — PR-01's own 1,000-user target is meant to
        // be run against a real staging environment (see docs/SDLC-docs/reports/load-test-results.md),
        // not the in-memory H2 instance this CI job boots.
        {
                setUp(
                                browseProductsScenario.injectOpen(
                                                rampUsers(USERS_RAMP).during(RAMP_DURATION)),
                                checkoutScenario.injectOpen(
                                                rampUsers(USERS_RAMP).during(RAMP_DURATION)))
                                .protocols(httpProtocol)
                                .assertions(
                                                global().responseTime().max().lt(5000),
                                                global().responseTime().percentile(95.0).lt(500),
                                                global().successfulRequests().percent().gt(95.0));

                /*
                 * Uncomment to run other scenarios individually:
                 * 
                 * // Test 2: Sustained Load Test
                 * // Maintain constant load over time
                 * setUp(
                 * mixedUserScenario.injectOpen(
                 * constantUsersPerSec(USERS_SUSTAINED).during(TEST_DURATION)
                 * ).protocols(httpProtocol)
                 * ).assertions(
                 * global().responseTime().mean().lt(2000),
                 * global().successfulRequests().percent().gt(98.0)
                 * );
                 * 
                 * // Test 3: Peak Load Test
                 * // Simulate traffic spike
                 * setUp(
                 * mixedUserScenario.injectOpen(
                 * rampUsers(USERS_PEAK).during(Duration.ofSeconds(10)),
                 * constantUsersPerSec(USERS_PEAK).during(Duration.ofSeconds(30))
                 * ).protocols(httpProtocol)
                 * ).assertions(
                 * global().responseTime().percentile3().lt(10000),
                 * global().successfulRequests().percent().gt(90.0)
                 * );
                 * 
                 * // Test 4: Stress Test
                 * // Push system beyond normal capacity
                 * setUp(
                 * mixedUserScenario.injectOpen(
                 * rampUsers(USERS_PEAK * 2).during(Duration.ofMinutes(1)),
                 * constantUsersPerSec(USERS_PEAK * 2).during(Duration.ofMinutes(2))
                 * ).protocols(httpProtocol)
                 * ).assertions(
                 * global().responseTime().percentile4().lt(15000)
                 * );
                 */
        }
}
