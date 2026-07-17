---
title: "Exported Env Vars From `source .env` Leaked Into a Later, Separate Bash Tool Call and Silently Broke an Unrelated Test Run"
category: tooling
tags: [claude-code, bash-tool, shell-state, env-vars, spring-test-profile, false-positive]
keywords: [source .env persists across bash calls, SPRING_DATASOURCE_URL leaking into test profile, org.h2.Driver claims to not accept jdbcUrl, shell state persistence contradicts tool description, false BUILD FAILURE from environment contamination]
source_conversations: [Session 2026-07-17, issue #461]
last_updated: 2026-07-17
confidence: high
evidence_strength: strong
root_cause: "an earlier Bash tool call ran `set -a; source .env; set +a` to get a live backend running against Docker MySQL for browser verification; those exported env vars (SPRING_DATASOURCE_URL pointing at MySQL) were still present in a much later, separate Bash tool call that ran `./mvnw test -P all-tests`, overriding application-test.properties's intended H2 in-memory datasource and causing the H2 driver to reject a MySQL JDBC URL — a pure shell-environment leak, not a code or test regression"
impact: high — produced a convincing false BUILD FAILURE (1735 tests, 232 errors, ApplicationContext failed to load) that looked exactly like a real, severe regression and would have been reported as one if not root-caused before writing it up
related_lessons:
  - docs/wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md
  - docs/wiki/learned-lessons/spring-boot-run-does-not-read-env-and-checksums-drift-across-fixes.md
---

# Exported Env Vars From `source .env` Leaked Across Separate Bash Tool Calls and Broke a Later Test Run

## What Happened

Earlier in the same session, `set -a; source backend/.env; set +a` was run in a Bash tool call so `./mvnw spring-boot:run` would pick up Docker-mapped MySQL credentials for a live browser-verification pass (see the related `spring-boot-run-does-not-read-env-and-checksums-drift-across-fixes.md` lesson for why that export was needed in the first place). Much later — a different task entirely, running the full backend test suite (`./mvnw test -P all-tests`) to get real numbers for a Test Plan documentation fix — the run failed with 232 errors across nearly every `@SpringBootTest`-based test class:

```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:mysql://localhost:3307/buildnest_ecommerce?...
```

The Spring `test` profile is supposed to use an H2 in-memory database (`application-test.properties`), completely independent of the dev MySQL connection. But `SPRING_DATASOURCE_URL` (the MySQL URL, exported by the much-earlier `source .env`) was still present in the shell environment and took precedence over the test profile's own H2 config — because Spring Boot's env-var property source always outranks a properties file. This produced a real, reproducible test failure with a completely misleading shape (looks like every integration test suddenly broke) that had nothing to do with any code change.

## Why It Matters

Claude Code's own Bash tool description states: *"The working directory persists between commands, but shell state does not."* This finding contradicts that in practice within this session — an exported environment variable from one Bash tool call was still active in a separate, later Bash tool call. Whether this is because the two calls happened to share an underlying shell process, or some other session-scoping behavior, the practical consequence is the same: **don't assume a fresh Bash tool call means a fresh environment.** An export from hours (or many tool calls) earlier can silently resurface and change the outcome of an unrelated later command.

This is a more dangerous failure mode than a normal flaky test, because the failure signature (`BUILD FAILURE`, hundreds of errors, `ApplicationContext` failed to load) is *exactly* what a real, severe regression looks like — it does not present as an environment problem on its face. Only reading the actual `Caused by:` chain down to the JDBC driver error revealed the true cause.

## How to Apply

- **Before trusting a scary test-suite failure, check whether an earlier command in the same session exported environment variables that could affect this run** — especially `SPRING_DATASOURCE_URL`/`SPRING_PROFILES_ACTIVE`/anything that a properties-file-driven test profile is supposed to control independently. Read the actual root-cause exception chain (`Caused by:`) before concluding "N tests broke" — a single environment-contamination cause can fan out into hundreds of cascading errors that look like independent failures.
- **When running a test suite after any earlier `source .env` (or similar) in the same session, run it in an isolated environment**, not the current shell: `env -i HOME="$HOME" PATH="$PATH" bash -c "./mvnw test ..."` strips inherited exports cleanly. This is now the safe default for any Maven test run in this repo when a prior command in the session may have exported datasource-related variables.
- **Never report a test-suite result (pass or fail) without confirming the run environment was clean** — the same "verify, don't assume" discipline this repo already applies to CI-green claims (`lessons_cicd_build_mechanics.md`) applies equally to a local ad hoc run used to source documentation numbers.

## Synthesis

A tool's documented behavior ("shell state does not persist") is a design intent, not a guarantee to build safety-critical assumptions on — verify empirically when a result depends on environment isolation, rather than trusting the description. The concrete, reusable habit: any command whose correctness depends on *not* inheriting prior exports should be run in an explicitly isolated subshell, regardless of what the tool's state-persistence model is documented to be.
