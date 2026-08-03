---
title: "`docker build` Succeeding Proves Nothing About the Container Actually Starting"
category: infrastructure
tags: [docker, dockerfile, healthcheck, jvm-flags, spring-profiles, smoke-testing]
keywords: [docker build succeeds container fails to start, COPY glob no match empty image, invalid JVM flag crashes startup, spring.profiles.include not allowed profile specific document, dormant Dockerfile bug never exercised]
source_conversations: [Session 2026-08-03, issue #124]
last_updated: 2026-08-03
confidence: high
evidence_strength: strong
root_cause: "docker build only validates that each layer's own commands exit 0 — a COPY glob matching zero files, an invalid JVM flag in ENTRYPOINT, and an application-config restriction violation are all invisible to build-time validation and only manifest when the image is actually run with docker run against its real ENTRYPOINT and dependencies"
impact: high — three independent bugs left the production Dockerfile unable to start at all, undetected since the project's initial commit because no prior session had run the built image end-to-end
related_lessons:
  - compose-pull-policy-build-silently-no-ops-an-explicit-pull-command.md
  - nginx-proxy-pass-bare-hostname-resolves-once-at-config-load-not-per-request.md
---

# `docker build` Succeeding Proves Nothing About the Container Actually Starting

**Found:** #124 (OPS-06, backend Dockerfile hardening)

`backend/Dockerfile` had built successfully on every prior CI run and local invocation since the
project's initial commit — but the resulting image had never actually been run end-to-end against
its real production `ENTRYPOINT` and dependencies. Doing so for #124's own acceptance criterion
("container starts and passes health check") surfaced three independent, pre-existing bugs that
`docker build`'s green exit code had masked for the entire project history:

1. **`COPY --from=builder /app/target/civil-ecommerce-*.jar app.jar`** — a stale glob from an old
   artifactId. The real jar was `buildnest-ecommerce-*.jar`. BuildKit's `COPY` did not error on the
   non-matching glob; it silently completed with `/app` left empty. The build layer reported
   `DONE 0.2s` with no warning.
2. **`-XX:G1NewCollectionHeuristicPercent=30`** — not a real JVM flag. `java` refused to start
   (`Error: Could not create the Java Virtual Machine`), but nothing before actually launching the
   container (build, lint, static analysis) could have caught it — it's a runtime-only failure.
3. **`spring.profiles.include=logstash`** inside `application-production.properties` — disallowed
   inside a profile-specific document since Spring Boot 2.4. The app crashed on `ConfigDataEnvironment`
   validation before any bean even started.

None of these were reachable by `docker build` alone, by CI's own build step, or by reading the
Dockerfile — each required actually running `docker run` against the image and watching it either
start or die. A `HEALTHCHECK` directive existing in the Dockerfile is not equivalent to that
healthcheck ever having been exercised; it can sit unverified for as long as no one runs the
container with real dependencies wired up.

**Generalizes:** a container image that "builds fine" and has a `HEALTHCHECK`/`ENTRYPOINT` that
"looks right" carries zero evidence about whether the container can actually start. This is the
Docker-specific instance of `testing.md`'s tier-0 principle (pure infra/build changes need
empirical execution, not just a green build) — but the lesson here is sharper: these bugs were not
introduced by the change being reviewed, they were **dormant since day one**, invisible to every
prior session that touched this Dockerfile because none of them ran the real image against real
dependencies with the real `ENTRYPOINT`. When touching a Dockerfile (or any deploy artifact) for
any reason — not just when its own issue asks for a startup-behavior change — a full `docker run`
against representative dependencies (not just `docker build`) is the only check that would surface
this class of bug, and it should not be assumed unnecessary just because "the Dockerfile hasn't
changed in this area."
