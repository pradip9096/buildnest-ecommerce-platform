# 0002. Playwright as the E2E testing tool, migrating off Selenium

* Status: accepted
* Date: 2026-08-01
* Issue: #117

## Context and Problem Statement

#117 asks for a Playwright E2E suite in `frontend/`, but a working E2E suite already exists —
`backend/src/test/java/.../e2e/E2ETest.java`, built with Selenium WebDriver + JUnit, hosted in
the *backend's* test tree as a `@SpringBootTest` that drives a real Chrome browser against the
built frontend. #632 (open) already flagged this exact tooling mismatch: #117's acceptance
criteria name a tool that was never adopted, while the real capability was built with a
different one. Which tool should own this repo's E2E coverage going forward, and where should
it live?

## Decision Drivers

* Industry direction for E2E tooling in JS/TS-frontend stacks (not backend-JVM-hosted stacks)
* Frontend-team ownership — who actually runs/debugs these tests day to day
* CI decoupling — ability to run E2E without depending on the backend's own build lifecycle
* Avoiding two permanently-parallel E2E frameworks as long-term maintenance debt
* Not discarding the existing, working Selenium suite's coverage in the same change that adds a
  replacement (see #630/#631 — that suite already had real, hard-won fixes for masked CI jobs)

## Considered Options

* Correct #117's acceptance criteria to name Selenium and stop there
* Build a real, separate Playwright suite that coexists with Selenium indefinitely
* Migrate: build Playwright in `frontend/` per #117's literal acceptance criteria, and retire
  the Selenium suite in a follow-up once Playwright's coverage is confirmed stable in CI

## Decision Outcome

Chosen option: **"Migrate"**, because Playwright is now the clear industry-standard default for
new E2E work in a JS/TS frontend stack (2026 TestGuild survey: 45.1% vs Selenium's 22.1%; State
of JS 2025: 91% satisfaction; ~33M vs ~2.1M weekly npm downloads), and because E2E UI tests are
conventionally owned by the frontend stack, not the backend's test tree — the existing Selenium
suite's location was a reasonable-at-the-time choice (reusing `@SpringBootTest`'s context
lifecycle for setup/teardown) but not the standard shape. Confirmed with the user via
`AskUserQuestion` plus web research before implementing (see PR #117's description for sources).

### Consequences

* Good, because the frontend team gains ownership of its own E2E suite, runnable via `npm run
  test:e2e` with no JVM/Maven dependency.
* Good, because Playwright's auto-waiting and WebSocket-based architecture materially reduce the
  flakiness class Selenium's explicit-wait model is prone to.
* Bad, because until the follow-up retiring Selenium lands, this repo temporarily runs two E2E
  frameworks in CI (`e2e-tests` and `playwright-e2e` jobs) — accepted deliberately rather than
  deleting a working, already-debugged suite in the same PR that adds its replacement.
* Bad, because the new `playwright-e2e` CI job stands up the backend via a non-standard
  `spring-boot:run` invocation (H2, non-`test`-profile property overrides) that hasn't yet been
  verified against a real CI run — flagged as an explicit follow-up risk, not silently assumed
  to work.

## Pros and Cons of the Options

### Correct #117 to Selenium and stop

* Good, because it's the cheapest option — no new tooling, no new CI job.
* Bad, because it leaves E2E ownership in the backend's test tree indefinitely, which is the
  thing the user's own question ("shouldn't E2E tests live in the frontend?") identified as the
  wrong shape.

### Build Playwright, coexist indefinitely

* Good, because it avoids any risk to the already-working Selenium suite.
* Bad, because two permanently-parallel E2E frameworks is real ongoing maintenance debt with no
  plan to converge — worse than a deliberately time-boxed coexistence with a named follow-up.

### Migrate (chosen)

* Good, because it converges on a single, industry-standard E2E framework, owned by the right
  team, without a risky same-PR deletion of working coverage.
* Bad, because it requires a second PR (the Selenium-retirement follow-up) before the migration
  is actually complete — tracked explicitly rather than left implicit.
