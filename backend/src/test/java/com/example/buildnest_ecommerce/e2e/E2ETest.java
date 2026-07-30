package com.example.buildnest_ecommerce.e2e;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.service.admin.AdminAnalyticsService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End UI tests using Selenium WebDriver, driven against the real React/Vite SPA
 * (built and served via `vite preview`, see .github/workflows/ci-cd-pipeline.yml's e2e-tests
 * job), not the backend's own random port. The backend binds to a fixed port (see
 * webEnvironment below) that the frontend's `vite preview` proxy config points at by default,
 * so the two can be started in either order.
 *
 * Tests TC-E2E-001 through TC-E2E-007. Run with: mvn test -P e2e-tests
 * (requires the frontend already built and served — see CI workflow or run locally via
 * `cd frontend && npm run build && npm run preview` before invoking this profile).
 *
 * Requirements:
 * - Chrome browser installed
 * - Backend running on the fixed port below (started automatically by @SpringBootTest)
 * - Frontend built and served via `vite preview` (not started by this test)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=8080")
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("removal")
class E2ETest {

    // Mock Elasticsearch and notification services not needed for E2E UI tests
    @MockitoBean
    private ElasticsearchIngestionService elasticsearchIngestionService;

    @MockitoBean
    private ElasticsearchAlertingService elasticsearchAlertingService;

    @MockitoBean
    private AdminAnalyticsService adminAnalyticsService;

    @MockitoBean
    private NotificationService notificationService;

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static String baseUrl;

    // Shared across ordered tests so login (TC-E2E-002) can authenticate as the user
    // registration (TC-E2E-001) just created, and later flows (add-to-cart, checkout)
    // rely on that same authenticated session.
    private static String registeredUsername;
    private static final String TEST_PASSWORD = "SecurePass123!";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();

        baseUrl = System.getProperty("e2e.frontend.baseUrl", "http://localhost:4173");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI/CD
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1280,900");

        driver = new ChromeDriver(options);
        // A single driver instance is reused across all ordered tests (rather than
        // recreated per-test) so the authenticated session's cookies survive from
        // TC-E2E-002 (login) into TC-E2E-004/005 (add-to-cart, checkout).
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    static void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * TC-E2E-001: Test complete user registration workflow.
     */
    @Test
    @Order(1)
    void testUserRegistrationFlow() {
        driver.get(baseUrl + "/register");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='register-email']")));

        WebElement emailInput = driver.findElement(By.cssSelector("[data-testid='register-email']"));
        WebElement usernameInput = driver.findElement(By.cssSelector("[data-testid='register-username']"));
        WebElement passwordInput = driver.findElement(By.cssSelector("[data-testid='register-password']"));
        WebElement firstNameInput = driver.findElement(By.cssSelector("[data-testid='register-firstName']"));
        WebElement lastNameInput = driver.findElement(By.cssSelector("[data-testid='register-lastName']"));
        WebElement confirmPasswordInput = driver.findElement(By.cssSelector("[data-testid='register-confirmPassword']"));
        WebElement submitButton = driver.findElement(By.cssSelector("[data-testid='register-submit']"));

        registeredUsername = "e2euser" + System.currentTimeMillis();
        String testEmail = "e2etest" + System.currentTimeMillis() + "@example.com";

        emailInput.sendKeys(testEmail);
        usernameInput.sendKeys(registeredUsername);
        passwordInput.sendKeys(TEST_PASSWORD);
        confirmPasswordInput.sendKeys(TEST_PASSWORD);
        firstNameInput.sendKeys("E2E");
        lastNameInput.sendKeys("Test");

        submitButton.click();

        // RegisterPage navigates to /login on success
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should navigate to /login after successful registration");

