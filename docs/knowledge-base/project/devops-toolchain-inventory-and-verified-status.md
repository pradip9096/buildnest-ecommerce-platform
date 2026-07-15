---
title: "DevOps Toolchain — Inventory, Architecture, and How to Verify a Tool Actually Runs"
category: tooling
tags: [ci-cd, devops, github-actions, sonarqube, spotbugs, checkstyle, jacoco, pit, codecov, owasp-dependency-check, maven]
keywords: [devops toolchain, ci/cd inventory, tool health check, silently broken CI, non-blocking check, mvn goal resolution, sonar-maven-plugin, spotbugs prefix, checkstyle violations]
objective: "What tools make up BuildNest's CI/CD and quality-gate toolchain, what does each actually verify, and how do you tell whether a tool is genuinely running versus merely appearing configured?"
audience: "Anyone adding to or auditing BuildNest's CI/CD pipeline, or deciding whether to trust a green (or silently non-blocking) check"
scope: both
source_conversations: ["Session 2026-07-11/12 — #350, #352, #353, #354"]
last_updated: 2026-07-14
confidence: high
evidence_strength: strong
related_articles:
  - quality-gate-ratchet-pattern.md
status: published
---

# DevOps Toolchain — Inventory, Architecture, and How to Verify a Tool Actually Runs

## What Is It?

