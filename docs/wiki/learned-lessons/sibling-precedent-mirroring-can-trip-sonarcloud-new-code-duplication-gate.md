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

Extract the genuinely duplicated logic into a small shared utility
(`ReviewRatingUtils.buildDistribution(List<Object[]>)`), used by both `ProductReviewServiceImpl`
and `SellerReviewServiceImpl`. This is not "working around" the gate — the two services really did
contain the exact same 8-line map-building block, so extracting it is a legitimate DRY fix, not a
threshold-dodge. Refactoring the *existing* sibling file (`ProductReviewServiceImpl`, not just the
new one) was necessary: only rewriting the new file would still leave the old block intact for CPD
to match against.

## What to do next time

- Sibling-precedent mirroring should stop at *structure* (file layout, method names, validation
  shape, error handling) — genuinely identical *logic* blocks (not just similar shape) should be
  extracted into a shared helper from the start, rather than copy-pasted and only fixed
  reactively once SonarCloud's gate catches it.
- If a mirrored file's control-flow blocks (loops, conditionals) are byte-for-byte identical to
  the precedent's equivalent blocks (only variable names differ), that's the specific signal to
  extract rather than copy — don't wait for the gate to flag it.
