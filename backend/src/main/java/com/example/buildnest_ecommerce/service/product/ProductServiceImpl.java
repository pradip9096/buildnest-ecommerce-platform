package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.ProductCreatedEvent;
import com.example.buildnest_ecommerce.event.ProductDeletedEvent;
import com.example.buildnest_ecommerce.event.ProductUpdatedEvent;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
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
import java.util.ArrayList;
import java.util.HashSet;
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

    private static final String PRODUCT_NOT_FOUND_MSG =
            "Product not found with id: ";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Retrieves all products from catalog.
     *
     * @return a list of all Product entities
     */
    @Override
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        // Force-initialize lazy `tags`/`variants` while the session is open —
        // open-in-view is disabled, so an uninitialized proxy throws once
        // Jackson serializes the response after the transaction has already
        // closed. Copied into plain collections (not just initialized) so a
        // Redis-cached caller of getProductById never has to deserialize a
        // Hibernate PersistentSet/PersistentBag — see that method's comment.
        products.forEach(ProductServiceImpl::detachCollections);
        return products;
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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        PRODUCT_NOT_FOUND_MSG + productId));
        detachCollections(product);
        return product;
    }

    /**
     * Initializes `tags`/`variants` and copies each into a plain
     * HashSet/ArrayList. A Hibernate-initialized collection is still a
     * PersistentSet/PersistentBag at runtime; the Redis cache serializer
     * (default typing) embeds that concrete class name on write, then fails
     * to instantiate it on a cache-hit read (those classes require a live
     * Hibernate session, not a no-arg constructor) — surfacing as a false
     * "product not found" once cached. Copying to a plain collection avoids
     * this entirely.
     *
     * Also initializes and unproxies the lazy `category`/`inventory`
     * references (#651): {@code Hibernate.initialize()} alone forces a
     * proxy's target to load but does not change the reference's runtime
     * type — it stays a Hibernate proxy subclass, which the Redis cache
     * serializer embeds by class name on write and then fails to
     * instantiate on a cache-hit read (the proxy class requires a live
     * Hibernate session, not a no-arg constructor), the same asymmetry as
     * the collection fields above. {@code Hibernate.unproxy()} replaces the
     * reference with the plain entity instance. `category` was previously
     * never touched at all here, relying on it happening to be initialized
     * elsewhere in the request — the same "don't infer one field is safe
     * from another's incidental behavior" trap this repo's own wiki lesson
     * on this bug family warns against.
     */
    private static void detachCollections(Product product) {
        Hibernate.initialize(product.getTags());
        product.setTags(new HashSet<>(product.getTags()));
        Hibernate.initialize(product.getVariants());
        product.setVariants(new ArrayList<>(product.getVariants()));
        if (product.getCategory() != null) {
            Hibernate.initialize(product.getCategory());
            product.setCategory((Category)
                    Hibernate.unproxy(product.getCategory()));
        }
        if (product.getInventory() != null) {
            Hibernate.initialize(product.getInventory());
            product.setInventory((Inventory)
                    Hibernate.unproxy(product.getInventory()));
        }
    }

    /**
     * Creates a new product from the provided request data.
     *
     * Initializes product with default values and associates it with the
     * specified category. Clears all product cache entries on successful
     * creation.
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
        return buildAndSaveProduct(request, null);
    }

    /**
     * Creates a product owned by a verified seller (FR-SEL-03/04, #555).
     * Rejects the request unless {@code seller} has a {@link Seller}
     * record with {@code VerificationStatus.VERIFIED} — a pending or
     * rejected seller cannot list products.
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public Product createProductForSeller(
            CreateProductRequest request, Long sellerUserId) {
        Seller sellerProfile = sellerRepository
                .findByUser_Id(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller profile not found for user: "
                                + sellerUserId));
        if (sellerProfile.getVerificationStatus()
                != Seller.VerificationStatus.VERIFIED) {
            throw new AccessDeniedException(
                    "Seller is not verified; cannot create products");
        }
        return buildAndSaveProduct(request, sellerProfile.getUser());
    }

    private Product buildAndSaveProduct(
            CreateProductRequest request, User seller) {
        log.info("Creating new product: {}", request.getName());
        Product product = new Product();
        product.setSeller(seller);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        product.setCreatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found")));
        }

        int initialStock = request.getStockQuantity() != null
                ? request.getStockQuantity()
                : 0;
        Product saved = productRepository.save(product);
        Inventory inventory = new Inventory();
        inventory.setProduct(saved);
        inventory.setQuantityInStock(initialStock);
        inventory.setMinimumStockLevel(0);
        inventory.setUseCategoryThreshold(true);
        inventory.setStatus(initialStock > 0
                ? InventoryStatus.IN_STOCK
                : InventoryStatus.OUT_OF_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        // Link in-memory (not an explicit inventoryRepository.save() call —
        // Product.inventory is cascade=ALL, so an explicit save() here plus
        // the cascade-persist Hibernate performs on this same transaction's
        // flush double-inserts the identical row; verified empirically via
        // a duplicate-key H2 error with identical bound values on both
        // inserts). Cascade alone persists it once, and getStockQuantity()
        // (derived from inventory, #485) is correct immediately for this
        // same request/response and for the ProductCreatedEvent below — the
        // OneToOne is mappedBy="product", so it's never populated
        // automatically from the Inventory side.
        saved.setInventory(inventory);
        detachCollections(saved);

        domainEventPublisher.publish(new ProductCreatedEvent(this, saved));
        return saved;
    }

    /**
     * Updates an existing product with new information.
     *
     * Modifies product details and clears the specific product cache entry
     * to ensure fresh data on next retrieval.
     *
     * @param productId the unique identifier of the product to update -
     *                  required
     * @param request   the CreateProductRequest containing updated product
     *                  details (name, description, price, category ID,
     *                  etc.) - required
     * @return the updated Product entity
     * @throws RuntimeException if the product or category is not found
     */
    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    @SuppressWarnings("null")
    public Product updateProduct(Long productId, CreateProductRequest request) {
        Product product = getProductById(productId);
        return applyUpdate(product, request);
    }

    /**
     * Updates a product owned by the given seller (FR-SEL-04, #555) —
     * scoped so a seller can only ever update their own listings.
     */
    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public Product updateProductForSeller(
            Long sellerUserId, Long productId, CreateProductRequest request) {
        Product product = productRepository
                .findByIdAndSeller_Id(productId, sellerUserId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Product " + productId
                                + " does not belong to seller "
                                + sellerUserId));
        return applyUpdate(product, request);
    }

    private Product applyUpdate(
            Product product, CreateProductRequest request) {
        log.info("Updating product with id: {}", product.getId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        // request.getStockQuantity() is intentionally NOT applied here —
        // Inventory is the sole writable source of stock (#485); stock
        // changes on an existing product go through AdminInventoryController
        // /adjustInventory, not this product-update request. A non-null
        // value here is silently ignored, matching how creation-only fields
        // already behave in this shared Create*Request DTO.
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found")));
        }

        Product saved = productRepository.save(product);
        domainEventPublisher.publish(new ProductUpdatedEvent(this, saved));
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        PRODUCT_NOT_FOUND_MSG + productId));
        softDeleteProduct(product);
    }

    /**
     * Soft-deletes a product owned by the given seller (FR-SEL-04, #555) —
     * scoped so a seller can only ever remove their own listings.
     */
    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public void deleteProductForSeller(Long sellerUserId, Long productId) {
        Product product = productRepository
                .findByIdAndSeller_Id(productId, sellerUserId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Product " + productId
                                + " does not belong to seller "
                                + sellerUserId));
        softDeleteProduct(product);
    }

    private void softDeleteProduct(Product product) {
        log.info("Soft-deleting product with id: {}", product.getId());
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        domainEventPublisher.publish(
                new ProductDeletedEvent(this, product.getId()));
    }

    @Override
    public Page<Product> getProductsForSeller(
            Long sellerUserId, Pageable pageable) {
        return productRepository
                .findBySeller_Id(sellerUserId, pageable)
                .map(p -> {
                    detachCollections(p);
                    return p;
                });
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public Product updateProductImage(Long productId, String imageUrl) {
        log.info("Updating image for product id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        PRODUCT_NOT_FOUND_MSG + productId));
        product.setImageUrl(imageUrl);
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        detachCollections(saved);
        return saved;
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category: {}", categoryId);
        List<Product> products = productRepository
                .findByCategory(categoryId, Pageable.unpaged())
                .getContent();
        products.forEach(ProductServiceImpl::detachCollections);
        return products;
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
        List<Product> products = productRepository
                .findByNameContainingIgnoreCase(keyword);
        products.forEach(ProductServiceImpl::detachCollections);
        return products;
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
                .orElseThrow(() -> new RuntimeException(
                        PRODUCT_NOT_FOUND_MSG + id));
    }

    @Override
    public Page<Product> advancedSearch(String query, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
            String tag, Pageable pageable) {
        log.info(
                "Advanced search (JPA) — query: {}, categoryId: {}, "
                        + "priceRange: {} - {}, tag: {}",
                query, categoryId, minPrice, maxPrice, tag);
        return productRepository.advancedSearch(query, categoryId, minPrice,
                maxPrice, inStock, true, tag, pageable);
    }

    @Override
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products by category: {}", categoryId);
        return productRepository.findByCategory(categoryId, pageable);
    }

    /** Maximum number of related products returned (PROD-04, #84). */
    private static final int RELATED_PRODUCTS_LIMIT = 8;

    /**
     * A JPQL {@code IN} clause rejects an empty collection, so a source
     * product with no tags is queried against this sentinel instead —
     * guaranteed not to match any real {@link ProductTag} id.
     */
    private static final List<Long> NO_TAGS_SENTINEL = List.of(-1L);

    /**
     * Returns up to {@value #RELATED_PRODUCTS_LIMIT} products related to
     * {@code productId}: same category first, then shared tags (PROD-04, #84).
     *
     * @param productId the ID of the source product (required)
     * @return the ranked list of related products, possibly empty
     * @throws RuntimeException if the source product is not found
     */
    @Override
    @Cacheable(cacheNames = "relatedProducts", key = "#productId")
    public List<Product> getRelatedProducts(final Long productId) {
        log.info("Fetching related products for id: {}", productId);
        Product source = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        PRODUCT_NOT_FOUND_MSG + productId));
        Category category = source.getCategory();
        Long categoryId = category == null ? null : category.getId();
        List<Long> tagIds = source.getTags().stream()
                .map(ProductTag::getId)
                .toList();
        if (tagIds.isEmpty()) {
            tagIds = NO_TAGS_SENTINEL;
        }
        Pageable limit = PageRequest.of(0, RELATED_PRODUCTS_LIMIT);
        return productRepository.findRelatedProducts(
                productId, categoryId, tagIds, limit);
    }
}
