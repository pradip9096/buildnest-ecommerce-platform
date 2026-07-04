---
title: "@AllArgsConstructor + Positional Test Construction Breaks on Every New Field"
category: testing
tags: [lombok, allargsconstructor, entity, dto, test-fragility]
keywords: [AllArgsConstructor compile error, constructor is undefined, positional constructor test, Product entity new field, CreateProductRequest constructor]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
related_lessons: []
---

# @AllArgsConstructor + Positional Test Construction Breaks on Every New Field

## Problem

Adding a single `isFeatured` field to `Product` (entity, `@AllArgsConstructor`) and `CreateProductRequest` (DTO, `@AllArgsConstructor`) — in support of a scoped, small feature (a home-page "featured products" flag) — broke compilation in two unrelated test files:

- `ProductTest.testProductConstructorAndGetters` — called `new Product(10L, "Test Product", ..., true, now, now)` positionally; the new field inserted between `isActive` and `createdAt` shifted every argument after it, giving `Unresolved compilation problem: constructor Product(...) is undefined`.
- `AdminProductControllerTest` — three call sites used `new CreateProductRequest("name", "desc desc", ..., "http://image")` positionally; same shift, same compile failure.

Neither failure was caught until running the full test suite — `tsc`/type-level backend equivalents (Lombok-generated constructors) don't get checked incrementally the way `mvn test -Dtest=<SpecificClass>` output suggests; the fix only surfaced when running everything.

## Why this recurs

`@AllArgsConstructor` generates a constructor whose parameter order is the *declaration order of fields in the class*. Any field addition anywhere except the very end of the class shifts every positional call site after that point. This is invisible at the call site until compilation — there's no partial/keyword-argument safety net in Java the way there is in some other languages.

## Fix applied each time

Insert the new argument at the correct position in every positional call site (matching exact field declaration order — verify by reading the entity/DTO source, don't guess), then re-run the full suite to confirm no other call sites were missed.

## Recommendation for this codebase going forward

- For entities/DTOs that are extended somewhat regularly (`Product`, `CreateProductRequest`, and likely similar catalog/order DTOs), prefer a builder pattern (Lombok `@Builder`) over relying on the all-args constructor in tests — builders are immune to field-order shifts since arguments are named.
- If `@AllArgsConstructor` is kept for framework/serialization reasons, avoid using the positional constructor directly in new tests — construct via no-args + setters (already the dominant pattern in this codebase's tests) instead, which is what most existing tests already do and why only a minority of call sites broke.
- After adding a field to any `@AllArgsConstructor` class, grep for `new <ClassName>(` across `src/test` before considering the change done — this is a cheap, mechanical check that would have caught both breakages before running the full suite.
