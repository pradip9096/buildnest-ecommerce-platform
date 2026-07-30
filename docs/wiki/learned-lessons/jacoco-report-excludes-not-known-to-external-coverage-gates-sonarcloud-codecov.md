---
title: "JaCoCo's Own Report Excludes Are Invisible to External Coverage Gates (SonarCloud, Codecov) Unless Separately Configured"
category: testing
tags: [jacoco, sonarcloud, codecov, coverage-gate, ci, silent-drift, config-duplication]
keywords: [sonar new_coverage fails on excluded package, codecov patch fails structurally, jacoco exclude not synced sonar, coverage gate unmeasurable target, config coverage exclusion package]
source_conversations: ["Session 2026-07-13, issue #84, PR #370, issue #396"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "pom.xml's jacoco-maven-plugin report execution excludes certain packages (config/**, model/dto/**, etc.) from the generated jacoco.xml as deliberate policy, but SonarCloud's new_coverage gate and codecov/patch both consume that same jacoco.xml with no knowledge of the exclusion, so they enforce an 80% target against files with zero coverage data in the report -- not merely untested, structurally absent -- and no amount of test-writing can move the metric"
impact: medium — blocked PR #370's merge on a coverage gate that was unmeetable by design for the specific file touched (CacheConfig), costing a full investigation cycle (writing a dedicated unit test that genuinely ran and passed, yet had zero effect on the reported metric) before the real cause was found one layer below the test-coverage question
related_lessons:
  - docs/wiki/learned-lessons/jacoco-report-and-check-executions-can-silently-diverge-in-scope.md
  - docs/wiki/learned-lessons/conditionalonproperty-beans-can-be-systematically-excluded-from-every-test-in-the-suite.md
---

# JaCoCo's Own Report Excludes Are Invisible to External Coverage Gates (SonarCloud, Codecov) Unless Separately Configured

## Problem

PR #370 (#84) added a small new method (`CacheConfig.regionConfig()`) and needed to satisfy
SonarCloud's `new_coverage` quality gate and `codecov/patch`, both requiring 80% coverage on new
lines. A dedicated `CacheConfigTest` (plain unit test, mocked `RedisConnectionFactory`) was
written, confirmed to run and pass in the exact CI job that generates the coverage report — and
the gate still failed, unchanged, at the same percentage.

The actual cause was one layer below "is this code tested": `pom.xml`'s `jacoco-maven-plugin`
`report` execution has a long-standing `<exclude>**/config/**</exclude>` (alongside
`model/dto/**`, `model/payload/**`, `*Application.class`, and several named exception/util
classes) — a deliberate policy decision from before this PR, presumably because these packages
are boilerplate/Spring wiring not meaningfully unit-testable. That means `CacheConfig.java`'s
lines are **never included in `jacoco.xml` at all**, regardless of what tests exist. SonarCloud
(configured via `-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`) and
codecov (uploading the same `jacoco.xml`) both consume this report with no separate knowledge of
that exclusion — so they see "no coverage data for these new lines" and correctly, but
uselessly, report 0% coverage for a file that is entirely out of scope for the underlying report
by design.

This is a distinct instance of the same class of bug documented in [JaCoCo Report and Check
Executions Can Silently Diverge in
Scope](jacoco-report-and-check-executions-can-silently-diverge-in-scope.md) — that lesson covers
JaCoCo's *own* `report` vs. `check` executions drifting apart within `pom.xml` alone. This one is
broader: the same single-source-of-truth problem extends to *any external tool* that consumes the
JaCoCo XML report without its own copy of the same exclusion list.

## Rule

When a JaCoCo report has deliberate package/class excludes, and any external coverage-gate tool
(SonarCloud, Codecov, a custom CI check) consumes that same report:

1. **The exclusion list must be mirrored into every consuming tool's own configuration** —
   `sonar.coverage.exclusions` for SonarCloud, a `codecov.yml` `ignore:` list for Codecov, etc.
   There is no automatic propagation; each tool's gate config is independent and must be updated
   in the same change whenever the JaCoCo exclude list changes.
2. **Before writing more tests to fix a stuck coverage-gate failure, check whether the file/package
   is excluded from the underlying report first.** A test that genuinely runs and passes in the
   right CI job is not proof the metric is measurable — verify the report itself contains data
   for that file (or check the plugin's exclude config directly) before assuming the fix is "write
   a test."
3. **This is a shared-CI-configuration change** (per this repo's risk-modifier list) — treat edits
   to `.github/workflows/**` or a new repo-root `codecov.yml` with the same care as any other
   shared-config edit: get explicit authorization before applying, since it changes gate behavior
   for every future PR, not just the one that surfaced the gap.
