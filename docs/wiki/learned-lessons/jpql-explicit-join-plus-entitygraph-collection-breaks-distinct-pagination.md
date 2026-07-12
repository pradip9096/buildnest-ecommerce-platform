---
title: "An Explicit JPQL Join on One Collection Combined With @EntityGraph Fetching a Different Collection Silently Breaks DISTINCT/Pagination Results"
category: technical
tags: [jpa, hibernate, entitygraph, jpql, pagination, distinct, many-to-many]
keywords: [entitygraph collection fetch, left join collection jpql, distinct pagination broken, cartesian product hibernate, correlated exists subquery]
source_conversations: [Session 2026-07-12, issue #83]
last_updated: 2026-07-12
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/known-table-drift-list-should-be-checked-before-writing-changesets.md
---

# An Explicit JPQL Join on One Collection Combined With @EntityGraph Fetching a Different Collection Silently Breaks DISTINCT/Pagination Results

## Problem

While adding a `tag` filter to `ProductRepository.advancedSearch` (#83), the query was written as:

```java
@Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.tags t
    WHERE ...
    AND (:tag IS NULL OR t.name = :tag)
    """)
@EntityGraph(attributePaths = { "category", "inventory", "variants" })
Page<Product> advancedSearch(..., @Param("tag") String tag, Pageable pageable);
```

A repository-level `@DataJpaTest` proved this returned **zero rows** for a product that
genuinely had the matching tag — even a raw `entityManager.createQuery` with the exact same
`LEFT JOIN p.tags t WHERE t.name = :tag` (no `@EntityGraph`, no other filters) correctly
returned 1 row. The only difference was the presence of `@EntityGraph` fetching a *different*
collection (`variants`) on the same query.

Combining an explicit `LEFT JOIN` on one collection (`tags`) with an `@EntityGraph` fetch of
another collection (`variants`) in the same query produces multiple collection joins in one
SQL statement — a landmine class already known in JPA/Hibernate (the "MultipleBagFetchException"
family of issues), except here it didn't throw an exception; it silently produced an incorrect
`DISTINCT`+in-memory-pagination result instead (Hibernate logged
`HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`).
Silent wrong results are worse than a thrown exception — nothing in CI would have caught this
without a test that actually persisted a real matching row and asserted the count.

## Fix

Replace the explicit collection join with a correlated `EXISTS` subquery instead of joining the
second collection into the main `FROM` clause at all:

```java
@Query("""
    SELECT p FROM Product p
    WHERE ...
    AND (:tag IS NULL OR EXISTS (SELECT 1 FROM p.tags t WHERE t.name = :tag))
    """)
@EntityGraph(attributePaths = { "category", "inventory", "variants" })
Page<Product> advancedSearch(...);
```

This also let `DISTINCT` be dropped entirely, since a correlated `EXISTS` doesn't multiply the
outer row set the way a join does.

## Rule

When writing a JPQL query that already carries an `@EntityGraph` fetching one collection, and a
new filter needs to test membership against a *different* collection on the same entity, prefer
a correlated `EXISTS (SELECT 1 FROM p.<collection> x WHERE ...)` subquery over an explicit
`LEFT JOIN p.<collection> x` in the main `FROM` clause. A join multiplies rows and interacts with
the `@EntityGraph`'s own collection fetch in ways that can silently corrupt `DISTINCT`+pagination
results, even when no exception is thrown. Always verify a new join-based filter with a real
`@DataJpaTest` that persists genuine matching data and asserts on the row count — don't trust
that "the SQL looks structurally right" is sufficient; this bug's generated SQL looked correct on
inspection and only failed at the result-set level.
