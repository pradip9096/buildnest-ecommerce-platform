package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.ProductCreatedEvent;
import com.example.buildnest_ecommerce.event.ProductDeletedEvent;
import com.example.buildnest_ecommerce.event.ProductUpdatedEvent;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private final DomainEventPublisher domainEventPublisher;

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
        product.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        product.setCreatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }

        Product saved = productRepository.save(product);
        domainEventPublisher.publish(new ProductCreatedEvent(this, saved));
        return saved;
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
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }

        Product saved = productRepository.save(product);
        domainEventPublisher.publish(new ProductUpdatedEvent(this, saved));
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public void deleteProduct(Long productId) {
        log.info("Soft-deleting product with id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        domainEventPublisher.publish(new ProductDeletedEvent(this, productId));
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public Product updateProductImage(Long productId, String imageUrl) {
        log.info("Updating image for product id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        product.setImageUrl(imageUrl);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category: {}", categoryId);
        return productRepository.findByCategory(categoryId, Pageable.unpaged()).getContent();
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
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> getFeaturedProducts() {
        log.info("Fetching featured products");
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue();
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
            Boolean inStock, String tag, Pageable pageable) {
        log.info("Advanced search (JPA) — query: {}, categoryId: {}, priceRange: {} - {}, tag: {}", query, categoryId,
                minPrice, maxPrice, tag);
        return productRepository.advancedSearch(query, categoryId, minPrice, maxPrice, inStock, true, tag, pageable);
    }

    @Override
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products by category: {}", categoryId);
        return productRepository.findByCategory(categoryId, pageable);
    }

    private static final int RELATED_PRODUCTS_LIMIT = 8;

    /**
     * A JPQL {@code IN} clause rejects an empty collection, so a source
     * product with no tags is queried against this sentinel instead —
     * guaranteed not to match any real {@link ProductTag} id.
     */
    private static final List<Long> NO_TAGS_SENTINEL = List.of(-1L);

    @Override
    @Cacheable(cacheNames = "relatedProducts", key = "#productId")
    public List<Product> getRelatedProducts(Long productId) {
        log.info("Fetching related products for id: {}", productId);
        Product source = getProductById(productId);
        Long categoryId = source.getCategory() != null ? source.getCategory().getId() : null;
        List<Long> tagIds = source.getTags().stream().map(ProductTag::getId).toList();
        if (tagIds.isEmpty()) {
            tagIds = NO_TAGS_SENTINEL;
        }
        Pageable limit = PageRequest.of(0, RELATED_PRODUCTS_LIMIT);
        return productRepository.findRelatedProducts(productId, categoryId, tagIds, limit);
    }
}
