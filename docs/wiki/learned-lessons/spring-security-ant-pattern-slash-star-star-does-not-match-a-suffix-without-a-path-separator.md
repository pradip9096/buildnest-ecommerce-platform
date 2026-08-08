---
title: "Spring Security's `/path/**` Ant Pattern Matches Sub-Paths, Not a Suffix Appended Without a `/` — `/v3/api-docs.yaml` Falls Through a `/v3/api-docs/**` Public-Path Rule"
category: security
tags: [spring-security, ant-pattern, springdoc, openapi, ci-cd, workflow-dispatch]
keywords: [Ant pattern /** suffix mismatch, springdoc api-docs.yaml 401, SecurityConfig permitAll suffix path, curl exit 22, workflow_dispatch live verification]
source_conversations: [Session 2026-08-08/09, issue #127]
last_updated: 2026-08-09
confidence: high
evidence_strength: strong
root_cause: "SecurityConfig.java's public-path allowlist uses the Ant pattern \"/v3/api-docs/**\", which Spring's AntPathMatcher interprets as \"this path, or any sub-path under a '/' separator\" -- it does NOT match a suffix appended directly to the base path with no separator (e.g. \"/v3/api-docs.yaml\", springdoc's own YAML-variant route). A request for that suffix path falls through every explicit permitAll() rule and hits the catch-all anyRequest().authenticated(), returning 401/403 for an endpoint that was intended to be fully public."
impact: medium — would have broken every future automated fetch of the YAML spec variant (a new CI job, a client tool) silently returning 401 instead of the spec; caught before any real release tag was ever cut, via a manual workflow_dispatch dry run
related_lessons:
  - docs/wiki/learned-lessons/documented-public-endpoint-not-actually-in-securityconfig-permitall.md
  - docs/wiki/learned-lessons/preauthorize-401-vs-403-depends-on-url-pattern-matching-a-role-rule.md
---

# Spring Security's `/path/**` Ant Pattern Matches Sub-Paths, Not a Suffix Appended Without a `/`

## Problem

Issue #127 added `.github/workflows/publish-api-docs.yml`, a GitHub Actions job that boots the
backend against H2 and curls the generated OpenAPI spec to publish it as a static GitHub Pages
site. The first version fetched `/v3/api-docs.yaml` (springdoc's YAML-format variant of the spec
endpoint). A live `workflow_dispatch` dry run — done deliberately before ever cutting a real
release tag, per this repo's `testing.md` tier-0 rule for CI-config-only changes — failed:

```
curl -sf http://localhost:8080/v3/api-docs.yaml -o site/openapi.yaml
##[error]Process completed with exit code 22
```

Exit 22 is curl's `-f` flag surfacing an HTTP error response. The application itself was fully
up — the preceding liveness check (`/actuator/health/liveness`) had already passed.

## Root Cause

`SecurityConfig.java`'s public-path allowlist includes:

```java
.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
.permitAll()
```

`/v3/api-docs/**` is an **Ant-style path pattern**. Spring's `AntPathMatcher` treats `/**` as
"this path, or zero-or-more additional path *segments* under a `/` separator" — it matches
`/v3/api-docs`, `/v3/api-docs/foo`, `/v3/api-docs/foo/bar`, etc. It does **not** match
`/v3/api-docs.yaml`: that string is not `/v3/api-docs` followed by a `/`-separated segment, it's
`/v3/api-docs` with a `.yaml` suffix glued directly onto the same path segment. The matcher
correctly reports no match, the request falls through every `permitAll()` rule, and lands on the
chain's catch-all `anyRequest().authenticated()` — which rejects an unauthenticated CI request.

This is the mirror image of a mistake that's easy to make in the other direction too: assuming a
`/**` suffix pattern covers *any* string starting with the given prefix (glob-style), when it
actually only covers path-segment-separated continuations.

## Why Existing Checks Didn't Catch It

- **YAML syntax validation** (`python3 -c "import yaml; yaml.safe_load(...)"`) only checks the
  workflow file parses as valid YAML — it has no way to know anything about the *runtime*
  behavior of a URL path being fetched inside a `run:` block.
- **The `code-reviewer` agent pass** (run before merge, per this repo's DoD Item 2) caught a real
  bug in the same file (a `working-directory` mismatch) but had no visibility into
  `SecurityConfig.java`'s actual matcher semantics from reading the workflow YAML alone — the
  defect lives at the intersection of two files that were never diffed together.
- **A local curl against a locally-running instance** would have caught this immediately, but
  nothing in the implementation step ran one — the workflow was written, reviewed, and merged
  on the assumption that `springdoc-openapi`'s documented YAML-variant convention
  (`{path}.yaml`) would "just work" the same way the JSON variant does.

Only an actual `workflow_dispatch` execution against the real, running `SecurityConfig` chain
surfaced it — the same category of gap this repo's `testing.md` tier-0 rule exists to name
("the test is empirical execution," not a green YAML lint or a code-review pass).

## Fix

Switch to the endpoint the security config's pattern *does* cover — the JSON variant, with no
suffix:

```diff
- curl -sf http://localhost:8080/v3/api-docs.yaml -o site/openapi.yaml
+ curl -sf http://localhost:8080/v3/api-docs -o site/openapi.json
```

`/v3/api-docs` (no trailing segment at all) matches `/v3/api-docs/**` — `/**` matches *zero* or
more segments, so the bare base path is itself covered. No `SecurityConfig.java` change was
needed. `swagger-ui-bundle`'s `url` option accepts a JSON spec exactly as well as YAML, so no
functional loss.

An alternative fix — widening the security pattern to `/v3/api-docs**` (single star, no leading
slash before the wildcard) or adding a second explicit matcher for `/v3/api-docs.yaml` — was
available but deliberately not taken: it would have been a security-relevant change to
`SecurityConfig.java` for a docs-workflow issue, a larger blast radius than switching which
already-public endpoint the workflow reads from.

## Generalizable Takeaway

Before assuming a Spring Security `/prefix/**` `permitAll()` rule covers every URL that starts
with `/prefix`, check whether the actual request path is a **sub-path** (segments after an extra
`/`) or a **suffix** (extra characters glued onto the last segment, no `/`) — only the former is
covered. This applies to any tool or library that exposes format-suffix route variants
(`{path}.json`, `{path}.yaml`, `{path}.xml`) alongside a base path already covered by a wildcard
security rule: the base path being public does not imply its suffix variants are.
