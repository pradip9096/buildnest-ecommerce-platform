---
title: "CodeQL's Java autobuild Scan on a PR's Merge-Ref Can Produce Fewer Findings Than the Same Code's Post-Merge Push Scan — Don't Trust a Clean PR-Time Scan as Final"
category: technical
tags: [codeql, ci, static-analysis, build-mode-autobuild, verification]
keywords: [codeql autobuild java different results, PR merge-ref vs push scan, code-scanning analyses api, post-merge re-verification, polynomial-redos, spring-disabled-csrf-protection]
source_conversations: ["Session 2026-07-14, issue #358, PR #402"]
last_updated: 2026-07-14
confidence: high
evidence_strength: strong
root_cause: "CodeQL's build-mode: autobuild for compiled languages (Java here) extracts its semantic database by tracing an actual Maven build. What gets extracted depends on what actually compiles/runs during that specific build invocation. The PR's CI ran CodeQL against GitHub's ephemeral refs/pull/402/merge test-merge commit; the post-merge push ran it against the real squash commit on master straight after. Despite near-identical source content, the PR-time java-kotlin scan reported 0 results while the master push scan of the same logical code reported 5 real findings (4 java/polynomial-redos in ValidationUtil, 1 java/spring-disabled-csrf-protection in SecurityConfig, the latter a documented false positive). The most likely explanation is autobuild/extraction nondeterminism tied to build state (module compile order, caching) rather than a genuine code difference between the two commits."
impact: medium — if the closure had stopped at 'PR's own CI is green, 0 CodeQL findings' (the natural place to stop, since the issue's own acceptance criteria only asked to confirm the scan runs and produces real findings or a clean scan), 4 real (if low-severity) ReDoS findings and one alert needing triage would have shipped to master silently unnoticed. Only caught because development-workflow.md's closure step already mandates re-checking fresh evidence after merge, not just trusting a green PR
related_lessons:
  - docs/wiki/learned-lessons/spring-boot-run-does-not-read-env-and-checksums-drift-across-fixes.md
---

# CodeQL's Java autobuild Scan on a PR's Merge-Ref Can Produce Fewer Findings Than the Same Code's Post-Merge Push Scan — Don't Trust a Clean PR-Time Scan as Final

## Problem

#358 added a dedicated `codeql.yml` workflow scanning `java-kotlin` (`build-mode: autobuild`) and
`javascript-typescript` (`build-mode: none`). On PR #402's own CI run
(`refs/pull/402/merge`), `gh api repos/.../code-scanning/analyses` showed
`{"category":"/language:java-kotlin","results_count":0}` — a clean scan, matching the issue's
"or a clean scan" acceptance criterion. The PR was merged on that basis.

Immediately after the squash-merge, `codeql.yml`'s push-triggered run on `master` (commit
`8a1050a`, functionally the same source as the PR) produced
`{"category":"/language:java-kotlin","results_count":5}` — 4 `java/polynomial-redos` findings in
`ValidationUtil.validatePassword`'s `.*X.*` regex checks, and 1 `java/spring-disabled-csrf-protection`
finding (a false positive — it's the Swagger-only chain's deliberate, scoped `csrf.disable()`,
already documented in `spring-security.md`).

## Why this happened

`build-mode: autobuild` extracts CodeQL's semantic database by tracing an actual build (here,
Maven). What CodeQL can analyze is bounded by what the extractor actually observes compiling —
this is fundamentally different from `build-mode: none` (interpreted languages, source read
directly, no build dependency) where PR-time and push-time results should be far more consistent.
The PR's CI ran against GitHub's ephemeral `refs/pull/N/merge` test-merge commit; the push scan ran
against the real squash commit straight after. Despite near-identical file content, the build
processes were two separate invocations with no guaranteed identical intermediate state
(module compile order, any incremental/cache reuse) — the extractor evidently traced a materially
different scope of compiled code between them.

## The generalizable lesson

For any CodeQL scan using `build-mode: autobuild` (or `manual`) on a compiled language, a clean
result on a PR's own CI run is **not sufficient evidence** that the same code will scan clean once
merged — build-dependent extraction is not guaranteed deterministic across separate build
invocations, even of logically identical source. `build-mode: none` (interpreted languages) is not
known to share this risk, since there is no build step for extraction to depend on.

**How to apply:** treat CodeQL's post-merge, push-triggered scan on the target branch as the
authoritative source of truth for "did this actually come out clean" — not the PR's own scan. This
is exactly why `development-workflow.md`'s closure procedure requires re-verifying against fresh
evidence after merge rather than treating "CI was green on the PR" as sufficient: that step is not
redundant ceremony, it is what caught this specific gap. If a compiled-language CodeQL job in any
repo reports a clean PR scan, don't close the loop until the equivalent push-triggered scan on the
merge target has also been checked.

## What generalizes vs. what's repo-specific

The autobuild-nondeterminism risk and the "verify the push-scan, not just the PR-scan" rule
generalize to any repo using CodeQL's `build-mode: autobuild`/`manual` for a compiled language.
The two specific findings (ReDoS in `ValidationUtil`, the Swagger CSRF false positive) are
BuildNest-specific — tracked as #403 (ReDoS fix) and a manual dismissal-with-reason on the CSRF
alert, not part of the generalizable lesson itself.
