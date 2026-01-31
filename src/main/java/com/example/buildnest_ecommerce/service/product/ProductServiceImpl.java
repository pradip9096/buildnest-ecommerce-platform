package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service Implementation
 *
 * Manages product catalog operations including creation, retrieval, updates,
 * search, and caching.
 * Handles product lifecycle and maintains product availability information.
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "products")
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Retrieves all products from catalog.
     *
     * @return a list of all Product entities
     */
    @Override
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Retrieves a product by ID with caching.
     *
     * @param productId the ID of the product to retrieve (required)
     * @return the Product entity
     * @throws RuntimeException if product is not found
     */
    @Override
    @Cacheable(key = "#productId")
    @SuppressWarnings("null")
    public Product getProductById(Long productId) {
        log.info("Fetching product with id: {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    /**
     * Creates a new product from the provided request data.
     *
     * Initializes product with default values and associates it with the
     * specified category. Clears all product cache entries on successful creation.
     *
     * @param request the CreateProductRequest containing product details
     *                (name, description, price, category ID, etc.) - required
     * @return the newly created Product entity with auto-generated ID
     * @throws RuntimeException if the specified category is not found
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    @SuppressWarnings("null")
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCreatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }

        return productRepository.save(product);
    }

    /**
     * Updates an existing product with new information.
     *
     * Modifies product details and clears the specific product cache entry
     * to ensure fresh data on next retrieval.
     *
     * @param productId the unique identifier of the product to update - required
     * @param request   the CreateProductRequest containing updated product details
     *                  (name, description, price, category ID, etc.) - required
     * @return the updated Product entity
     * @throws RuntimeException if the product or category is not found
     */
    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    @SuppressWarnings("null")
    public Product updateProduct(Long productId, CreateProductRequest request) {
        log.info("Updating product with id: {}", productId);
        Product product = getProductById(productId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    @SuppressWarnings("null")
    public void deleteProduct(Long productId) {
        log.info("Deleting product with id: {}", productId);
        productRepository.deleteById(productId);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category: {}", categoryId);
        return productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());
    }

    /**
     * Clear product cache daily at 2 AM
     * Ensures fresh data and prevents stale cache issues
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @CacheEvict(allEntries = true)
    public void clearProductCache() {
        log.info("Clearing product cache - scheduled task");
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        log.info("Fetching all products with pagination");
        return productRepository.findAll(pageable);
    }

    @Override
    public Product findById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Page<Product> advancedSearch(String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, Pageable pageable) {
        log.info("Advanced search - query: {}, categoryId: {}, priceRange: {} - {}", query, categoryId, minPrice,
                maxPrice);

        List<Product> allProducts = productRepository.findAll();
        List<Product> filtered = allProducts.stream()
                .filter(p -> query == null || p.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(p -> categoryId == null
                        || (p.getCategory() != null && p.getCategory().getId().equals(categoryId)))
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> inStock == null || !inStock || p.getStockQuantity() > 0)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @Override
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products by category: {}", categoryId);

        List<Product> categoryProducts = productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), categoryProducts.size());

        return new PageImpl<>(categoryProducts.subList(start, end), pageable, categoryProducts.size());
    }
}
