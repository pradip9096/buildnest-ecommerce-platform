package com.example.buildnest_ecommerce.e2e.product;

import com.example.buildnest_ecommerce.e2e.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductApiTest extends BaseApiTest {

    private String token;

    @BeforeEach
    public void setupToken() {
        token = getAuthToken();
        seedProduct();
    }

    @Test
    @Order(1)
    public void testGetAllProductsV2() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v2/products")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(true))
                .body("data.content", notNullValue()) // Check for pagination content
                .body("data.size", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testSearchProducts() {
        // Note: seedProduct() doesn't take name, so we just search for "Test" which is
        // in all seeded products
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .queryParam("query", "Test")
                .when()
                .get("/api/v2/products/search")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(true))
                .body("data.content", notNullValue())
                .body("data.content.size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void testGetProductDetails() {
        Long productId = seedProduct();
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v2/products/" + productId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(true))
                .body("data.id", equalTo(productId.intValue()));
    }

    @Test
    @Order(4)
    public void testGetProductsByCategory() {
        Long productId = seedProduct();

        // 1. Get product to find its category ID
        Object catIdObj = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v2/products/" + productId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.category.id");

        Long categoryId = Long.valueOf(catIdObj.toString());

        // 2. Get by category
        given()
                .header("Authorization", "Bearer " + token)
                .pathParam("categoryId", categoryId)
                .when()
                .get("/api/v2/products/category/{categoryId}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(true))
                .body("data.content", notNullValue())
                .body("data.content.size()", greaterThan(0));
    }

    @Test
    @Order(5)
    public void testGetDeprecatedV1Products() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/products")
                .then()
                .statusCode(HttpStatus.OK.value())
                .header("X-API-Deprecated", "true")
                .header("X-API-Sunset", "2026-12-31");
    }
}
