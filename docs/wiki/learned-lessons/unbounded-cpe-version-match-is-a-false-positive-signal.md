---
title: An Unbounded CPE Version Match in dependency-check Is a Strong False-Positive Signal
category: process
tags: [owasp-dependency-check, cpe, false-positive, nvd, security-triage]
keywords: [dependency-check false positive, cpe versionEndIncluding null, vendor product name collision, verify CVE against NVD before suppressing]
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
related_lessons: []
---

# An Unbounded CPE Version Match in dependency-check Is a Strong False-Positive Signal

## Problem

Fixing #332, four dependencies still failed the CVSS gate even after upgrading each to its
latest available version: `kotlin-stdlib` (9.8 CRITICAL), `micrometer-registry-prometheus`,
`netty-transport`, and `httpcore`. No further upstream patch existed for any of them, so the
temptation was to assume these were simply unfixed, current CVEs and either accept the risk or
give up on clearing the gate.

Pulling the dependency-check JSON report and inspecting each finding's `vulnerableSoftware`
entries showed all four shared the same shape: a CPE like `cpe:2.3:a:jetbrains:kotlin:*` or
`cpe:2.3:a:prometheus:prometheus:*`, with **both `versionEndIncluding` and `versionEndExcluding`
null** — meaning the match applies to *every* version of that product, with no range restriction
at all. Fetching each CVE's actual NVD detail page confirmed all four were real vulnerabilities,
but in a *different* component than the one flagged:

- `kotlin-stdlib` → the CVE was in Kotlin's build-cache/compiler tooling, not the runtime stdlib jar
- `micrometer-registry-prometheus` → the CVE was in the Prometheus *server's* remote-read endpoint, not the client-side exposition library
- `netty-transport` → the CVE only affected Netty 4.2.x's HTTP/3 codec module; the project was on 4.1.x
- `httpcore` → the CVE affected HttpComponents Core **5.x**, a different artifact lineage from the legacy 4.x branch actually in use

## Root Cause

NVD CPE entries are sometimes scoped at the whole-product level (vendor+product, no version
range) rather than to the specific vulnerable artifact/module/version — especially when a
security advisory covers one component of a multi-artifact project (a compiler, a server, a
specific codec module) but the CPE dictionary entry doesn't distinguish it from sibling artifacts
released under the same product name. dependency-check's CPE analyzer matches on vendor/product
name similarity to the Maven artifact's own metadata, so it can match a Maven library artifact
against a CPE for an entirely different component of the same umbrella product.

## Rule

- When a dependency-check finding persists after upgrading to the latest available version, check
  the finding's CPE match for a version range before assuming the CVE is simply unfixed. Pull the
  raw JSON report (`target/dependency-check-report.json`) and inspect `vulnerabilityIds` /
  `vulnerableSoftware` — no `versionEndIncluding`/`versionEndExcluding` bound is a strong signal
  of an imprecise, product-level match rather than a version-scoped one.
- Before suppressing at CRITICAL/HIGH severity, fetch the actual CVE detail page (e.g.
  `nvd.nist.gov/vuln/detail/<CVE-ID>`) and read what it's really about — the vulnerable
  module/version range described there, compared against what's actually being consumed
  (module, scope, major version line), is far stronger evidence than "the CPE looked generic."
  Suppressing on pattern-matching alone at high severity is not enough; suppressing after reading
  the primary source and confirming a concrete mismatch (wrong module, wrong major version line,
  wrong scope) is a defensible, auditable decision.
- This is a documented, general dependency-check limitation
  (dependency-check.github.io docs on false positives; see also arxiv.org/pdf/1808.09753 on
  coarse-grained CPE matching), not specific to any one library or ecosystem.