BuildNest's DevOps toolchain is the set of GitHub Actions workflows (`.github/workflows/`) and the
build/quality tools they invoke — JaCoCo, PIT, CheckStyle, SpotBugs, SonarQube, OWASP
Dependency-Check, Codecov, Liquibase, Docker — that together gate what "passing CI" means on a
pull request. This article inventories every tool with its **verified** current status (not just
its presence in a config file), and generalizes the verification method that surfaced three real,
previously-invisible failures in this exact toolchain within a single session (#350, #353, #354).

The central distinction this article exists to make precise: **a tool being *configured* is not
the same as a tool *actually running*, which is not the same as a tool's findings *being
enforced*.** All three states look identical from a green checkmark alone.

## Why It Matters

A CI step can look completely normal — present in the workflow file, referencing a real tool name,
wrapped in error handling — while never having executed a single time. Three separate instances of
exactly this were found in BuildNest's own `security.yml` in one audit:

- **SonarQube** (#350): the Maven command only ran `clean test`; no `sonar:sonar` goal was ever on
  the command line, and `-Psonar` referenced a Maven profile that has never existed in
  `backend/pom.xml`. Maven does not fail on an unknown `-P` profile — it prints a warning and
  continues, so the surrounding `clean test` succeeded normally every time. `SONAR_TOKEN` being a
  valid secret was irrelevant; nothing was ever submitted to SonarCloud.
- **SpotBugs** (#353): `mvn spotbugs:check` fails with `No plugin found for prefix 'spotbugs'`,
  because `com.github.spotbugs` isn't one of Maven's default-searched plugin groups and the plugin
  is declared nowhere in `pom.xml`. The step is wrapped in `|| echo "SpotBugs issues found
  (non-blocking)"`, so this resolution failure has always been silently swallowed and misreported
  as "issues found" — the tool has likely never run once.
- **CheckStyle** (#354): the inverse failure mode — this one **does** run correctly, and currently
  reports 8,305 real violations. But it's wrapped in the same non-blocking pattern, so the CI
  summary has never shown anything but a static "(non-blocking)" string regardless of whether the
  count was 0 or 8,305. A genuinely functioning tool can be just as invisible as a broken one if
  nothing surfaces its actual output.

None of these were caught by `./mvnw test` (the command every contributor runs locally) or by
watching CI go green on PRs — they only surfaced once a user asked "why don't I see a SonarQube
report" and each tool was verified by running its raw command directly, outside the CI wrapper
that had been hiding the truth.

## How It Works

### The failure pattern

Two independent Maven/shell behaviors combine to make a broken tool invocation invisible:

1. **Maven treats several classes of "this doesn't exist" as a warning, not a failure**: an
   unknown `-P<profile>` reference, or a bare goal (`tool:goal`) whose plugin groupId isn't in the
   default search path and isn't declared in `pom.xml`. Neither stops the surrounding build.
2. **CI steps wrapped in `|| echo "... (non-blocking)"`** (a deliberate, reasonable pattern for
   genuinely advisory tools — see [Blocking vs. Advisory](#blocking-vs-advisory-checks) below)
   swallow *any* non-zero exit code identically, whether that's "47 real findings" or "the tool
   never started." The echoed message is static text, not derived from what actually happened.

Combined, a tool can go from "never executes" to "green CI" with zero observable difference from a
tool that's working exactly as intended.

### Blocking vs. advisory checks

Not every check in this toolchain is meant to fail the build — that distinction is itself part of
the architecture, not an oversight:

| Blocking (fails the build/PR) | Advisory-only (`|| echo ...`, never fails the step) |
|---|---|
| JaCoCo (85%/package instruction coverage) | CheckStyle |
| PIT (77% mutation score, ratcheting to 83% through M5) | SpotBugs |
| `codecov/patch` (GitHub status check) | SonarQube (`code-quality` job) |
| The test suites themselves | |

"Advisory-only" is a legitimate design choice for tools whose findings need human judgment before
they're worth blocking a merge over. The bug in all three incidents above wasn't that these tools
are non-blocking — it's that non-blocking was silently doing double duty as "never actually shows
you what happened."

### Verified tool inventory (as of 2026-07-12; CodeQL row added and CheckStyle/SpotBugs rows corrected 2026-07-14 — both had graduated to blocking since #354/#317 but this table still showed their pre-fix status; other rows not re-verified since)

| Tool | Workflow / job | Verified status | How it was verified |
|---|---|---|---|
| JaCoCo | `ci.yml` (`build`), `ci-cd-pipeline.yml`, `security.yml`'s `jacoco-check` (`ci` profile) | ✅ Working | Real per-package coverage numbers observed gating multiple real PRs this session |
| PIT | `ci-cd-pipeline.yml` | ✅ Working | Real mutation-score gate (77% threshold) observed passing/failing correctly across #63/#65/#77 |
| Codecov (`codecov/patch`) | External GitHub App/Action | ✅ Working | Real per-line diff-coverage findings observed and fixed across multiple PRs |
| OWASP Dependency-Check | `ci.yml`, `security.yml` (`dependency-check` job) | ✅ Working | Real CVEs found and fixed in #332; runs on `verify` phase, declared in `pom.xml` |
| SonarQube / SonarCloud | `security.yml` (`code-quality` job) | ✅ Working, genuinely blocking | Was completely non-functional (#350) — fixed and confirmed via a real SonarCloud dashboard with real findings (#351). That fix only got analysis *running*; the step's own exit code still didn't reflect the quality gate result until #320 added `sonar.qualitygate.wait=true` — verified via the literal `QUALITY GATE STATUS: PASSED`/`FAILED` line appearing in the CI log (PR #401), which never existed in pre-#320 runs. Uses the org-default "Sonar way" gate on sonarcloud.io (no new bugs/vulnerabilities, Maintainability rating A, ≥80% new coverage) |
| Liquibase | Local `mvn test`/`verify`, all CI jobs that run tests | ✅ Working | Schema migrations exercised extensively (e.g. #77's coupon changeset) |
| Docker build | `deploy.yml` | ⚠️ Partial | Image build succeeds; registry push and Kubernetes deploy steps are both hardcoded `if: false` placeholders — no real deployment target configured yet |
| CheckStyle | `security.yml` (`code-quality` job) | ✅ Working, blocking (baseline + ratchet) | Was "runs, findings ignored" (#354's premise) — fixed same issue: `maven-checkstyle-plugin`'s native `maxAllowedViolations` parameter now genuinely fails the build above the verified baseline. Confirmed live in `pom.xml`/`security.yml` (`-Dcheckstyle.maxAllowedViolations=8305`) — the build fails only on a *net-new* violation beyond that ceiling, not on the existing debt itself |
| SpotBugs | `security.yml` (`code-quality` job) | ✅ Working, blocking on Priority-1/High only | Was "❌ Broken — `No plugin found for prefix 'spotbugs'`" (#353) — fixed same issue by declaring `spotbugs-maven-plugin` in `pom.xml`. Graduated further in #317: the 4 pre-existing High-priority findings were fixed so the ceiling starts at zero, then `threshold=High`/`effort=Max`/`failOnError=true` set and the `\|\| echo` fallback removed — confirmed live in `pom.xml` (lines ~819-824). Priority-2/Medium findings remain advisory (tracked separately, not yet triaged) |
| `ci-cd.yml` (whole workflow) | N/A | 🗑️ Deleted (#407) | Was only reachable on `main`/`develop`; this repo's actual default branch is `master` — unreachable for its entire lifetime. Deleted rather than fixed since everything of value in it (PMD, migrated in #406) was already superseded live elsewhere; `ci.yml`/`ci-cd-pipeline.yml` renamed to "Quality Gate Pipeline"/"Full Test Matrix & Docker Publish" for clarity in the same change |
| JMeter (`performance.yml`) | Manual/weekly dispatch | ❓ Not verified | Never exercised or checked during this audit — status genuinely unknown |
| CodeQL (semantic analysis) | `codeql.yml` (dedicated workflow, `java-kotlin` + `javascript-typescript`) | ✅ Working, non-blocking (advisory) | Previously only `upload-sarif` existed (display-only for OWASP's report; CodeQL's own `init`/`analyze` engine never ran) — added in #358. Verified via two independent checks, not just a green CI run: the PR's own CI completed both matrix jobs and `gh api .../code-scanning/analyses` showed genuine new analysis categories; the post-merge push scan on `master` then surfaced 5 real findings the PR's own scan had missed (4 `java/polynomial-redos` in `ValidationUtil`, tracked as #403; 1 `java/spring-disabled-csrf-protection`, confirmed a false positive and dismissed). See [CodeQL's `build-mode: autobuild` Scan Can Differ Between a PR's Merge-Ref and the Same Code's Post-Merge Push](../../wiki/learned-lessons/codeql-autobuild-pr-scan-results-can-differ-from-post-merge-branch-scan.md) for why a clean PR-time scan alone isn't sufficient verification for this tool |

**This table will go stale.** Treat it the same way `project_state.md`'s "Key Technical Facts"
section treats its own build numbers: a snapshot to re-verify before citing, not a permanently
true record. Update `last_updated` in this file's frontmatter whenever a tool's status changes.

## When to Use It

Re-run the verification method below — don't just trust this table — in these situations:

- **Before citing a specific tool as "working" in a PR description, an issue, or this article** —
  a green CI run does not establish this on its own, per the SpotBugs/SonarQube incidents above.
- **After adding, removing, or modifying a Maven-invoked CI step**, especially one that changes
  which lifecycle phase is invoked (`test` → `verify` → `package`) — a phase change can silently
  activate *other* plugins already bound to that phase (see the dependency-check-maven interaction
  documented in #350's PR: switching to `verify` for SonarQube would have silently triggered a
  redundant full OWASP/NVD scan if not explicitly guarded against).
- **Periodically, as a toolchain health check** — nothing in this repo currently re-verifies these
  tools automatically; the three incidents in this article were all found manually, by someone
  asking a direct question about one tool and then checking the rest while already in there.
- **Before trusting a non-blocking check's silence as "no findings"** — silence from an advisory
  check means "nothing was surfaced," which is a different claim from "nothing was found."

## Examples

### Verifying SonarQube (#350)

The broken command (`clean test -Psonar -Dsonar.login=...`) produced a normal, successful build —
no error anywhere. The fix was verified by running the corrected command locally with a
deliberately **invalid** token:

```bash
./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
  -Dsonar.token=invalid-token-for-local-smoke-test ...
```

This produced a real authentication error (`403 Forbidden... check sonar.token`) — proof the goal
now genuinely resolves and attempts to reach SonarCloud, something the old command never did
regardless of token validity. The real fix was then confirmed end-to-end on an actual PR: a new
`SonarCloud Code Analysis` check appeared, linking to a live per-PR dashboard.

### Verifying SpotBugs and CheckStyle (#353, #354)

Both were checked the same way — by running the exact command CI uses, directly, outside the
`|| echo` wrapper:

```bash
./mvnw -B spotbugs:check
# → [ERROR] No plugin found for prefix 'spotbugs' ...

./mvnw -B checkstyle:check
# → [ERROR] ... You have 8305 Checkstyle violations.
```

The first command's error type (plugin resolution failure) versus the second's (a real,
substantive rule violation count) is the difference between "never ran" and "ran and found a lot"
— a distinction the CI log's identical non-blocking message for both cases completely erases.

## Synthesis

The reusable lesson here generalizes past this specific toolchain: **a CI step's presence in a
workflow file, wrapped in reasonable-looking error handling, is not evidence it does what its name
suggests.** The only way to know is to run the exact command locally, outside the wrapper, and
read what it actually says — a resolution failure, a real finding count, or a genuine success.
This is cheap to do and wasn't done for SpotBugs and the old SonarQube command for what was likely
months, because CI staying green never prompted anyone to look closer. Advisory/non-blocking gates
are a legitimate design choice, but they remove the one signal (a red check) that would otherwise
force this kind of verification — which means someone has to substitute deliberate, periodic
verification for the check they chose not to make blocking.

## Quick Reference

| Question | Answer |
|---|---|
| Is a tool "configured" the same as "running"? | No — see SonarQube/SpotBugs, both looked configured while never executing |
| Does `-P<nonexistent-profile>` fail the build? | No, Maven warns and continues |
| Does a bare `mvn <prefix>:<goal>` always resolve? | No — only if the plugin's groupId is in Maven's default search path (`org.apache.maven.plugins`, `org.codehaus.mojo`, etc.) or declared in `pom.xml`. `com.github.spotbugs` is neither by default. |
| How do you tell a broken tool from a quiet one? | Run its exact CI command locally, outside any `\|\| echo` wrapper, and read the real exit behavior |
| Which checks in this repo actually block a merge? | JaCoCo, PIT, `codecov/patch`, and the test suites — see [Blocking vs. Advisory](#blocking-vs-advisory-checks) |
| Where's the full CI/CD workflow table? | `README.md`'s "CI/CD" section — this article explains *why* to distrust it without periodic re-verification, not a substitute for keeping it updated |

## Related Articles

- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the PIT mutation-threshold
  ratchet (77%→83% through M5) is one of the genuinely-blocking gates in this toolchain; this
  article assumes that mechanism as background.
