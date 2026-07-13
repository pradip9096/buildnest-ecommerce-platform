---
title: "CheckStyle's Baseline Ratchet Counts Whole-File Violations, Not Diff Lines — New Code Must Be Individually Clean, Not Just Consistent With Its Neighbors"
category: technical
tags: [checkstyle, quality-gate-ratchet, ci, javadoc, code-style]
keywords: [checkstyle maxAllowedViolations, baseline ratchet whole file count, DesignForExtension javadoc, MissingJavadocMethod new method, matching existing style still fails]
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "checkstyle:check with maxAllowedViolations runs against the whole compiled source tree on every CI run and compares the absolute violation count to a fixed ceiling — it has no concept of a diff, so a new method written in the exact same (violating) style as its sibling methods in the same file still adds new violation occurrences and can push the total over the ceiling, even though it introduces zero *new kinds* of violation"
impact: medium — blocked PR #370's merge (8338 violations vs the 8305 ceiling from #354) despite the new code being style-consistent with its surrounding file; required a second commit to fix after CI failure, costing a review/CI round-trip
related_lessons:
  - docs/wiki/learned-lessons/jpql-in-clause-rejects-empty-collection-use-a-sentinel-value.md
---

# CheckStyle's Baseline Ratchet Counts Whole-File Violations, Not Diff Lines — New Code Must Be Individually Clean, Not Just Consistent With Its Neighbors

## Problem

PR #370 (#84, related-products endpoint) added ~30 new lines across
`ProductControllerV2`, `ProductServiceImpl`, `ProductService`, `ProductRepository`, and
`CacheConfig`. The new code matched the existing style of each file exactly — e.g.
`ProductControllerV2`'s other endpoint methods also have no javadoc and already trigger
`MissingJavadocMethod`/`DesignForExtension`, and `CacheConfig`'s other cache-region blocks are
already well over 80 characters per line. The assumption was that matching an already-violating
local style wouldn't make things worse.

CI's `Code Quality Analysis` job failed anyway: `./mvnw checkstyle:check
-Dcheckstyle.maxAllowedViolations=8305` reported 8338 violations, 33 over the ratchet ceiling from
#354. The ratchet check has no diff awareness — it re-scans the entire compiled source tree and
compares the absolute count. Every new line that happens to match an existing violation pattern
(missing javadoc, a line over 80 chars, a non-final parameter) is still counted as one more
occurrence of that violation type, regardless of whether the *pattern itself* is new to the
repo or already tolerated as debt in neighboring code.

Confirmed by isolating which lines the new violations landed on (`gh run view --log-failed`
cross-referenced against the file's line numbers) — all 33 new violations mapped exactly to the
lines added by this PR, not to any pre-existing code.

## Rule

When adding new code to a file that already carries CheckStyle debt (missing javadoc, long
lines, non-final parameters), do not assume matching the file's existing (violating) style is
safe under a `maxAllowedViolations` ratchet. Only the *total* count matters, and it's a hard
ceiling, not a per-file or per-PR allowance:

1. Write genuinely clean new code — full javadoc (`@param`/`@return`/`@throws` as applicable),
   `final` parameters, ≤80-char lines — even in a file where sibling code doesn't. A complete
   javadoc block (description + tags) is what actually satisfies both `MissingJavadocMethod` and
   `DesignForExtension` simultaneously; a class doesn't need to be marked `final` to dodge
   `DesignForExtension` (and for `@Transactional`/`@Cacheable` beans it must not be — CGLIB proxy
   requirement, see the sibling lesson on final Spring beans).
2. For deeply-nested builder-chain code (e.g. a fluent Redis cache config chain) where matching
   the existing indentation makes every line exceed 80 characters, extract a small private helper
   method rather than fighting the indentation — this shrinks both the line count and the width
   per call site without touching the untouched, already-debt-carrying blocks around it.
3. Verify locally before pushing: `./mvnw -B checkstyle:check
   -Dcheckstyle.maxAllowedViolations=<current ceiling from pom.xml/CI>` and confirm the reported
   count is at or under the ceiling, rather than relying on CI to catch it after the fact.
