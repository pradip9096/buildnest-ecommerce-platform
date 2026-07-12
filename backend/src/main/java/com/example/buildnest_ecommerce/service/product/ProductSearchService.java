package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Full-text product search backed by Elasticsearch (SRCH-01, #74).
 *
 * Beans of this type are only registered when {@code elasticsearch.enabled=true};
 * callers must inject {@code Optional<ProductSearchService>} and fall back to the
 * JPA-backed search when the bean is absent.
 */
public interface ProductSearchService {

    /**
     * Full-text search with optional category / price / stock filters.
     * Returns a relevance-ordered page of ProductDocuments.
     */
    Page<ProductDocument> search(String query, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, String tag, Pageable pageable);

    /** Index (or re-index) a single product. */
    void indexProduct(Product product);

    /** Remove a product from the search index. */
    void deleteFromIndex(Long productId);

    /** Full re-index: clears the index and re-indexes every active product. */
    void reindexAll();
}
