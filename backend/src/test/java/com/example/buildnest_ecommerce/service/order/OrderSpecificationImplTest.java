package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.entity.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderSpecification")
class OrderSpecificationImplTest {

    @Mock private Root<Order> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path<Object> statusPath;
    @Mock private Path<Object> userPath;
    @Mock private Path<Object> userIdPath;
    @Mock private Path<Object> createdAtPath;
    @SuppressWarnings("rawtypes")
    @Mock private Path isDeletedPath;
    @Mock private Predicate isDeletedPredicate;
    @Mock private Predicate resultPredicate;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        lenient().when(root.get("isDeleted")).thenReturn(isDeletedPath);
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("user")).thenReturn(userPath);
        lenient().when(root.get("createdAt")).thenReturn(createdAtPath);
        lenient().when(userPath.get("id")).thenReturn(userIdPath);
        lenient().when(cb.isFalse(any(Expression.class))).thenReturn(isDeletedPredicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(resultPredicate);
    }

    // ── fetch join guard ─────────────────────────────────────────────────────

    @Test
    @DisplayName("adds fetch join when query result type is not Long (entity query)")
    void withFilters_entityQuery_addsFetchJoin() {
        when(query.getResultType()).thenAnswer(inv -> Order.class);

        toSpec(null, null, null, null).toPredicate(root, query, cb);

        verify(root).fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
    }

    @Test
    @DisplayName("skips fetch join when query result type is Long (count query)")
    void withFilters_countQuery_skipsFetchJoin() {
        when(query.getResultType()).thenAnswer(inv -> Long.class);

        toSpec(null, null, null, null).toPredicate(root, query, cb);

        verify(root, never()).fetch(anyString(), any());
    }

    @Test
    @DisplayName("skips fetch join when query is null")
    void withFilters_nullQuery_skipsFetchJoin() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(root, never()).fetch(anyString(), any());
    }

    // ── isDeleted predicate ──────────────────────────────────────────────────

    @Test
    @DisplayName("always adds isDeleted=false predicate regardless of other filters")
    void withFilters_alwaysAddsIsDeletedFalse() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(cb).isFalse(any(Expression.class));
        verify(cb).and(any(Predicate[].class));
    }

    // ── optional filters ─────────────────────────────────────────────────────

    @Test
    @DisplayName("adds status predicate when status is non-null")
    void withFilters_nonNullStatus_addsStatusPredicate() {
        toSpec(Order.OrderStatus.SHIPPED, null, null, null).toPredicate(root, null, cb);

        verify(cb).equal(statusPath, Order.OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("omits status predicate when status is null")
    void withFilters_nullStatus_omitsStatusPredicate() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(cb, never()).equal(eq(statusPath), any());
    }

    @Test
    @DisplayName("adds userId predicate when userId is non-null")
    void withFilters_nonNullUserId_addsUserIdPredicate() {
        toSpec(null, 42L, null, null).toPredicate(root, null, cb);

        verify(cb).equal(userIdPath, 42L);
    }

    @Test
    @DisplayName("omits userId predicate when userId is null")
    void withFilters_nullUserId_omitsUserIdPredicate() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(cb, never()).equal(eq(userIdPath), any());
    }

    @Test
    @DisplayName("adds dateFrom predicate when dateFrom is non-null")
    void withFilters_nonNullDateFrom_addsGreaterThanOrEqualPredicate() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);

        toSpec(null, null, from, null).toPredicate(root, null, cb);

        verify(cb).greaterThanOrEqualTo(any(), eq(from));
    }

    @Test
    @DisplayName("omits dateFrom predicate when dateFrom is null")
    void withFilters_nullDateFrom_omitsGreaterThanPredicate() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(cb, never()).greaterThanOrEqualTo(any(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("adds dateTo predicate when dateTo is non-null")
    void withFilters_nonNullDateTo_addsLessThanOrEqualPredicate() {
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        toSpec(null, null, null, to).toPredicate(root, null, cb);

        verify(cb).lessThanOrEqualTo(any(), eq(to));
    }

    @Test
    @DisplayName("omits dateTo predicate when dateTo is null")
    void withFilters_nullDateTo_omitsLessThanPredicate() {
        toSpec(null, null, null, null).toPredicate(root, null, cb);

        verify(cb, never()).lessThanOrEqualTo(any(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("combines all predicates into a single AND when all filters are provided")
    void withFilters_allFiltersProvided_combinesAllPredicates() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        toSpec(Order.OrderStatus.DELIVERED, 7L, from, to).toPredicate(root, null, cb);

        verify(cb).isFalse(any(Expression.class));
        verify(cb).equal(statusPath, Order.OrderStatus.DELIVERED);
        verify(cb).equal(userIdPath, 7L);
        verify(cb).greaterThanOrEqualTo(any(), eq(from));
        verify(cb).lessThanOrEqualTo(any(), eq(to));
        verify(cb).and(any(Predicate[].class));
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Specification<Order> toSpec(Order.OrderStatus status, Long userId,
                                        LocalDateTime dateFrom, LocalDateTime dateTo) {
        return OrderSpecification.withFilters(status, userId, dateFrom, dateTo);
    }
}
