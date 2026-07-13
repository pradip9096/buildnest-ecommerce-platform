---
title: Verify a GitHub Issue's Technical Premises Against the Repo Before Implementing
category: process
tags: [github, issues, ci, pitest, verification, requirements]
keywords: [issue wrong file, issue wrong metric, mutationThreshold, test strength vs mutation score, ci workflow topology, don't trust issue body]
source_conversations: [Session 2026-07-03]
last_updated: 2026-07-03
confidence: high
evidence_strength: strong
root_cause: "a self-authored issue's technical claims (which workflow file runs a step, which metric gates the build) were written from memory in an earlier session rather than re-verified against the current repo state, and both claims had since drifted or were wrong from the start"
impact: medium — would have produced code changes to the wrong file gating on the wrong metric had the premises not been checked before implementation
related_lessons:
  - docs/wiki/learned-lessons/github-issue-hygiene.md
  - docs/wiki/learned-lessons/pit-mutation-testing-patterns.md
---

# Verify a GitHub Issue's Technical Premises Against the Repo Before Implementing

## Problem

Issue #275 ("report PIT test-strength score in CI PR comments alongside JaCoCo") contained two factual errors about the codebase it was describing:

1. **Wrong file.** It said to extend the JaCoCo PR-comment step in `.github/workflows/ci.yml`. But `ci.yml`'s test step is `./mvnw -B clean test jacoco:report` — it never runs `verify`, so the `pitest-maven` plugin (bound to the `verify` phase) never executes there and `target/pit-reports/mutations.xml` never exists. PIT actually only runs in the separate `ci-cd-pipeline.yml` workflow's `integration-tests` job, via `./mvnw verify -P ci`.
2. **Wrong metric.** It said to gate the ✅/❌ indicator on **test strength** (`killed / (killed + survived)`) and called `killed / total` "the misleading number already printed in the Maven console." `pom.xml`'s own inline comment on the `pitest-maven` plugin configuration (written during an earlier issue, #277) states the opposite: `mutationThreshold` gates on **mutation score** (`killed / total generated`, including `NO_COVERAGE` mutants), and test strength is printed for visibility only and is *not* gated. This matches PIT's documented plugin semantics.

Both errors were self-authored — the issue was written by the same person requesting the work, in an earlier session, evidently from memory/assumption rather than a fresh read of the current workflow files and `pom.xml`.

## Fix

Before writing any code against an issue that makes a specific technical claim ("X runs in file Y", "the gate uses metric Z"), grep/read the actual current state:

- For "which workflow runs this" claims: read the workflow YAML directly, don't trust the issue's file reference.
- For "this metric is what gates the build" claims: check the tool's actual config (here, the `pom.xml` plugin block) and any inline comments explaining prior decisions — these are often more reliable than the issue text, since they were written closer to the point of implementation and sometimes corrected after the issue was filed.

When a discrepancy is found, do not silently "fix" the issue's premise and proceed, and do not implement it literally knowing it's wrong — stop and surface the specific contradiction (quote the conflicting evidence) so the user can confirm the correction before code changes are made. Two `AskUserQuestion` prompts were used in this session, one per discrepancy — both confirmed the repo's evidence over the issue's stated premise.

## Rule

Treat a GitHub issue body as a *hypothesis* about the codebase, not as verified fact — even (especially) issues you wrote yourself in a previous session. Re-verify file targets and metric/gate semantics against the current repo state before implementing, and flag contradictions explicitly rather than resolving them unilaterally.
