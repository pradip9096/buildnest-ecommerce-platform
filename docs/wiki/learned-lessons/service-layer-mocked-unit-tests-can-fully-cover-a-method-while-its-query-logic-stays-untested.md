---
title: "A Service Method's Unit Tests Can Look Fully Covered While the Query That Actually Implements Its Acceptance Criteria Has Zero Real-Data Test Coverage"
category: testing
tags: [test-coverage-gap, mocking, jpql, acceptance-criteria, data-jpa-test, repository-testing]
keywords: [mocked repository proves nothing about query correctness, acceptance criteria enforced at wrong layer, DataJpaTest missing for new query, unit test parameter pass-through only, coverage percentage vs coverage of the right thing]
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "when a new repository method's acceptance criteria (ranking, filtering, exclusion rules) are implemented entirely inside a JPQL query, a service-layer unit test that mocks the repository can only ever verify that the correct parameters were passed to that method -- it cannot verify the query itself does what the parameters imply, since the mock returns whatever the test tells it to regardless of the real query's correctness"
impact: medium — #84's PR (#370) shipped with full unit-test coverage (2 passing tests, CI green, code review complete) for a method whose actual acceptance criteria (same-category-first ranking, source exclusion, inactive/out-of-stock exclusion) were 100% implemented in a JPQL query with zero real-data test coverage; the gap was only caught by an unrelated question ("how do we determine test scenarios") prompting a review of which layer each acceptance criterion was actually enforced at
related_lessons:
  - docs/wiki/learned-lessons/testing-cacheable-proxy-behavior-needs-cache-type-override-and-has-a-scope-limit.md
  - docs/wiki/learned-lessons/jpql-explicit-join-plus-entitygraph-collection-breaks-distinct-pagination.md
---

# A Service Method's Unit Tests Can Look Fully Covered While the Query That Actually Implements Its Acceptance Criteria Has Zero Real-Data Test Coverage

## Problem

Issue #84 (related-products recommendation) required: same-category matches ranked ahead of
shared-tag matches, source product excluded, inactive/out-of-stock products excluded. All of this
logic was written entirely inside `ProductRepository.findRelatedProducts`'s JPQL query — the
service method (`ProductServiceImpl.getRelatedProducts`) does nothing but build the query's
parameters (category ID, tag IDs, a sentinel for "no tags") and delegate.

`ProductServiceImplTest` (Mockito-mocked `ProductRepository`) had two tests: one verifying the
right `categoryId`/`tagIds` were captured and passed to `findRelatedProducts`, one verifying the
no-tags sentinel value. Both passed. CI was green. Code review (including a thorough self-review
pass covering CheckStyle, SonarCloud, and a `@Cacheable` proxy-behavior integration test) treated
the feature as adequately tested.

But neither test — nor anything else added during that review — ever exercised the actual JPQL
query against real data. A repository-level `ProductRepositoryTest` (`@DataJpaTest`, H2) already
existed in the codebase for other methods, and had zero coverage of `findRelatedProducts`. This
meant every one of #84's real acceptance criteria (ranking order, exclusions) was completely
unverified — the mocked unit tests proved only that the *service* correctly forwards parameters,
never that the *query* correctly acts on them. A query with an inverted ranking `CASE` expression,
a missing exclusion clause, or a typo'd column reference would have passed both existing tests
without any failure, since the mock's return value is whatever the test hardcodes, independent of
what the real query would actually return.

This gap wasn't found by a systematic check — it surfaced only when an unrelated conversational
question ("how do we determine which test scenarios are needed") prompted mapping each acceptance
criterion back to the layer that actually implements it, revealing that layer had never been
tested at all.

## Rule

When a change's acceptance criteria are implemented inside a query (JPQL/SQL/native), a stored
procedure, or any other logic that a service-layer mock cannot see through:

1. **Explicitly map each acceptance criterion to the layer that enforces it**, not just the layer
   that's easiest to unit test. If the criterion's logic lives in a `@Query` string, a unit test
   mocking that repository method proves parameter pass-through only — it is not evidence the
   criterion is met.
2. **Add a real-data test at that layer** (`@DataJpaTest` for JPA/Hibernate queries, or the
   equivalent for the persistence technology in use) that persists genuine rows and asserts on the
   actual result set — ranking order, exclusion, filtering — not just on what parameters were
   captured.
3. **Do not treat "tests exist and pass" as equivalent to "the acceptance criteria are tested."**
   A green CI run and 100% of a method's own lines covered by unit tests can coexist with zero
   coverage of the actual business rule, if that rule lives one layer down. Coverage percentage
   is a proxy for "code was executed," not "the thing that matters was verified" — the same
   distinction already documented for `@Cacheable` proxy behavior (see the related lesson): a
   test can be green and observe nothing meaningful.
4. When reviewing a PR (self-review or otherwise) that adds a new query with acceptance-criteria
   logic embedded in it, explicitly check whether a repository/data-layer test exists for that
   specific new query — an existing `ProductRepositoryTest`-style file with unrelated methods
   already tested is not evidence the new one is covered; check the specific method name.
