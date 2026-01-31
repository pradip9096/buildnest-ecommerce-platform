package com.example.buildnest_ecommerce.controller.public_;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController Tests")
class HomeControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setStockQuantity(10);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Category Description");
    }

    @Test
    @DisplayName("Should return welcome message on home endpoint")
    void testGetHome() throws Exception {
        mockMvc.perform(get("/api/public"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Welcome to BuildNest – E-Commerce Platform for Home Construction and Décor Products API"));
    }

    @Test
    @DisplayName("Should return health status")
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("API is running"));
    }

    @Test
    @DisplayName("Should return all products successfully")
    void testGetAllProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("Should handle error when getting all products")
    void testGetAllProductsError() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error retrieving products"));
    }

    @Test
    @DisplayName("Should return product by ID successfully")
    void testGetProductById() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(get("/api/public/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("Should handle error when product not found")
    void testGetProductByIdNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(999L)).thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/public/products/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    @DisplayName("Should search products successfully")
    void testSearchProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchProducts("Test")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/public/products/search")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Search results"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("Should handle error when searching products")
    void testSearchProductsError() throws Exception {
        // Arrange
        when(productService.searchProducts(anyString())).thenThrow(new RuntimeException("Search error"));

        // Act & Assert
        mockMvc.perform(get("/api/public/products/search")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error searching products"));
    }

    @Test
    @DisplayName("Should return all categories successfully")
    void testGetAllCategories() throws Exception {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Category"));
    }

    @Test
    @DisplayName("Should handle error when getting all categories")
    void testGetAllCategoriesError() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error retrieving categories"));
    }

    @Test
    @DisplayName("Should return empty list when no products available")
    void testGetAllProductsEmpty() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when search finds no results")
    void testSearchProductsEmpty() throws Exception {
        // Arrange
        when(productService.searchProducts("NonExistent")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/public/products/search")
                .param("keyword", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
