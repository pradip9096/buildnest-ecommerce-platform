---
title: PIT Mutation Testing — Surviving Mutation Patterns and Fixes
category: testing
tags: [pit, mutation-testing, junit5, assertthrows, argumentcaptor, archunit, ternary, removeconditionalmutator]
keywords: [survived mutation, lambda null return, setter removal, orElseThrow, NPE, ImplTest, targetTests, ternary sentinel, isNull assertion]
source_conversations: [Session 2026-07-01, Session 2026-07-22]
last_updated: 2026-07-22
confidence: high
evidence_strength: strong
root_cause: "four independent assertion-weakness patterns (asserting exception type instead of message, asserting a DTO that hides a mutated field, a naming-convention filter silently excluding a test class, and a null-control-value ternary assertion) each let a real mutation survive PIT without the test genuinely exercising the mutated behavior"
impact: medium — mutation coverage silently understated actual test quality, masking gaps a coverage-percentage metric alone wouldn't reveal; blocked CI's PIT gate (76% < 77% threshold) on #554
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

---

## Pattern 4 — A ternary's untested branch survives when the control value happens to equal both branches' output

### Problem

```java
notificationService.sendSellerVerificationDecision(
        email, businessName, approved, approved ? null : rejectionReason);
```

PIT mutates the `approved` check so the ternary always evaluates the `rejectionReason` branch (`RemoveConditionalMutator_EQUAL_ELSE`). A test for the `approved == true` path called with `rejectionReason = null` (the caller had no reason to pass one — it's an approval) asserts `isNull()` on the fourth argument. The mutant also produces `null` here, since the untaken branch's *input* was null anyway — the assertion can't distinguish "the ternary correctly suppressed a non-null value" from "the ternary was never really exercised because there was nothing to suppress." The mutation survives even though the test superficially covers both branches (`approved=true`, `approved=false`).

### Fix

Pass a distinguishable, non-null sentinel into the untaken branch's argument even when the caller wouldn't realistically pass one, so the assertion only passes if the ternary actually discards it:

```java
// approved=true path — rejectionReason passed as a non-null sentinel value
// specifically so isNull() only passes if the ternary actually suppresses it
SellerResponseDTO result = sellerService.updateVerificationStatus(
        10L, "VERIFIED", "ignored on approval");
...
verify(notificationService).sendSellerVerificationDecision(
        eq(email), eq(businessName), eq(true), isNull());
```

Generalizes beyond this repo: any `assertThat(x).isNull()` (or `isEqualTo(y)`) is only a real assertion about a conditional's behavior if the fixture supplies a value that would visibly differ between the mutated and unmutated code paths. If the untaken branch's value coincides with the null/default the mutant also produces, the assertion is a tautology with respect to that mutant.
