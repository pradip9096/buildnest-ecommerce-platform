---
title: Mirroring a sibling-precedent file closely can trip SonarCloud's new-code duplication gate
category: process
tags: [sonarcloud, duplication, ci, quality-gate, sibling-precedent, refactoring]
keywords: [new_duplicated_lines_density, sonar quality gate, sibling precedent, DRY, extract utility]
source_conversations: ["#558"]
last_updated: 2026-07-28
confidence: high
evidence_strength: single-occurrence, high-cost (blocked a merge; direct tension with an existing process rule)
related_lessons: []
root_cause: "development-workflow.md's solution-options-adr sibling-precedent check instructs mirroring an existing pattern closely when building a structurally similar sibling feature, but that same near-identical control flow is exactly what SonarCloud's Copy-Paste-Detector (CPD)-based new-code duplication gate (new_duplicated_lines_density, default max 3%) is built to flag -- the two rules pull in opposite directions with nothing reconciling them"
impact: "medium — a real merge-blocking CI failure (PR #594), not a false positive; caught only at CI time, invisible to any local unit test or manual code review"
---

# Sibling-Precedent Mirroring Can Trip SonarCloud's New-Code Duplication Gate

## What happened

Building `SellerReview` (FR-SEL-07, #558) followed `development-workflow.md`'s
`solution-options-adr` sibling-precedent check as designed: the existing `ProductReview` entity/
repository/service/controller was read and mirrored closely, scoped by `sellerId` instead of
`productId`. This produced `SellerReviewServiceImpl.getRatingDistribution()` — a rating-map-
building loop — as a near-line-for-line copy of `ProductReviewServiceImpl.getRatingDistribution()`.

PR #594's `Code Quality Analysis` job failed with:

```
QUALITY GATE STATUS: FAILED
new_duplicated_lines_density: 6.1% (max 3%)
```

Confirmed via `curl "https://sonarcloud.io/api/qualitygates/project_status?..."` — a genuine
quality-gate failure, not a flaky run or a misconfigured job (the same PR's `new_coverage`
condition also failed independently at the time, for an unrelated reason — a missing controller
test — so this wasn't a single spurious signal).

## Why this is non-obvious

Nothing in `development-workflow.md`'s sibling-precedent instruction, nor in this repo's own
`security.yml` SonarCloud wiring, flags the conflict ahead of time. The sibling-precedent check's
entire premise is "match the process/shape of a prior similar decision unless there's a stated
reason not to" — it has no built-in awareness that literal code-shape matching has its own CI
consequence once the mirrored file crosses SonarCloud's block-size threshold for its CPD engine.
An agent following the sibling-precedent instruction faithfully can walk directly into this gate
with no warning until the PR is already open.

## The fix

This took two rounds, not one — the first fix was necessary but not sufficient:

1. Extract the genuinely duplicated *logic* into a small shared utility
   (`ReviewRatingUtils.buildDistribution(List<Object[]>)`), used by both
   `ProductReviewServiceImpl` and `SellerReviewServiceImpl`. This dropped
   `new_duplicated_lines_density` from 6.1% to 5.3% — real progress, but still above the 3% gate.
2. The remaining duplication was in the *entities themselves* — `ProductReview` and `SellerReview`
   had byte-for-byte identical `rating`/`comment`/`createdAt`/`updatedAt`/`verifiedPurchase`/
   `isVisible` fields and identical `@PrePersist`/`@PreUpdate` bodies (53 duplicated lines total,
   confirmed via `curl .../api/duplications/show?key=...`). Fixed by extracting a
   `@MappedSuperclass AbstractReview` that both entities extend — JPA's purpose-built mechanism
   for exactly this pattern (shared columns across sibling entity hierarchies with no shared
   table). This dropped duplication to a passing level.

Both refactors touched the *existing* sibling file (`ProductReviewServiceImpl`/`ProductReview`),
not just the new one — rewriting only the new file would leave the old block intact for CPD to
match against.

**Gotcha inside the `@MappedSuperclass` fix**: Lombok's plain `@Builder` does **not** include
inherited fields — a subclass keeping `@Builder` after extending a `@MappedSuperclass` parent
silently drops the parent's fields from `.builder()...build()` calls (compiles fine, then the
built object silently has null rating/comment/etc. at runtime). Use `@SuperBuilder` on both the
parent and every subclass consistently instead — it's built for exactly this inheritance case
and both entities' existing `.builder()...build()` call sites in the service layer kept working
unchanged with no code changes required there.

**Don't assume one round of "extract the duplicated logic" clears a SonarCloud duplication gate**
— check the metric again after the first fix. A 6.1%→5.3% improvement is real progress, but
"progress" and "under the threshold" are different questions; re-query
`new_duplicated_lines_density` (or re-run CI) after any partial fix rather than assuming the
first extraction was sufficient.

## What to do next time

- Sibling-precedent mirroring should stop at *structure* (file layout, method names, validation
  shape, error handling) — genuinely identical *logic* blocks (not just similar shape) should be
  extracted into a shared helper from the start, rather than copy-pasted and only fixed
  reactively once SonarCloud's gate catches it.
- If a mirrored file's control-flow blocks (loops, conditionals) are byte-for-byte identical to
  the precedent's equivalent blocks (only variable names differ), that's the specific signal to
  extract rather than copy — don't wait for the gate to flag it.
- The same applies to entity *fields*, not just method bodies — two sibling JPA entities sharing
  an identical field/lifecycle-callback set are a `@MappedSuperclass` candidate from the start.
- Always re-check the actual duplication metric after a partial fix before declaring it resolved.