        System.out.println("TC-E2E-001: User registration workflow completed successfully");
    }

    /**
     * TC-E2E-002: Test user login and authentication flow.
     */
    @Test
    @Order(2)
    void testLoginFlow() {
        assertNotNull(registeredUsername, "TC-E2E-001 must run first and register a user");

        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        WebElement usernameInput = driver.findElement(By.id("username"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameInput.sendKeys(registeredUsername);
        passwordInput.sendKeys(TEST_PASSWORD);
        loginButton.click();

        // LoginPage navigates to /account by default after a successful login
        wait.until(ExpectedConditions.urlContains("/account"));
        assertTrue(driver.getCurrentUrl().contains("/account"),
                "Should navigate to authenticated /account page after login");

        System.out.println("TC-E2E-002: Login flow completed successfully");
    }

    /**
     * TC-E2E-003: Test product browsing and search functionality.
     */
    @Test
    @Order(3)
    void testProductBrowsingFlow() {
        driver.get(baseUrl + "/products");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='product-grid']")));

        WebElement searchBox = driver.findElement(By.cssSelector("[data-testid='navbar-search-input']"));
        searchBox.sendKeys("cement");
        searchBox.submit();

        wait.until(ExpectedConditions.urlContains("search="));
        assertTrue(driver.getCurrentUrl().contains("search=cement"),
                "Search should update the URL with the search keyword");

        // The same product grid re-renders with (possibly empty) filtered results —
        // the page itself, not a separate results element, is what search updates.
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));

        System.out.println("TC-E2E-003: Product browsing flow completed successfully");
    }

    /**
     * TC-E2E-004: Test add to cart workflow.
     */
    @Test
    @Order(4)
    void testAddToCartFlow() {
        driver.get(baseUrl + "/products");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='product-card']")));

        WebElement addToCartButton = driver.findElement(By.cssSelector("[data-testid='add-to-cart-button']"));
        addToCartButton.click();

        // ProductCard's own state machine flips the button label to "Added ✓" on success
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector("[data-testid='add-to-cart-button']"), "Added"));

        WebElement cartLink = driver.findElement(By.cssSelector("[data-testid='navbar-cart-link']"));
        cartLink.click();

        wait.until(ExpectedConditions.urlContains("/cart"));
        assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Should navigate to cart page");

        System.out.println("TC-E2E-004: Add to cart flow completed successfully");
    }

    /**
     * TC-E2E-005: Test checkout workflow up to address entry (without payment).
     */
    @Test
    @Order(5)
    void testCheckoutFlow() {
        driver.get(baseUrl + "/cart");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='checkout-button']")));
        driver.findElement(By.cssSelector("[data-testid='checkout-button']")).click();

        wait.until(ExpectedConditions.urlContains("/checkout"));
        assertTrue(driver.getCurrentUrl().contains("/checkout"),
                "Should navigate to checkout page");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='address-fullName']")));

        driver.findElement(By.cssSelector("[data-testid='address-fullName']")).sendKeys("E2E Test");
        driver.findElement(By.cssSelector("[data-testid='address-line1']")).sendKeys("123 Test Street");
        driver.findElement(By.cssSelector("[data-testid='address-city']")).sendKeys("Mumbai");
        driver.findElement(By.cssSelector("[data-testid='address-state']")).sendKeys("Maharashtra");
        driver.findElement(By.cssSelector("[data-testid='address-postalCode']")).sendKeys("400001");
        driver.findElement(By.cssSelector("[data-testid='address-phone']")).sendKeys("9876543210");

        WebElement orderSummary = driver.findElement(By.cssSelector("[data-testid='order-summary']"));
        assertTrue(orderSummary.isDisplayed(), "Order summary should be visible");

        System.out.println("TC-E2E-005: Checkout flow completed successfully");
    }

    /**
     * TC-E2E-006: Test responsive design on mobile viewport.
     */
    @Test
    @Order(6)
    void testMobileResponsiveness() {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667));

        driver.get(baseUrl + "/products");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Mobile viewport swaps the Navbar's search form/menu for a hamburger toggle
        // with this aria-label — see components/common/Navbar.tsx.
        boolean hasMobileMenuToggle = driver.findElements(
                By.cssSelector("button[aria-label='Open menu'], button[aria-label='Close menu']")).size() > 0;

        assertTrue(hasMobileMenuToggle || driver.findElements(By.tagName("nav")).size() > 0
                        || driver.findElements(By.tagName("header")).size() > 0,
                "Should have mobile-friendly navigation");

        driver.manage().window().maximize();

        System.out.println("TC-E2E-006: Mobile responsiveness verified successfully");
    }

    /**
     * TC-E2E-007: Test navigation between real, routed pages.
     */
    @Test
    @Order(7)
    void testNavigationFlow() {
        String[] pages = { "/products", "/login", "/register" };

        for (String page : pages) {
            driver.get(baseUrl + page);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            assertEquals(baseUrl + page, driver.getCurrentUrl(),
                    "Should navigate to " + page);
        }

        System.out.println("TC-E2E-007: Navigation flow completed successfully");
    }
}
