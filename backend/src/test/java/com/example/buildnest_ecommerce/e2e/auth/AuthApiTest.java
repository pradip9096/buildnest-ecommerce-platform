package com.example.buildnest_ecommerce.e2e.auth;

import com.example.buildnest_ecommerce.e2e.BaseApiTest;
import com.example.buildnest_ecommerce.model.payload.LoginRequest;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthApiTest extends BaseApiTest {

    @Test
    public void testRegisterAndLogin() {
        String username = "testuser_" + System.currentTimeMillis();
        String email = "testuser_" + System.currentTimeMillis() + "@example.com";
        String password = "Password@123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setConsentGiven(true);

        // Register
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("message", equalTo("User registered successfully"));

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .cookie("access_token", notNullValue())
                .cookie("refresh_token", notNullValue())
                .body("data.accessToken", nullValue())
                .body("data.refreshToken", nullValue())
                .body("data.username", equalTo(username));
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("wrongpassword");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    // #113 (SEC-04): end-to-end refresh-token rotation through the real
    // service/repository — proves the old refresh token is revoked on
    // rotation and cannot be reused, not just that a bogus string is
    // rejected.
    @Test
    public void testRefreshTokenRotationRevokesOldToken() {
        String username = "rotationuser_" + System.currentTimeMillis();
        String email = "rotationuser_" + System.currentTimeMillis() + "@example.com";
        String password = "Password@123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("Rotation");
        registerRequest.setLastName("User");
        registerRequest.setConsentGiven(true);

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String originalRefreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie("refresh_token");

        assertThat(originalRefreshToken, notNullValue());

        // Rotate: exchange the original refresh token for a new one.
        String newRefreshToken = given()
                .cookie("refresh_token", originalRefreshToken)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie("refresh_token");

        assertThat(newRefreshToken, notNullValue());
        assertThat(newRefreshToken, not(equalTo(originalRefreshToken)));

        // Reuse of the original (now-rotated-out) refresh token must be rejected.
        given()
                .cookie("refresh_token", originalRefreshToken)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // The new refresh token from rotation must still work.
        given()
                .cookie("refresh_token", newRefreshToken)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
