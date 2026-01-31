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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End UI tests using Selenium WebDriver.
 * Tests TC-E2E-001 through TC-E2E-005.
 * 
 * These tests verify complete user workflows through the browser.
 * Run with: mvn test -Dtest=E2ETest
 * 
 * Requirements:
 * - Chrome browser installed
 * - Application running on local server
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class E2ETest {

    // Mock Elasticsearch and notification services not needed for E2E UI tests
    @MockBean
    private ElasticsearchIngestionService elasticsearchIngestionService;

    @MockBean
    private ElasticsearchAlertingService elasticsearchAlertingService;

    @MockBean
    private AdminAnalyticsService adminAnalyticsService;

    @MockBean
    private NotificationService notificationService;

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI/CD
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void teardown() {
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

        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));

        // Fill registration form
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement firstNameInput = driver.findElement(By.id("firstName"));
        WebElement lastNameInput = driver.findElement(By.id("lastName"));
        WebElement submitButton = driver.findElement(By.id("registerButton"));

        String testEmail = "e2etest" + System.currentTimeMillis() + "@example.com";
        emailInput.sendKeys(testEmail);
        passwordInput.sendKeys("SecurePass123!");
        firstNameInput.sendKeys("E2E");
        lastNameInput.sendKeys("Test");

        submitButton.click();

        // Verify registration success (adjust selectors based on actual UI)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.presenceOfElementLocated(By.className("success-message"))));

        System.out.println("TC-E2E-001: User registration workflow completed successfully");
    }

    /**
     * TC-E2E-002: Test user login and authentication flow.
     */
    @Test
    @Order(2)
    void testLoginFlow() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));

        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        // Use test credentials
        emailInput.sendKeys("test@example.com");
        passwordInput.sendKeys("password123");
        loginButton.click();

        // Verify successful login
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/products"),
                ExpectedConditions.presenceOfElementLocated(By.className("user-profile"))));

        assertTrue(driver.getCurrentUrl().contains("dashboard") ||
                driver.getCurrentUrl().contains("products"),
                "Should navigate to authenticated page after login");

        System.out.println("TC-E2E-002: Login flow completed successfully");
    }

    /**
     * TC-E2E-003: Test product browsing and search functionality.
     */
    @Test
    @Order(3)
    void testProductBrowsingFlow() {
        driver.get(baseUrl + "/products");

        // Wait for products to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-list")));

        // Verify products are displayed
        WebElement productList = driver.findElement(By.className("product-list"));
        assertTrue(productList.isDisplayed(), "Product list should be visible");

        // Test search functionality
        WebElement searchBox = driver.findElement(By.id("searchInput"));
        searchBox.sendKeys("cement");
        searchBox.submit();

        // Wait for search results
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("search-results")));

        System.out.println("TC-E2E-003: Product browsing flow completed successfully");
    }

    /**
     * TC-E2E-004: Test add to cart workflow.
     */
    @Test
    @Order(4)
    void testAddToCartFlow() {
        driver.get(baseUrl + "/products");

        // Wait for products
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));

        // Click first "Add to Cart" button
        WebElement addToCartButton = driver.findElement(By.className("add-to-cart-btn"));
        addToCartButton.click();

        // Verify cart update
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.className("cart-notification")),
                ExpectedConditions.attributeContains(By.id("cartCount"), "textContent", "1")));

        // Navigate to cart
        WebElement cartIcon = driver.findElement(By.id("cartIcon"));
        cartIcon.click();

        // Verify cart page
        wait.until(ExpectedConditions.urlContains("/cart"));
        assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Should navigate to cart page");

        System.out.println("TC-E2E-004: Add to cart flow completed successfully");
    }

    /**
     * TC-E2E-005: Test complete checkout workflow (without payment).
     */
    @Test
    @Order(5)
    void testCheckoutFlow() {
        // First add item to cart
        driver.get(baseUrl + "/products");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("add-to-cart-btn")));
        driver.findElement(By.className("add-to-cart-btn")).click();

        // Navigate to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("cartIcon")));
        driver.findElement(By.id("cartIcon")).click();

        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkoutButton")));
        driver.findElement(By.id("checkoutButton")).click();

        // Verify checkout page
        wait.until(ExpectedConditions.urlContains("/checkout"));
        assertTrue(driver.getCurrentUrl().contains("/checkout"),
                "Should navigate to checkout page");

        // Fill shipping address
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("shippingAddress")));
        WebElement addressInput = driver.findElement(By.id("shippingAddress"));
        addressInput.sendKeys("123 Test Street, Test City, 12345");

        // Verify order summary is displayed
        WebElement orderSummary = driver.findElement(By.className("order-summary"));
        assertTrue(orderSummary.isDisplayed(), "Order summary should be visible");

        System.out.println("TC-E2E-005: Checkout flow completed successfully");
    }

    /**
     * TC-E2E-006: Test responsive design on mobile viewport.
     */
    @Test
    @Order(6)
    void testMobileResponsiveness() {
        // Set mobile viewport
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667));

        driver.get(baseUrl + "/products");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Verify mobile menu is present
        boolean hasMobileMenu = driver.findElements(By.className("mobile-menu")).size() > 0 ||
                driver.findElements(By.className("hamburger-menu")).size() > 0;

        assertTrue(hasMobileMenu || driver.findElements(By.tagName("nav")).size() > 0,
                "Should have mobile-friendly navigation");

        System.out.println("TC-E2E-006: Mobile responsiveness verified successfully");
    }

    /**
     * TC-E2E-007: Test navigation between pages.
     */
    @Test
    @Order(7)
    void testNavigationFlow() {
        driver.get(baseUrl);

        // Navigate through main pages
        String[] pages = { "/products", "/about", "/contact" };

        for (String page : pages) {
            driver.get(baseUrl + page);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            assertEquals(baseUrl + page, driver.getCurrentUrl(),
                    "Should navigate to " + page);
        }

        System.out.println("TC-E2E-007: Navigation flow completed successfully");
    }
}
