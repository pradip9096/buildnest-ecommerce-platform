---
title: PIT Mutation Testing — Surviving Mutation Patterns and Fixes
category: testing
tags: [pit, mutation-testing, junit5, assertthrows, argumentcaptor, archunit]
keywords: [survived mutation, lambda null return, setter removal, orElseThrow, NPE, ImplTest, targetTests]
source_conversations: [Session 2026-07-01]
last_updated: 2026-07-01
confidence: high
evidence_strength: strong
related_lessons:
  - docs/knowledge-base/project/quality-gate-ratchet-pattern.md
---

# PIT Mutation Testing — Surviving Mutation Patterns and Fixes

## Pattern 1 — Lambda null-return mutation survives `assertThrows(RuntimeException.class)`

### Problem

PIT replaces `orElseThrow(() -> new RuntimeException("msg"))` lambda body with `return null`. When the `Optional` is empty, `orElseThrow` receives `() -> null`, evaluates the supplier, and gets `null` — which `orElseThrow` then throws as a `NullPointerException`. Because `NullPointerException` is-a `RuntimeException`, `assertThrows(RuntimeException.class, ...)` still passes. The mutation survives.

### Fix

Assert the exception message to distinguish a genuine domain exception from an NPE:

```java
RuntimeException ex = assertThrows(RuntimeException.class, () ->
    service.findById(99L));
assertEquals("User not found with id: 99", ex.getMessage());
```

This fails when PIT injects `return null` because an NPE has a `null` message, not the expected string.

---

## Pattern 2 — Setter-removal mutations survive when DTO hides the field

### Problem

PIT removes a `entity.setUpdatedAt(LocalDateTime.now())` call. The test asserts the returned DTO — but the DTO doesn't expose `updatedAt`. The mutation survives because the assertion can't see what was passed to `repository.save()`.

### Fix

Capture the entity passed to `repository.save()` and assert the field directly:

```java
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(orderRepository).save(captor.capture());
assertNotNull(captor.getValue().getUpdatedAt());
```

Use this whenever PIT survives a setter call that the DTO-level assertion cannot reach.

---

## Pattern 3 — PIT silently excludes test classes that don't match `targetTests`

### Problem

PIT's `targetTests` pattern in `pom.xml` is typically `**.*ImplTest` or `**.*ServiceTest`. A test class named `OrderSpecificationTest` (no `Impl` or `Service`) is silently excluded from the mutation run. Mutations in the class under test show as "no coverage" rather than "survived", and the class appears untested even when a test file exists.

### Symptom

A `*Test.java` file exists, tests pass in Surefire, but PIT reports 0% mutation coverage for the corresponding production class.

### Fix

Enforce the naming convention with an ArchUnit test:

```java
@AnalyzeClasses(packages = "com.example.buildnest_ecommerce.service")
public class PitNamingConventionTest {

    @ArchTest
    static final ArchRule serviceTestNaming = classes()
        .that().resideInAPackage("..service..")
        .and().haveSimpleNameEndingWith("Test")
        .should().haveSimpleNameEndingWith("ImplTest")
        .orShould().haveSimpleNameEndingWith("ServiceTest");
}
```

Place this in an `architecture` package so it runs in every Surefire execution and fails the build when a misnamed test class is added.

### Alternative

Broaden `targetTests` in `pom.xml` to include plain `*Test`:

```xml
<targetTests>
    <param>com.example.buildnest_ecommerce.*</param>
</targetTests>
```

This is simpler but picks up unrelated test helpers. The ArchUnit approach enforces convention rather than relaxing the filter.
