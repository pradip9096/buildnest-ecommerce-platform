package com.example.buildnest_ecommerce.e2e.cart;

import com.example.buildnest_ecommerce.e2e.BaseApiTest;
import com.example.buildnest_ecommerce.model.payload.AddItemRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.*;

public class CartApiTest extends BaseApiTest {

        private String token;
        private Long userId;
        private Long productId;

        @BeforeEach
        public void setupCart() {
                token = getAuthToken();
                userId = getUserId(token);
                productId = seedProduct();
        }

        @Test
        public void testCartOperations() {
                // 1. Add item to cart
                AddItemRequest addRequest = new AddItemRequest(productId, 2);

                RestAssured.given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body(addRequest)
                                .queryParam("userId", userId)
                                .when()
                                .post("/api/user/cart/add")
                                .then()
                                .statusCode(200)
                                .body("success", is(true))
                                .body("message", containsString("successfully"));

                // 2. Get cart
                RestAssured.given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get("/api/user/cart/" + userId)
                                .then()
                                .statusCode(200)
                                .body("data.userId", is(userId.intValue()))
                                .body("data.items", not(empty()))
                                .body("data.totalAmount", greaterThan(0.0f));

                // 3. Get cart total
                RestAssured.given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get("/api/user/cart/total/" + userId)
                                .then()
                                .statusCode(200)
                                .body("data", greaterThan(0.0f));

                // 4. Clear cart
                RestAssured.given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .delete("/api/user/cart/clear/" + userId)
                                .then()
                                .statusCode(200)
                                .body("success", is(true));

                // 5. Verify cart is empty
                RestAssured.given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get("/api/user/cart/" + userId)
                                .then()
                                .statusCode(200)
                                .body("data.items", empty());
        }
}
