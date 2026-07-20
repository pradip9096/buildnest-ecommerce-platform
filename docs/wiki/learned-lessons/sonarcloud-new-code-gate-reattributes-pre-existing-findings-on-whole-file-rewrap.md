---
title: SonarCloud's "New Code" Quality Gate Is Git-Blame-Based — a Full-File Line-Wrap Reattributes Every Pre-Existing Finding on That File as New
category: technical
tags: [sonarcloud, checkstyle, git-blame, quality-gate, ci]
keywords: [new_security_rating, sonar.qualitygate.wait, S4502, S1192, line-length, rewrap]
source_conversations: ["#443"]
last_updated: 2026-07-20
confidence: high
evidence_strength: direct — reproduced via SonarCloud API cross-check (same rule/file, confirmed OPEN on master pre-PR) and the PR's own quality-gate failure/pass before/after the fix
related_lessons: [checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md]
root_cause: SonarCloud's PR "new code" period is defined by git blame on the changed lines, not by whether the underlying logic actually changed — a pure reformat (every line's blame now points at this commit) makes 100% of a file's existing findings count as "new", even ones with zero semantic change.
impact: medium — blocked merge on a required, branch-protection-enforced check (`Code Quality Analysis`) for a change with no actual new security/quality regression
---

## What happened

Fixing `SecurityConfig.java`'s CheckStyle line-length violations (see
[checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md](checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md)
for why touching the file at all forces this) required rewrapping nearly
every line in the file — a pure reformat, no logic change. The PR's
`SonarCloud Code Analysis` check then failed the required, branch-protected
`Code Quality Analysis` gate on `new_security_rating` (threshold `<=1`,
actual `4`). Cross-checking the flagged rules (`java:S4502` — CSRF disabled
on the monitoring-only chain; `java:S1192` — the string literal `"ADMIN"`
repeated) against SonarCloud's own API for `master` confirmed both were
already `OPEN` there, on the exact same file, just different line numbers
— nothing about them was actually introduced by this PR.

## Why it's non-obvious

SonarCloud's per-PR "new code" definition is git-blame-based: a line whose
`git blame` now points at the current commit counts as new, regardless of
whether the line's *content* is semantically identical to before. A
find-and-rewrap pass — exactly what CheckStyle's whole-file line-length
gate forces — reassigns blame for every touched line to the current
commit, so every pre-existing finding on those lines becomes, from
SonarCloud's perspective, a brand-new finding introduced by this change.
This is the same underlying shape as the CheckStyle "whole file, not diff"
gotcha, but via a different mechanism (git blame vs. a raw violation
count) — checking one doesn't warn you about the other.

## The check

Before assuming a SonarCloud PR-gate failure represents an actual
regression from your change, query the same rule against `master`:

```
gh api "https://sonarcloud.io/api/issues/search?componentKeys=<key>&branch=master&rules=<rule-id>"
```

If the same rule fires on the same file on `master` (even at a different
line number), the finding is pre-existing — confirmed via
`development-workflow.md`'s own "same failure on `master` before this
branch existed" standard for CI Failure Handling item 4, just applied via
the SonarCloud API instead of a workflow-run comparison. Fix it anyway if
cheap and genuinely correct (a real constant extraction, a documented
`@SuppressWarnings` referencing the already-existing rationale comment) —
don't just note it as "pre-existing, unrelated" and try to merge past a
branch-protection-required check, which this repo's own CI Failure
Handling rules against without confirming enforcement state first.
