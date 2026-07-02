---
title: Stale Compiled Test Classes Cause False Failures After Source Deletion
category: testing
tags: [maven, surefire, incremental-compile, target-directory, stale-artifacts]
keywords: [NoClassDefFoundError, stale class file, mvn test without clean, deleted test source still runs, target/test-classes]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/shell-pipeline-exit-code-masking.md
---

# Stale Compiled Test Classes Cause False Failures After Source Deletion

## Problem

After deleting 7 redundant `@DataJpaTest` classes (issue #255) and running `./mvnw test` (no `clean`), an unrelated test — `RedisCheckoutSessionStoreTest` — failed with:

```
java.lang.NoClassDefFoundError: com/example/buildnest_ecommerce/service/checkout/RedisCheckoutSessionStoreTest$1
Caused by: java.lang.ClassNotFoundException: ...RedisCheckoutSessionStoreTest$1
```

Investigation showed the source file `RedisCheckoutSessionStoreTest.java` no longer existed anywhere in `src/test` — it had been deleted in an earlier commit (`c7c471f`). Surefire was still picking up an orphaned compiled `.class` file (including its anonymous inner class `$1`) left over in `target/test-classes` from before that commit's `mvn clean`. Maven's incremental `test` goal does not prune `.class` files whose source was deleted; it only recompiles changed/new sources.

## Fix

Run `./mvnw clean test` (or `clean verify`) whenever a `mvn test` failure looks unrelated to the change just made, especially `NoClassDefFoundError` for a class that no longer has a matching source file. `clean` removes `target/`, forcing a full recompile that excludes orphaned classes.

## Rule

Before concluding a test failure was caused by your change, grep for the failing class's source file. If it doesn't exist, the failure is a stale-artifact false positive — run `clean test`, not a targeted rerun (targeted `-Dtest=<Class>` reruns will also fail with "no tests matching pattern" once the class is confirmed deleted, which is itself the tell).
