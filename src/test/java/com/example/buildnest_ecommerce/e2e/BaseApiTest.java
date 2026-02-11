package com.example.buildnest_ecommerce.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }
}
