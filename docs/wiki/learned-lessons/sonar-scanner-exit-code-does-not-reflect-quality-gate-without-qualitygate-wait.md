---
title: "SonarScanner's Own Exit Code Reflects Report Submission, Not the Quality Gate, Unless sonar.qualitygate.wait=true Is Set"
category: ci-cd
tags: [sonarcloud, sonarqube, quality-gate, ci, maven, silent-drift]
keywords: [sonar qualitygate wait, ANALYSIS SUCCESSFUL misleading, sonar:sonar exit code, quality gate not blocking, sonar-maven-plugin async submission]
source_conversations: ["#320", "PR #401"]
last_updated: 2026-07-14
confidence: high
evidence_strength: direct-repo-verification
root_cause: "sonar-maven-plugin's default behavior is fire-and-forget: the sonar:sonar goal uploads the analysis report to SonarCloud's background compute engine and returns success as soon as the upload succeeds, without waiting for or evaluating the quality gate server-side -- so a CI step invoking the bare goal can never fail on a real quality-gate violation regardless of whether a non-blocking `|| echo` wrapper is also present"
impact: medium — #320 sat open for a full milestone under the assumption that removing a non-blocking `|| echo` wrapper would be sufficient to make SonarCloud gate CI; the real fix required one additional Maven property most engineers wouldn't know to look for, since the scanner's own "ANALYSIS SUCCESSFUL" log line looks identical whether or not the gate would have failed
related_lessons:
  - docs/wiki/learned-lessons/checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md
  - docs/wiki/learned-lessons/github-actions-working-directory-default-only-applies-to-run-steps.md
---

# SonarScanner's Own Exit Code Reflects Report Submission, Not the Quality Gate, Unless `sonar.qualitygate.wait=true` Is Set

## Problem

Issue #320 asked to make BuildNest's SonarCloud CI step "fail the build on new Critical/Blocker
findings." The obvious-looking fix — remove the step's `|| echo "...non-blocking..."` fallback so
a non-zero exit code actually fails the job — would not have worked.

Pulling the live CI log for `security.yml`'s `Run Quality Analysis` step on `master` (before this
fix) showed:

```
[INFO] ANALYSIS SUCCESSFUL, you can find the results at: https://sonarcloud.io/dashboard?...
[INFO] More about the report processing at https://sonarcloud.io/api/ce/task?id=...
[INFO] SonarScanner Engine completed successfully
```

This looks like a clean, successful run — and it is, but only for one narrow claim: the analysis
report was successfully **uploaded** to SonarCloud's background compute engine. The actual quality
gate evaluation happens **asynchronously, server-side**, after the Maven process has already
exited. By default, `sonar-maven-plugin`'s `sonar:sonar` goal does not wait for that evaluation or
reflect its result in its own exit code — a project with a badly-failing quality gate produces the
exact same "ANALYSIS SUCCESSFUL" log line as a project with a perfectly clean one.

## Fix

Add `-Dsonar.qualitygate.wait=true` to the `sonar:sonar` goal invocation. This makes the Maven
goal poll SonarCloud's compute-engine task until it completes, then fail (non-zero exit) if the
quality gate result is anything other than passed.

Confirmed empirically, before vs. after, on the same project:

**Before** (bare goal, no wait flag) — log ends at `SonarScanner Engine completed successfully`,
no gate-related output at all, exits 0 regardless of gate outcome.

**After** (`sonar.qualitygate.wait=true` added) — log gains two new lines not present before:
```
[INFO] ------------- Check Quality Gate status
[INFO] QUALITY GATE STATUS: PASSED - View details on https://sonarcloud.io/dashboard?...
```
and would exit non-zero with `QUALITY GATE STATUS: FAILED` if the gate had failed.

## Rule

1. **Never assume a CI-invoked static-analysis tool's own "success" exit code reflects a
   server-side/asynchronous evaluation** (a quality gate, a policy check, a background scan) just
   because the immediate command completed without error. Distinguish "did the operation I
   triggered succeed" from "did the thing I actually care about pass" — they are different claims
   that can share the same green checkmark.
2. For `sonar-maven-plugin` specifically: always pair `sonar:sonar` with
   `sonar.qualitygate.wait=true` in any CI context where the gate result is meant to block the
   build. Without it, no combination of removing `|| echo` wrappers or checking the goal's exit
   code will ever catch a real quality-gate failure.
3. **Before believing a scanner step "already gates on X," read its actual log output for the
   specific evaluation step, not just the top-level success message.** A log line search for
   `"Check Quality Gate status"` (or the tool's equivalent) is the concrete verification step —
   its absence means the gate was never evaluated in that run, however clean the rest of the log
   looks.
4. When flipping a previously-non-blocking CI step to genuinely blocking, check every step *after*
   it in the same job for a missing `if:` condition (GitHub Actions skips subsequent steps by
   default once one fails) — this is the same gotcha already documented for CheckStyle→SpotBugs in
   #354, and recurred one step earlier here (Sonar→CheckStyle) once Sonar itself became capable of
   failing for the first time.
