package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long productId);

    Product createProduct(CreateProductRequest request);

    Product updateProduct(Long productId, CreateProductRequest request);

    void deleteProduct(Long productId);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> searchProducts(String keyword);

    // New methods for V2 API
    Page<Product> findAll(Pageable pageable);

    Product findById(Long id);

    Page<Product> advancedSearch(String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, Pageable pageable);

    Page<Product> findByCategory(Long categoryId, Pageable pageable);
}
