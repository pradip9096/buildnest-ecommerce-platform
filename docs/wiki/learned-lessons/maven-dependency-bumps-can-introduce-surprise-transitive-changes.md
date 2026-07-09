---
title: Maven Dependency Bumps Can Introduce Surprise Transitive Changes — Verify the Real Tree
category: tooling
tags: [maven, dependency-tree, transitive-dependencies, elasticsearch-java, kotlin]
keywords: [dependency bump breaks build, tools.jackson.core, kotlin-stdlib-common jar not found, verify dependency tree after version bump]
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
related_lessons: []
---

# Maven Dependency Bumps Can Introduce Surprise Transitive Changes — Verify the Real Tree

## Problem

Two separate surprises during #332's dependency upgrades, both from assuming a version bump was
"just a version bump":

1. Bumping `elasticsearch-java`/`elasticsearch-rest-client` from 8.18.8 to 8.19.18 (to fix a real
   CVE) silently introduced a brand-new transitive dependency: **Jackson 3.0.0**, under the new
   `tools.jackson.core` groupId — a different Maven coordinate entirely from the
   `com.fasterxml.jackson.core` groupId the rest of the project uses. This only became visible
   as a `jackson-core-3.0.0.jar` / `jackson-databind-3.0.0.jar` finding in a dependency-check
   scan; it wasn't apparent from the version bump itself, and it brought its own fresh CVEs
   (a brand-new major release has no patch history yet).
2. Overriding `kotlin-stdlib-common` to the same version as the rest of the `kotlin-stdlib`
   family (2.4.0, to match a CVE-driven bump of `kotlin-stdlib` itself) broke the build with a
   `DependencyResolutionException` — `kotlin-stdlib-common:2.4.0` has a published POM but no
   published JAR. Newer Kotlin releases stopped publishing a standalone JAR for that artifact
   (it became Gradle-module-metadata-only for multiplatform resolution); plain Maven dependency
   resolution, which expects an actual JAR, fails. `mvn dependency:tree` did not catch this —
   only a real `compile`/`verify` phase did.

## Root Cause

A version bump on one artifact changes that artifact's own transitive dependency declarations,
which can differ significantly between versions — a new major dependency, a groupId rename, or an
artifact within the same family that stopped shipping a JAR. `dependency:tree` resolves the
dependency graph but does not always perform the same strict artifact-existence check a full
`compile`/`package`/`verify` does, so it can report a resolution that later fails for real.

## Rule

- After bumping any dependency version — especially a minor/major version, not just a patch —
  re-run the actual dependency tree (`mvn dependency:tree`) and scan it for new or renamed
  groupIds you didn't expect, not just confirm the target artifact itself resolved to the
  intended version.
- Don't treat a clean `dependency:tree` as proof the build will actually compile — it's a
  necessary check, not sufficient. Run a real `compile`/`verify` before considering a dependency
  bump validated; `dependency:tree` can succeed on an artifact whose JAR doesn't actually exist
  (POM-only, Gradle-module-metadata artifacts being the concrete case found here).
- When a dependency-management override family includes multiple related artifacts (e.g. all
  `kotlin-stdlib-*` variants), don't assume they all take the identical override value safely —
  verify each one individually still has a real JAR at that version before bumping it in lockstep
  with its siblings.
