package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ProductElasticsearchRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Elasticsearch-backed implementation of {@link ProductSearchService} (SRCH-01/SRCH-02, #74/#75).
 * Only registered when {@code elasticsearch.enabled=true}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductElasticsearchRepository esRepository;
    private final ProductRepository productRepository;
    private final CircuitBreaker elasticsearchCircuitBreaker;

    @Override
    public Page<ProductDocument> search(String query, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, String tag, Pageable pageable) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(() ->
                    doSearch(query, categoryId, minPrice, maxPrice, inStock, tag, pageable));
        } catch (CallNotPermittedException e) {
            log.warn("Elasticsearch circuit breaker open — returning empty search results");
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        } catch (Exception e) {
            log.error("Elasticsearch search failed — returning empty results", e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    private Page<ProductDocument> doSearch(String query, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, String tag, Pageable pageable) {

        Page<ProductDocument> results;
        if (query != null && !query.isBlank()) {
            results = esRepository.fullTextSearch(query, pageable);
        } else if (categoryId != null) {
            results = esRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        } else if (tag != null && !tag.isBlank()) {
            results = esRepository.findByTagsAndIsActiveTrue(tag, pageable);
        } else {
            results = esRepository.findByIsActiveTrue(pageable);
        }

        // Apply in-document price, stock, and tag filters (Elasticsearch handles text/category;
        // the rest are filtered here to avoid complex native query construction
        // while keeping circuit-breaker protection over the entire search path).
        if (minPrice == null && maxPrice == null && (inStock == null || !inStock) && (tag == null || tag.isBlank())) {
            return results;
        }

        List<ProductDocument> filtered = results.stream()
                .filter(d -> minPrice == null || (d.getPrice() != null && d.getPrice() >= minPrice.doubleValue()))
                .filter(d -> maxPrice == null || (d.getPrice() != null && d.getPrice() <= maxPrice.doubleValue()))
                .filter(d -> inStock == null || !inStock || Boolean.TRUE.equals(d.getInStock()))
                .filter(d -> tag == null || tag.isBlank() || (d.getTags() != null && d.getTags().contains(tag)))
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public void indexProduct(Product product) {
        try {
            elasticsearchCircuitBreaker.executeRunnable(() ->
                    esRepository.save(toDocument(product)));
            log.debug("Indexed product {} in Elasticsearch", product.getId());
        } catch (CallNotPermittedException e) {
            log.warn("Elasticsearch circuit breaker open — skipping index for product {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to index product {} in Elasticsearch", product.getId(), e);
        }
    }

    @Override
    public void deleteFromIndex(Long productId) {
        try {
            elasticsearchCircuitBreaker.executeRunnable(() ->
                    esRepository.deleteById(String.valueOf(productId)));
            log.debug("Deleted product {} from Elasticsearch index", productId);
        } catch (CallNotPermittedException e) {
            log.warn("Elasticsearch circuit breaker open — skipping index delete for product {}", productId);
        } catch (Exception e) {
            log.error("Failed to delete product {} from Elasticsearch index", productId, e);
        }
    }

    @Override
    public void reindexAll() {
        log.info("Starting full product re-index");
        try {
            elasticsearchCircuitBreaker.executeRunnable(() -> {
                esRepository.deleteAll();
                List<Product> products = productRepository.findByIsActiveTrue();
                List<ProductDocument> docs = products.stream().map(this::toDocument).toList();
                esRepository.saveAll(docs);
                log.info("Re-indexed {} products", docs.size());
            });
        } catch (CallNotPermittedException e) {
            log.warn("Elasticsearch circuit breaker open — re-index aborted");
            throw new IllegalStateException("Re-index unavailable: Elasticsearch circuit breaker is open");
        } catch (Exception e) {
            log.error("Full re-index failed", e);
            throw new IllegalStateException("Re-index failed: " + e.getMessage(), e);
        }
    }

    private ProductDocument toDocument(Product product) {
        boolean inStock = product.getStockQuantity() != null && product.getStockQuantity() > 0;
        return ProductDocument.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
                .discountPrice(product.getDiscountPrice() != null ? product.getDiscountPrice().doubleValue() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .inStock(inStock)
                .stockQuantity(product.getStockQuantity())
                .isActive(Boolean.TRUE.equals(product.getIsActive()))
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .tags(product.getTags() == null ? List.of()
                        : product.getTags().stream().map(t -> t.getName()).toList())
                .build();
    }
}
