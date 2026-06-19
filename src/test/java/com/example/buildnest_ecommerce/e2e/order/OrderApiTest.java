package com.example.buildnest_ecommerce.e2e.order;

import com.example.buildnest_ecommerce.e2e.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderApiTest extends BaseApiTest {

        private String token;
        private Long userId;

        @BeforeEach
        public void setupToken() {
                token = getAuthToken();
                userId = getUserId(token);
        }

        @Test
        @Order(1)
        public void testCheckoutProcess() {
                // 1. Seed Product
                Long productId = seedProduct();

                // 2. Add to Cart
                Map<String, Object> addItemRequest = new HashMap<>();
                addItemRequest.put("productId", productId);
                addItemRequest.put("quantity", 2);

                given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body(addItemRequest)
                                .queryParam("userId", userId)
                                .when()
                                .post("/api/user/cart/add")
                                .then()
                                .statusCode(HttpStatus.OK.value());

                // 3. Get Cart ID
                Object cartIdObj = given()
                                .header("Authorization", "Bearer " + token)
                                .pathParam("userId", userId)
                                .when()
                                .get("/api/user/cart/{userId}")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .extract()
                                .path("data.cartId");
                Long cartId = Long.valueOf(cartIdObj.toString());

                // 4. Process Checkout
                given()
                                .header("Authorization", "Bearer " + token)
                                .pathParam("cartId", cartId)
                                .when()
                                .post("/api/checkout/process/{cartId}")
                                .then()
                                .statusCode(HttpStatus.CREATED.value())
                                .body("success", equalTo(true))
                                .body("message", containsString("Order placed successfully"));
        }

        @Test
        @Order(2)
        public void testGetOrderHistory() {
                // Place an order first to ensure history isn't empty (though empty is also fine
                // to test)
                testCheckoutProcess();

                given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get("/api/user/orders")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .body("success", equalTo(true))
                                .body("data", notNullValue())
                                .body("data.size()", greaterThan(0));
        }

        @Test
        @Order(3)
        public void testGetOrderDetails() {
                // 1. Place Order
                Long productId = seedProduct();
                Map<String, Object> addItemRequest = new HashMap<>();
                addItemRequest.put("productId", productId);
                addItemRequest.put("quantity", 1);

                given()
                                .header("Authorization", "Bearer " + token)
                                .contentType(ContentType.JSON)
                                .body(addItemRequest)
                                .queryParam("userId", userId)
                                .when()
                                .post("/api/user/cart/add")
                                .then()
                                .statusCode(HttpStatus.OK.value());

                Object cartIdObj2 = given()
                                .header("Authorization", "Bearer " + token)
                                .pathParam("userId", userId)
                                .when()
                                .get("/api/user/cart/{userId}")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .extract()
                                .path("data.cartId");
                Long cartId = Long.valueOf(cartIdObj2.toString());

                Object orderIdObj = given()
                                .header("Authorization", "Bearer " + token)
                                .pathParam("cartId", cartId)
                                .when()
                                .post("/api/checkout/process/{cartId}")
                                .then()
                                .statusCode(HttpStatus.CREATED.value())
                                .extract()
                                .path("data.id");
                Long orderId = Long.valueOf(orderIdObj.toString());

                // 2. Get Details
                given()
                                .header("Authorization", "Bearer " + token)
                                .pathParam("orderId", orderId)
                                .when()
                                .get("/api/user/orders/{orderId}")
                                .then()
                                .statusCode(HttpStatus.OK.value())
                                .body("success", equalTo(true))
                                .body("data.id", notNullValue());
        }
}
