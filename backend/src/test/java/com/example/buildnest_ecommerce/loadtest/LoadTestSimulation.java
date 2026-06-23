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
        ChainBuilder browseProductsChain = exec(
                        http("Get Products List")
                                        .get("/api/products")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(2))
                        .exec(
                                        http("Get Product Details")
                                                        .get("/api/products/1")
                                                        .check(status().in(200, 404)))
                        .pause(Duration.ofSeconds(1));

        ScenarioBuilder browseProductsScenario = scenario("Browse Products")
                        .exec(browseProductsChain);

        // Scenario: Search Products (TC-LOAD-002)
        ChainBuilder searchProductsChain = exec(
                        http("Search Products")
                                        .get("/api/products/search?q=cement")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(2));

        ScenarioBuilder searchProductsScenario = scenario("Search Products")
                        .exec(searchProductsChain);

        // Scenario: Authentication Flow (TC-LOAD-003)
        ChainBuilder authenticationChain = exec(
                        http("Login Request")
                                        .post("/api/auth/login")
                                        .header("Content-Type", "application/json")
                                        .body(StringBody("""
                                                        {
                                                            "email": "test@example.com",
                                                            "password": "password123"
                                                        }
                                                        """))
                                        .check(status().in(200, 401)))
                        .pause(Duration.ofSeconds(3));

        ScenarioBuilder authenticationScenario = scenario("User Authentication")
                        .exec(authenticationChain);

        // Scenario: Add to Cart (TC-LOAD-004)
        ChainBuilder addToCartChain = exec(
                        http("View Products")
                                        .get("/api/products")
                                        .check(status().is(200)))
                        .pause(Duration.ofSeconds(1))
                        .exec(
                                        http("Add to Cart")
                                                        .post("/api/cart/items")
                                                        .header("Content-Type", "application/json")
                                                        .body(StringBody("""
                                                                        {
                                                                            "productId": 1,
                                                                            "quantity": 2
                                                                        }
                                                                        """))
                                                        .check(status().in(200, 201, 401)))
                        .pause(Duration.ofSeconds(2));

        ScenarioBuilder addToCartScenario = scenario("Add to Cart")
                        .exec(addToCartChain);

        // Test 1: Ramp-Up Load Test
        // Gradually increase load to identify breaking point
        {
                setUp(
                                browseProductsScenario.injectOpen(
                                                rampUsers(USERS_RAMP).during(RAMP_DURATION)))
                                .protocols(httpProtocol)
                                .assertions(
                                                global().responseTime().max().lt(5000),
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
