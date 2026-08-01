package com.example.buildnest_ecommerce.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected InventoryRepository inventoryRepository;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // SEC-15: CSRF protection is enabled for every non-exempt mutating endpoint (see
        // spring-security.md). A real browser fetches XSRF-TOKEN and echoes it as
        // X-XSRF-TOKEN automatically; RestAssured does not, so every mutating request in
        // this suite was receiving 403 until this bootstrap was added (#635).
        //
        // RestAssured.given() merges the static RestAssured.requestSpecification set
        // below, so on the 2nd+ test class in the same JVM this bootstrap call would
        // otherwise carry the *previous* class's already-valid XSRF-TOKEN cookie.
        // NonClearingCsrfTokenRepository (and CookieCsrfTokenRepository generally) skips
        // re-emitting Set-Cookie when the incoming cookie already matches the resolved
        // token, so .cookie("XSRF-TOKEN") on that response finds nothing new. Clearing
        // the static spec first forces a clean request and a genuinely fresh cookie.
        RestAssured.requestSpecification = null;
        String csrfToken = RestAssured.given()
                .when()
                .get("/api/auth/csrf")
                .then()
                .statusCode(204)
                .extract()
                .cookie("XSRF-TOKEN");

        RestAssured.requestSpecification = new io.restassured.builder.RequestSpecBuilder()
                .addCookie("XSRF-TOKEN", csrfToken)
                .addHeader("X-XSRF-TOKEN", csrfToken)
                .build();
    }

    protected String getAuthToken() {
        return getAuthToken(false);
    }

    protected String getAuthToken(boolean isAdmin) {
        String uniqueId = java.util.UUID.randomUUID().toString();
        String username = (isAdmin ? "admin_" : "user_") + uniqueId.substring(0, 8);
        String email = username + "@example.com";
        String password = "Password@123";

        // Register
        String firstName = "Test";
        String lastName = "User";
        String registerBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                username, email, password, firstName, lastName);
        RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(registerBody)
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        if (isAdmin) {
            promoteToAdmin(username);
        }

        // Login — tokens travel as httpOnly cookies (SEC-15), not in the JSON body
        String loginBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        return RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(loginBody)
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("access_token");
    }

    @Transactional
    public void promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_ADMIN");
                    role.setDescription("Administrator role");
                    return roleRepository.save(role);
                });

        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    @Transactional
    public Long seedProduct() {
        Category category = categoryRepository.findByName("Test Category")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("Test Category");
                    cat.setDescription("Category for testing");
                    return categoryRepository.save(cat);
                });

        Product product = new Product();
        product.setName("Test Product " + java.util.UUID.randomUUID().toString().substring(0, 8));
        product.setDescription("Product for testing purposes only");
        product.setPrice(new BigDecimal("100.00"));
        product.setSku("TEST-SKU-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        product.setCategory(category);
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Seed Inventory as well to prevent NPE in services
        com.example.buildnest_ecommerce.model.entity.Inventory inventory = new com.example.buildnest_ecommerce.model.entity.Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantityInStock(50);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(com.example.buildnest_ecommerce.model.entity.InventoryStatus.IN_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        return savedProduct.getId();
    }

    protected Long getUserId(String token) {
        Object id = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/user/profile")
                .then()
                .statusCode(200)
                .extract()
                .path("data.id");
        return Long.valueOf(id.toString());
    }
}
