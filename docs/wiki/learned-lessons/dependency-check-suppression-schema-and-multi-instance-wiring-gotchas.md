---
title: OWASP dependency-check Suppression Gotchas That Silently Void Every Suppression
category: tooling
tags: [owasp-dependency-check, maven, suppressions, xml-schema, ci-cd]
keywords: [dependency-check suppressionFile not working, until element vs attribute, suppress schema error, multiple dependency-check-maven executions]
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
related_lessons: []
---

# OWASP dependency-check Suppression Gotchas That Silently Void Every Suppression

## Problem

Fixing #332 (CVSS≥7.0 dependency findings), suppressions were written for confirmed false
positives, referenced correctly from `owasp-suppressions.xml`, and the build kept failing with
the exact same findings — as if the suppressions didn't exist at all. Two independent causes,
both silent:

1. **`<until>` is an XML attribute on `<suppress>`, not a child element.** Writing
   `<suppress><until>2026-10-09Z</until>...</suppress>` instead of
   `<suppress until="2026-10-09Z">...</suppress>` violates the suppression XSD. The plugin logs
   this as a `[WARNING] Unable to parse suppression xml file` with a `SuppressionParseException`
   — **not a build failure** — and then proceeds as if the suppression file were empty. Every
   suppression in the file is silently discarded, not just the malformed entry.
2. **A single `pom.xml` can have multiple `dependency-check-maven` plugin instances** (e.g. one
   bound unconditionally in `<build><plugins>`, another inside a Maven profile) with independently
   configured `<suppressionFiles>`. Wiring the suppression file into one instance does nothing for
   the other. If CI actually invokes the profile-scoped instance (`mvn verify -Powasp`) but only
   the default-build instance has `<suppressionFiles>` configured, every suppression is silently
   ignored in the run that actually matters.

## Root Cause

Both failure modes share the same shape: a configuration mistake that degrades to a *warning*
or a *no-op* rather than an error, so the build "succeeds" at loading suppressions while actually
applying none of them. Nothing in the console output screams "your suppressions aren't working" —
you have to specifically grep the log for `Unable to parse` or `suppress` to notice.

## Rule

- After writing or editing an OWASP dependency-check suppression file, grep the actual build log
  for `Unable to parse suppression` before trusting that a re-run reflects your changes — a
  schema error here is a warning, not a failure, and will not stop the build.
- `until` goes on the `<suppress>` opening tag as an attribute (`<suppress until="2026-10-09Z">`),
  never as a nested `<until>` element. Validate the file's XSD conformance (not just well-formed
  XML) before relying on it — well-formed XML with a schema violation still "parses" cleanly under
  a plain `xml.etree` well-formedness check but fails the plugin's stricter schema validation.
- Before assuming a suppression file is wired correctly, check for more than one
  `dependency-check-maven` plugin declaration in the POM (profiles are the common place a second
  one hides) and confirm `<suppressionFiles>` is present on the specific instance/goal execution
  your CI pipeline actually invokes — not just any instance in the file.
