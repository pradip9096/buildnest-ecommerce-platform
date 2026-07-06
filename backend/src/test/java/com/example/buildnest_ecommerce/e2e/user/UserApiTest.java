package com.example.buildnest_ecommerce.e2e.user;

import com.example.buildnest_ecommerce.e2e.BaseApiTest;
import com.example.buildnest_ecommerce.model.payload.LoginRequest;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseApiTest {

        @Test
        public void testGetUserProfile() {
                String username = "user_profile_test_" + System.currentTimeMillis();
                String password = "Password@123";
                String email = username + "@example.com";

                // Register User
                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setUsername(username);
                registerRequest.setEmail(email);
                registerRequest.setPassword(password);
                registerRequest.setFirstName("Test");
                registerRequest.setLastName("User");

                given()
                                .contentType(ContentType.JSON)
                                .body(registerRequest)
                                .when()
                                .post("/api/auth/register")
                                .then()
                                .statusCode(HttpStatus.CREATED.value());

                // Login to get token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(username);
                loginRequest.setPassword(password);

                String accessToken = given()
                                .contentType(ContentType.JSON)
                                .body(loginRequest)
                                .when()
                                .post("/api/auth/login")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .extract().cookie("access_token");

                // Get Profile
                given()
                                .header("Authorization", "Bearer " + accessToken)
                                .when()
                                .get("/api/user/profile")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .body("data.username", equalTo(username))
                                .body("data.email", equalTo(email));
        }
}
