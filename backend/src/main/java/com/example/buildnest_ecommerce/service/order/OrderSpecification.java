package com.example.buildnest_ecommerce.service.order;

import com.example.buildnest_ecommerce.model.entity.Order;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> withFilters(
            Order.OrderStatus status,
            Long userId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        return (root, query, cb) -> {
            // Eager-join user to avoid N+1 on list view
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("user", JoinType.LEFT);
            }

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
