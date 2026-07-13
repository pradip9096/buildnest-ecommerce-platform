---
title: "A JPQL `IN` Clause Bound to an Empty Collection Parameter Throws — Pass a Sentinel Value Instead"
category: technical
tags: [jpa, hibernate, jpql, in-clause, empty-collection, optional-filter-pattern]
keywords: [param IN emptyList throws, IllegalArgumentException empty parameter list, t.id IN tagIds, sentinel value no match, optional collection filter]
source_conversations: ["Session 2026-07-13, issue #84"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "Hibernate/JPQL rejects binding an empty Collection to an :param used in an IN clause (IllegalArgumentException at query execution, not compile time) — there is no implicit 'never matches' semantics the way an empty IN list has in some SQL dialects, so any caller-supplied list that can legitimately be empty (e.g. 'products sharing any of this product's tags' when the product has zero tags) must never be passed through directly"
impact: medium — would surface as a runtime IllegalArgumentException the first time a real record has no matching child rows (e.g. the first tagless product), not caught by a test fixture that always sets up at least one tag/category/etc.
related_lessons:
  - docs/wiki/learned-lessons/jpql-implicit-singlevalued-path-in-where-acts-as-inner-join-even-under-param-is-null-guard.md
---

# A JPQL `IN` Clause Bound to an Empty Collection Parameter Throws — Pass a Sentinel Value Instead

## Problem

`ProductRepository.findRelatedProducts` (PROD-04, #84) ranks related products by shared tags:

```java
@Query("""
        SELECT p FROM Product p
        WHERE p.id <> :productId
        AND (p.category.id = :categoryId OR EXISTS (SELECT 1 FROM p.tags t WHERE t.id IN :tagIds))
        ...
        """)
List<Product> findRelatedProducts(@Param("productId") Long productId,
        @Param("categoryId") Long categoryId, @Param("tagIds") List<Long> tagIds, Pageable pageable);
```

`tagIds` is built from the source product's own tags — `product.getTags().stream().map(ProductTag::getId).toList()`.
A product with zero tags produces an empty `List`. Binding that empty list to the `:tagIds`
parameter throws `IllegalArgumentException` (or an equivalent Hibernate parameter-binding
error) at query execution — an `IN` clause with no values is invalid JPQL/SQL, not a silent
"matches nothing" no-op the way it can behave in some raw SQL dialects with special-cased empty
`IN` handling.

Unlike the sibling lesson on `:param IS NULL OR <implicit-join>`, this isn't about join
semantics — it's a parameter-binding validity error that happens regardless of whether the
surrounding boolean logic would have short-circuited around the clause at the SQL level. JPQL
still has to bind a syntactically valid `IN (...)` before evaluating anything.

## Rule

Never bind a caller-derived collection directly into a JPQL/HQL `:param IN (...)` clause without
first checking it for emptiness. If the collection can legitimately be empty (e.g. "products
sharing any of this product's tags" for a tagless product), substitute a sentinel value
guaranteed not to match any real row before binding:

```java
List<Long> tagIds = source.getTags().stream().map(ProductTag::getId).toList();
if (tagIds.isEmpty()) {
    tagIds = List.of(-1L); // no real ProductTag id will ever be -1
}
```

This preserves the intended "no tags to match against" semantics (the `EXISTS` subquery
correctly returns no rows) without touching query text or introducing a second query path for
the empty case. Verify with a unit/repository test that specifically exercises the empty-input
case — a fixture that always attaches at least one tag/category will never catch this, the same
blind spot the sibling implicit-join lesson describes.
