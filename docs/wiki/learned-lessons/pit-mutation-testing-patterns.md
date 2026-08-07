---
title: PIT Mutation Testing — Surviving Mutation Patterns and Fixes
category: testing
tags: [pit, mutation-testing, junit5, assertthrows, argumentcaptor, archunit, ternary, removeconditionalmutator]
keywords: [survived mutation, lambda null return, setter removal, orElseThrow, NPE, ImplTest, targetTests, ternary sentinel, isNull assertion]
source_conversations: [Session 2026-07-01, Session 2026-07-22, #88]
last_updated: 2026-08-07
confidence: high
evidence_strength: strong
root_cause: "five independent assertion-weakness patterns (asserting exception type instead of message, asserting a DTO that hides a mutated field, a naming-convention filter silently excluding a test class, a null-control-value ternary assertion, and — project-wide — new/modified branches anywhere in targetClasses scope going unasserted) each let a real mutation survive PIT without the test genuinely exercising the mutated behavior"
impact: medium — mutation coverage silently understated actual test quality, masking gaps a coverage-percentage metric alone wouldn't reveal; blocked CI's PIT gate (76% < 77% threshold) on #554 and again on #88 (76%→77%, required fixes in two different classes, one of them only modified, not newly added, by the PR)
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

---

## Pattern 5 — The PIT gate is project-wide/aggregate; a CI failure names only the score, never the class, and a new PR can fail it via code the PR only *touched*, not just code it *added*

### Problem

`mvnw verify`'s PIT execution (`pit-report`, bound to the `verify` phase, scoped to `targetClasses = service.*, util.*`) computes one aggregate mutation score across the whole project and fails the build with only `Mutation score of N is below threshold of M` — no per-class or per-package breakdown in the CI log. This makes the failure look uninformative, but the real cause is always attributable: any code added or *modified* within `targetClasses` since the last time the gate passed contributes new mutants to the same denominator. On #88, this happened twice in the same PR: the new `ReturnServiceImpl` class was the obvious first suspect (55% own-class mutation coverage), but after fixing it the aggregate score barely moved (76.3%→76.3%, same rounded value) — because three *pre-existing* methods in `OrderServiceImpl` that the PR modified (adding a `deliveredAt`-setting conditional branch to each) also picked up new, unkilled mutants that no existing test exercised, since no prior test had a reason to assert a field the PR had just introduced.

### Fix

1. Reproduce the failure locally first — `./mvnw clean verify -Dcheckstyle.skip=true -Dspotbugs.skip=true -Dpmd.skip=true -Ddependency-check.skip=true` (skip the other slow/network-dependent gates; PIT itself still takes ~5-7 minutes for a project this size). This is necessary because the CI log gives no more detail than the console summary already shown here.
2. Open `target/pit-reports/index.html` (generated after the run, whether the goal itself failed the build or not) and sort by survived-mutant count per package to find where the shortfall actually concentrates — don't assume it's only in the class the PR's diff is "about."
3. **Check every `targetClasses`-scoped file the PR's diff touches, not just the files it created.** A `git diff --stat` against the PR's base branch is the list to check — a one-line conditional added to an existing, previously-well-tested method is exactly the shape that slips through, because the surrounding class's high existing score gives no signal that the *new* branch specifically is uncovered.
4. For each survived mutant, open that class's own `<ClassName>.java.html` report and read the `<p class='SURVIVED'>...</p>` list directly — it names the exact line, method, and mutation type (e.g. "removed call to X::setY", "removed conditional - replaced equality check with false"), which is enough to write the missing assertion or test case without needing to guess.
5. Some survived mutants are effectively equivalent (e.g. a setter call whose value duplicates a field initializer already run moments earlier) and cannot be killed without changing the production code's own structure — don't chase 100% on a single class; fixing the killable majority is normally enough to move the aggregate back over the threshold, as it was here (74%→77%, +21 kills across two classes was sufficient).

Generalizes beyond this repo: any project-wide, ratcheted quality gate (mutation score, aggregate coverage, aggregate duplication) can be tipped over its threshold by an in-scope *modification* to old code, not only by new code — the "what did this PR add" mental model undercounts the actual diff surface a project-wide gate evaluates.
