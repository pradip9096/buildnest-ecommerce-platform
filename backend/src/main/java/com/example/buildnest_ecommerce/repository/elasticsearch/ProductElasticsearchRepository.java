package com.example.buildnest_ecommerce.repository.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for product full-text search (SRCH-01, #74).
 */
@Repository
public interface ProductElasticsearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * Multi-field full-text search across name (boosted) and description,
     * restricted to active products.
     */
    @Query("""
            {
              "bool": {
                "must": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": ["name^3", "description", "categoryName"],
                      "type": "best_fields",
                      "fuzziness": "AUTO"
                    }
                  }
                ],
                "filter": [
                  { "term": { "isActive": true } }
                ]
              }
            }
            """)
    Page<ProductDocument> fullTextSearch(String query, Pageable pageable);

    Page<ProductDocument> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<ProductDocument> findByIsActiveTrue(Pageable pageable);

    void deleteByProductId(String productId);
}
