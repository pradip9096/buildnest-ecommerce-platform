---
title: "Wrapping a Javadoc @throws FQN reference across lines to satisfy CheckStyle breaks the javadoc goal"
category: technical
tags: [javadoc, checkstyle, ci, maven]
last_updated: 2026-07-25
root_cause: "CheckStyle's line-length check operates on raw text and has no opinion on javadoc grammar, so rewrapping a long @throws fully-qualified-name reference across two lines to satisfy the 80-char limit produces a token the javadoc parser cannot resolve as a type reference — it expects the FQN on one line"
impact: low — a genuine CI build failure (Build & Test, plus Quality Gates/Security Vulnerabilities cascading from the same shared Maven build), zero functional risk, caught immediately on first push
---

# Wrapping a Javadoc `@throws` FQN Reference Across Lines to Satisfy CheckStyle Breaks the `javadoc` Goal

## What happened

While adding seller-scoped inventory methods (#556), an existing long `@throws` line was
rewrapped to satisfy CheckStyle's 80-char line-length ratchet:

```java
/**
 * @throws com.example.buildnest_ecommerce.exception.
 *         InventoryException if available quantity is
 *         insufficient
 */
```

This compiled and tested fine locally (`./mvnw compile`, `./mvnw test`). Only CI's `javadoc:jar`
goal (attached to the `package` phase, same as the sibling Lombok lesson below) failed:

```
[ERROR] .../InventoryService.java:68: error: syntax error in reference
[ERROR]      * @throws com.example.buildnest_ecommerce.exception.
[ERROR]                                                          ^
```

Two other CI checks (`Quality Gates`, `Security Vulnerabilities`) failed in the same run — the
same shared-Maven-build cascade already documented in the Lombok lesson below, not independent
findings.

## Why this is non-obvious

CheckStyle's line-length rule is purely textual — it rewraps (or flags) any line over 80 chars,
including inside a javadoc comment, with no awareness that a `@throws <FQN>` tag's argument is a
single unbroken token to the javadoc parser. Splitting `com.example...exception.InventoryException`
across two comment lines looks like normal prose wrapping (and is legal for the *description* text
that follows the type), but the type reference itself must be contiguous. `./mvnw test` never runs
the `javadoc` goal, so nothing catches this locally unless `./mvnw javadoc:jar` (or `package`) is
run explicitly first.

## Rule

Never wrap a `@throws`/`@see`/`@link` type reference across lines to fix a CheckStyle line-length
violation. Instead, import the exception class and use its simple name:

```java
import com.example.buildnest_ecommerce.exception.InventoryException;

/**
 * @throws InventoryException if available quantity is insufficient
 */
```

This is almost always shorter than the FQN anyway, so it satisfies both tools at once. If a simple
name still doesn't fit, shorten the description text, not the type reference.

## Related
- [`{@link}` to a Lombok-Generated Method Fails the `javadoc` Goal, Invisible to `./mvnw test`](javadoc-link-to-lombok-generated-method-fails-only-in-ci-javadoc-goal.md)
- [CheckStyle Ratchet Counts Whole File, Not Diff — Add Javadoc Per New Method](checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md)
