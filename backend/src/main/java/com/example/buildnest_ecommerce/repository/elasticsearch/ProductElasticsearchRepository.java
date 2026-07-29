package com.example.buildnest_ecommerce.repository.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository
        .ElasticsearchRepository;
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

    /**
     * Same as {@link #fullTextSearch} with an additional district term
     * filter (FR-LOC-03, #563) — mirrors the existing isActive:true filter
     * shape rather than the categoryId derived-query shape, since both
     * conditions must apply together to the same full-text query.
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
                  { "term": { "isActive": true } },
                  { "term": { "districtIds": ?1 } }
                ]
              }
            }
            """)
    Page<ProductDocument> fullTextSearchByDistrict(
            String query, Long districtId, Pageable pageable);

    Page<ProductDocument> findByCategoryIdAndIsActiveTrue(
            Long categoryId, Pageable pageable);

    Page<ProductDocument> findByCategoryIdAndDistrictIdsAndIsActiveTrue(
            Long categoryId, Long districtId, Pageable pageable);

    Page<ProductDocument> findByTagsAndIsActiveTrue(
            String tag, Pageable pageable);

    Page<ProductDocument> findByTagsAndDistrictIdsAndIsActiveTrue(
            String tag, Long districtId, Pageable pageable);

    Page<ProductDocument> findByIsActiveTrue(Pageable pageable);

    Page<ProductDocument> findByDistrictIdsAndIsActiveTrue(
            Long districtId, Pageable pageable);

    void deleteByProductId(String productId);
}
