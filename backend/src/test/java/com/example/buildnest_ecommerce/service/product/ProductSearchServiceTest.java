package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ProductElasticsearchRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductSearchServiceImpl} (SRCH-01/SRCH-02, #74/#75).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchServiceImpl unit tests")
class ProductSearchServiceTest {

    @Mock private ProductElasticsearchRepository esRepository;
    @Mock private ProductRepository productRepository;

    private ProductSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        CircuitBreaker cb = CircuitBreaker.of("test", CircuitBreakerConfig.ofDefaults());
        service = new ProductSearchServiceImpl(esRepository, productRepository, cb);
    }

    private ProductDocument doc(String id, String name) {
        return ProductDocument.builder()
                .id(id).name(name).price(100.0).isActive(true).inStock(true).build();
    }

    private Product product(Long id, String name) {
        Product p = new Product();
        p.setId(id); p.setName(name); p.setPrice(new BigDecimal("100.00"));
        setStock(p, 10);
        p.setIsActive(true); p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private void setStock(Product p, Integer quantity) {
        if (quantity == null) {
            p.setInventory(null);
            return;
        }
        Inventory inventory = new Inventory();
        inventory.setQuantityInStock(quantity);
        p.setInventory(inventory);
    }

    @Test
    @DisplayName("search with query — delegates to fullTextSearch")
    void search_withQuery_delegatesToFullText() {
        PageRequest pr = PageRequest.of(0, 10);
        Page<ProductDocument> expected = new PageImpl<>(List.of(doc("1", "cement")));
        when(esRepository.fullTextSearch("cement", pr)).thenReturn(expected);

        Page<ProductDocument> result = service.search("cement", null, null, null, null, null, pr);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(esRepository).fullTextSearch("cement", pr);
    }

    @Test
    @DisplayName("search without query — delegates to findByIsActiveTrue")
    void search_noQuery_returnsAllActive() {
        PageRequest pr = PageRequest.of(0, 10);
        Page<ProductDocument> expected = new PageImpl<>(List.of(doc("1", "tiles"), doc("2", "paint")));
        when(esRepository.findByIsActiveTrue(pr)).thenReturn(expected);

        Page<ProductDocument> result = service.search(null, null, null, null, null, null, pr);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("search with categoryId — delegates to findByCategoryId")
    void search_withCategory_filtersByCategory() {
        PageRequest pr = PageRequest.of(0, 10);
        when(esRepository.findByCategoryIdAndIsActiveTrue(5L, pr))
                .thenReturn(new PageImpl<>(List.of(doc("3", "tile"))));

        Page<ProductDocument> result = service.search(null, 5L, null, null, null, null, pr);

        assertThat(result.getContent()).hasSize(1);
        verify(esRepository).findByCategoryIdAndIsActiveTrue(5L, pr);
    }

    @Test
    @DisplayName("search with tag (no query/category) — delegates to findByTagsAndIsActiveTrue")
    void search_withTag_filtersByTag() {
        PageRequest pr = PageRequest.of(0, 10);
        ProductDocument tagged = ProductDocument.builder().id("4").price(100.0).isActive(true)
                .inStock(true).tags(List.of("eco-friendly")).build();
        when(esRepository.findByTagsAndIsActiveTrue("eco-friendly", pr))
                .thenReturn(new PageImpl<>(List.of(tagged)));

        Page<ProductDocument> result = service.search(null, null, null, null, null, "eco-friendly", pr);

        assertThat(result.getContent()).hasSize(1);
        verify(esRepository).findByTagsAndIsActiveTrue("eco-friendly", pr);
    }

    @Test
    @DisplayName("search — blank tag string (not null) falls back to findByIsActiveTrue and skips the tag filter")
    void search_blankTagString_fallsBackToActiveSearchAndSkipsFilter() {
        PageRequest pr = PageRequest.of(0, 10);
        when(esRepository.findByIsActiveTrue(pr))
                .thenReturn(new PageImpl<>(List.of(doc("1", "cement"))));

        Page<ProductDocument> result = service.search(null, null, null, null, null, "   ", pr);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(esRepository).findByIsActiveTrue(pr);
        verify(esRepository, never()).findByTagsAndIsActiveTrue(any(), any());
    }

    @Test
    @DisplayName("tag filter applied post-query when a text query is also present")
    void search_withQueryAndTag_filtersDocumentsByTag() {
        PageRequest pr = PageRequest.of(0, 10);
        ProductDocument tagged = ProductDocument.builder().id("1").price(100.0).isActive(true)
                .inStock(true).tags(List.of("eco-friendly")).build();
        ProductDocument untagged = ProductDocument.builder().id("2").price(100.0).isActive(true)
                .inStock(true).tags(List.of("clearance")).build();
        when(esRepository.fullTextSearch("cement", pr))
                .thenReturn(new PageImpl<>(List.of(tagged, untagged)));

        Page<ProductDocument> result = service.search("cement", null, null, null, null, "eco-friendly", pr);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1");
    }

    @Test
    @DisplayName("price filter applied post-query")
    void search_withPriceRange_filtersDocuments() {
        PageRequest pr = PageRequest.of(0, 10);
        ProductDocument cheap = ProductDocument.builder().id("1").price(50.0).isActive(true).inStock(true).build();
        ProductDocument expensive = ProductDocument.builder().id("2").price(500.0).isActive(true).inStock(true).build();
        when(esRepository.fullTextSearch("paint", pr))
                .thenReturn(new PageImpl<>(List.of(cheap, expensive)));

        Page<ProductDocument> result = service.search("paint", null,
                new BigDecimal("100"), new BigDecimal("1000"), null, null, pr);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("2");
    }

    @Test
    @DisplayName("indexProduct — saves document to ES repository")
    void indexProduct_savesDocument() {
        Product p = product(10L, "bricks");
        Category cat = new Category(); cat.setId(1L); cat.setName("Materials");
        p.setCategory(cat);

        service.indexProduct(p);

        verify(esRepository).save(argThat(d ->
                "10".equals(d.getId()) && "bricks".equals(d.getName()) && "Materials".equals(d.getCategoryName())));
    }

    @Test
    @DisplayName("deleteFromIndex — deletes by string id")
    void deleteFromIndex_deletesById() {
        service.deleteFromIndex(42L);
        verify(esRepository).deleteById("42");
    }

    @Test
    @DisplayName("reindexAll — clears index and re-saves all active products")
    void reindexAll_clearsAndReindexes() {
        Product p1 = product(1L, "cement"); Product p2 = product(2L, "tile");
        when(productRepository.findByIsActiveTrue()).thenReturn(List.of(p1, p2));

        service.reindexAll();

        verify(esRepository).deleteAll();
        verify(esRepository).saveAll(argThat(docs -> {
            List<ProductDocument> list = (List<ProductDocument>) docs;
            return list.size() == 2;
        }));
    }

    // ── search — circuit breaker and exception paths ─────────────────────────

    @Test
    @DisplayName("search — circuit breaker OPEN returns empty page without throwing")
    void search_circuitBreakerOpen_returnsEmptyPage() {
        CircuitBreaker openBreaker = CircuitBreaker.of("test",
                CircuitBreakerConfig.custom().minimumNumberOfCalls(1).failureRateThreshold(1).build());
        openBreaker.transitionToOpenState();
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(esRepository, productRepository, openBreaker);

        Page<ProductDocument> result = svc.search("cement", null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(esRepository, never()).fullTextSearch(any(), any());
    }

    @Test
    @DisplayName("search — repository throws Exception returns empty page without throwing")
    void search_repositoryThrows_returnsEmptyPage() {
        when(esRepository.fullTextSearch(any(), any()))
                .thenThrow(new RuntimeException("ES cluster unreachable"));

        Page<ProductDocument> result = service.search("tile", null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    // ── doSearch — query routing edge cases ──────────────────────────────────

    @Test
    @DisplayName("search — blank query string falls back to findByIsActiveTrue (not fullTextSearch)")
    void search_blankQuery_fallsBackToActiveSearch() {
        PageRequest pr = PageRequest.of(0, 10);
        when(esRepository.findByIsActiveTrue(pr))
                .thenReturn(new PageImpl<>(List.of(doc("1", "cement"))));

        Page<ProductDocument> result = service.search("   ", null, null, null, null, null, pr);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(esRepository).findByIsActiveTrue(pr);
        verify(esRepository, never()).fullTextSearch(any(), any());
    }

    @Test
    @DisplayName("search — blank query with categoryId falls back to findByCategoryId")
    void search_blankQueryWithCategory_fallsBackToCategorySearch() {
        PageRequest pr = PageRequest.of(0, 10);
        when(esRepository.findByCategoryIdAndIsActiveTrue(3L, pr))
                .thenReturn(new PageImpl<>(List.of(doc("2", "tile"))));

        service.search("  ", 3L, null, null, null, null, pr);

        verify(esRepository).findByCategoryIdAndIsActiveTrue(3L, pr);
        verify(esRepository, never()).fullTextSearch(any(), any());
    }

    // ── doSearch — inStock filter ─────────────────────────────────────────────

    @Test
    @DisplayName("search — inStock=true excludes out-of-stock documents")
    void search_inStockTrue_excludesOutOfStockDocs() {
        PageRequest pr = PageRequest.of(0, 10);
        ProductDocument inStockDoc = ProductDocument.builder().id("1").price(100.0).inStock(true).build();
        ProductDocument outOfStockDoc = ProductDocument.builder().id("2").price(100.0).inStock(false).build();
        when(esRepository.findByIsActiveTrue(pr))
                .thenReturn(new PageImpl<>(List.of(inStockDoc, outOfStockDoc)));

        Page<ProductDocument> result = service.search(null, null, null, null, true, null, pr);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1");
    }

    // ── indexProduct — circuit breaker and exception paths ───────────────────

    @Test
    @DisplayName("indexProduct — circuit breaker OPEN does not throw")
    void indexProduct_circuitBreakerOpen_doesNotThrow() {
        CircuitBreaker openBreaker = CircuitBreaker.of("test",
                CircuitBreakerConfig.custom().minimumNumberOfCalls(1).failureRateThreshold(1).build());
        openBreaker.transitionToOpenState();
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(esRepository, productRepository, openBreaker);

        assertThatCode(() -> svc.indexProduct(product(1L, "brick"))).doesNotThrowAnyException();
        verify(esRepository, never()).save(any());
    }

    @Test
    @DisplayName("indexProduct — repository throws does not propagate exception")
    void indexProduct_repositoryThrows_doesNotThrow() {
        doThrow(new RuntimeException("ES save failed")).when(esRepository).save(any());

        assertThatCode(() -> service.indexProduct(product(1L, "brick"))).doesNotThrowAnyException();
    }

    // ── toDocument — null field branches ────────────────────────────────────

    @Test
    @DisplayName("indexProduct — product with null category sets null categoryId and categoryName")
    void indexProduct_nullCategory_setsNullCategoryFields() {
        Product p = product(5L, "gravel");
        p.setCategory(null);

        service.indexProduct(p);

        verify(esRepository).save(argThat(d -> d.getCategoryId() == null && d.getCategoryName() == null));
    }

    @Test
    @DisplayName("indexProduct — product with null price and null discountPrice sets null in document")
    void indexProduct_nullPriceAndDiscount_setsNullFields() {
        Product p = product(6L, "sand");
        p.setPrice(null);
        p.setDiscountPrice(null);

        service.indexProduct(p);

        verify(esRepository).save(argThat(d -> d.getPrice() == null && d.getDiscountPrice() == null));
    }

    @Test
    @DisplayName("indexProduct — product with null stockQuantity maps to inStock=false")
    void indexProduct_nullStockQuantity_inStockFalse() {
        Product p = product(7L, "mortar");
        setStock(p, null);

        service.indexProduct(p);

        verify(esRepository).save(argThat(d -> Boolean.FALSE.equals(d.getInStock())));
    }

    @Test
    @DisplayName("indexProduct — product with stockQuantity=0 maps to inStock=false")
    void indexProduct_zeroStockQuantity_inStockFalse() {
        Product p = product(8L, "plaster");
        setStock(p, 0);

        service.indexProduct(p);

        verify(esRepository).save(argThat(d -> Boolean.FALSE.equals(d.getInStock())));
    }

    // ── deleteFromIndex — circuit breaker and exception paths ────────────────

    @Test
    @DisplayName("deleteFromIndex — circuit breaker OPEN does not throw")
    void deleteFromIndex_circuitBreakerOpen_doesNotThrow() {
        CircuitBreaker openBreaker = CircuitBreaker.of("test",
                CircuitBreakerConfig.custom().minimumNumberOfCalls(1).failureRateThreshold(1).build());
        openBreaker.transitionToOpenState();
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(esRepository, productRepository, openBreaker);

        assertThatCode(() -> svc.deleteFromIndex(99L)).doesNotThrowAnyException();
        verify(esRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteFromIndex — repository throws does not propagate exception")
    void deleteFromIndex_repositoryThrows_doesNotThrow() {
        doThrow(new RuntimeException("ES delete failed")).when(esRepository).deleteById(any());

        assertThatCode(() -> service.deleteFromIndex(99L)).doesNotThrowAnyException();
    }

    // ── reindexAll — circuit breaker and exception paths ─────────────────────

    @Test
    @DisplayName("reindexAll — circuit breaker OPEN throws IllegalStateException")
    void reindexAll_circuitBreakerOpen_throwsIllegalState() {
        CircuitBreaker openBreaker = CircuitBreaker.of("test",
                CircuitBreakerConfig.custom().minimumNumberOfCalls(1).failureRateThreshold(1).build());
        openBreaker.transitionToOpenState();
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(esRepository, productRepository, openBreaker);

        assertThatThrownBy(svc::reindexAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("circuit breaker");
    }

    @Test
    @DisplayName("reindexAll — repository throws wraps exception in IllegalStateException")
    void reindexAll_repositoryThrows_throwsIllegalState() {
        doThrow(new RuntimeException("ES cluster down")).when(esRepository).deleteAll();

        assertThatThrownBy(service::reindexAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Re-index failed");
    }
}
