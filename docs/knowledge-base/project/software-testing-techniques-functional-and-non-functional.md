---
title: Software Testing Techniques (Functional and Non-Functional), Mapped to BuildNest's Own Code and CI
category: quality-engineering
tags: [test-design, equivalence-partitioning, boundary-value-analysis, decision-table, state-transition, exploratory-testing, non-functional-testing, performance-testing]
keywords: [how to choose test cases, equivalence partitioning example, boundary value analysis example, decision table testing, state transition testing, error guessing, exploratory testing, load testing, stress testing, reliability testing]
objective: How do we systematically derive which specific test cases to write, once the test type (unit/integration/E2E) and scenario set are already decided?
audience: Anyone implementing an issue in this repo who needs to design test cases, not just decide unit-vs-integration-vs-E2E
scope: general, with BuildNest-specific worked examples throughout
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
related_articles:
  - ../../../.claude/rules/common/testing.md
  - ../../../docs/wiki/learned-lessons/service-layer-mocked-unit-tests-can-fully-cover-a-method-while-its-query-logic-stays-untested.md
status: published
---

# Software Testing Techniques (Functional and Non-Functional), Mapped to BuildNest's Own Code and CI

## What Is It?

Testing *techniques* are systematic methods for deriving specific test cases — distinct from test
*type* (unit/integration/E2E — see `testing.md`'s "Choosing the Right Test Type") and test
*scenario identification* (mapping acceptance criteria to their enforcing layer — see
`development-workflow.md`'s `add-run-tests` step). Type answers "where does this run"; scenario
identification answers "which cases, at which layer"; technique answers **"how do I systematically
generate those cases instead of guessing."** A technique applies *within* any type — Equivalence
Partitioning can shape a unit test's inputs just as easily as an integration test's.

## Why It Matters

Without a technique, test-case design defaults to ad hoc coverage of whatever cases happen to come
to mind — which is exactly what happened while implementing #84's `findRelatedProducts`: category
match, tag match, exclusion, and stock-boundary cases were each added as they were separately
noticed, not derived from one systematic method. A Decision Table (see below) would have generated
the same set of cases directly from the query's own four boolean-ish conditions, with less risk of
missing a combination. Techniques exist to convert "did I think of every important case?" from a
guess into a checkable derivation.

## How It Works

### Functional techniques (the 80/20 core, in order of frequency of use)

| Technique | What it does | BuildNest example |
|---|---|---|
| **Positive testing** | Valid input, expected path | `ProductServiceImplTest.testGetProductById` — existing ID, existing product returned |
| **Negative testing** | Invalid input, expected rejection | `testGetProductByIdNotFound` — missing ID throws; `testFindRelatedProductsExcludesInactiveProduct` — inactive product must not appear |
| **Equivalence Partitioning (EP)** | Group inputs that should behave alike; test one representative per group | `findRelatedProducts`'s stock check partitions into "available > 0" (included) vs. "available ≤ 0" (excluded) — one representative per partition, not every possible stock number |
| **Boundary Value Analysis (BVA)** | Test the edges between partitions, where off-by-one defects live | `testFindRelatedProductsExcludesOutOfStockProduct` tests *exactly* zero-available (`inStock=reserved`) and negative-available (`reserved>inStock`) — the two boundary values either side of the `>0` cutoff, not an arbitrary large in-stock number. Also: CheckStyle's `maxAllowedViolations` ceiling — the real regression on PR #370 was exactly *one over* the boundary (8306 vs. 8305), the classic BVA failure mode |
| **Decision Table Testing** | Systematically enumerate combinations when behavior depends on multiple independent conditions | `findRelatedProducts` is a 4-condition decision table (category-match × tag-match × active × in-stock) that was tested ad hoc rather than derived from a table — see Examples below for the table this should have started from |
| **State Transition Testing** | Model behavior as states + transitions, test each transition and each illegal one | `Order.status` (`PENDING → CONFIRMED → SHIPPED → DELIVERED`, plus `CANCELLED` from multiple states); `CheckoutSession`'s multi-step flow (`PENDING_SHIPPING → PENDING_PAYMENT → CONFIRMED`) — #77's coupon-apply restriction ("only while `PENDING_SHIPPING`/`PENDING_PAYMENT`") is a state-transition guard, and its test suite covers illegal-transition rejection explicitly |
| **Use Case / Scenario Testing** | Test a full multi-step user workflow end to end | The E2E/smoke-sanity-regression tier (see `testing.md`) — e.g. #122's real `docker compose up` verification of the full metrics → Grafana → Alertmanager → Mailpit pipeline |
| **Error Guessing / Exploratory** | Tester experience anticipates likely defects, no formal derivation | The self-invocation bug on PR #370 wasn't found by any of the above — it was found by SonarCloud's `java:S6809`, effectively automated error-guessing against a known defect pattern; #122's WSL2 mount-propagation bug was found only by exploratory testing (actually running the stack), not by any planned technique |

### Non-functional techniques

BuildNest's own CI pipeline already names several of these as distinct jobs, confirming the
taxonomy rather than introducing a new one:

| Technique | Question it answers | BuildNest mapping |
|---|---|---|
| **Performance** | Is it fast enough? | Response-time assertions where present; `Reliability Tests`/`Stress Tests`/`Load Tests` CI jobs |
| **Load** | Can it handle expected load? | `Load Tests` CI job (Gatling-based, conditionally run — `skipping` unless triggered) |
| **Stress** | What happens overloaded? | `Stress Tests` CI job, same conditional-run pattern |
| **Scalability** | Does it hold up as demand grows? | The rate-limiting (`rate-limiting.md`) and circuit-breaker (`resilience4j.md`) designs exist specifically to bound behavior under growing load, not just handle a fixed baseline |
| **Security** | Protected against attacks? | `Security Scan (OWASP)`, `Security Vulnerabilities`, `OWASP Dependency-Check` CI jobs; `security.md`/`spring-security.md` rules |
| **Reliability** | Runs consistently without failure? | `Reliability Tests` CI job; circuit-breaker graceful-degradation design (`resilience4j.md`) |
| **Usability** | Is it easy to use? | Not yet applicable — `frontend/` is a stub per `CLAUDE.md`; revisit once a real UI exists |
| **Endurance / Soak** | Does performance degrade over time? | **Gap** — no soak-testing job exists in this repo's CI today; worth a follow-up issue if long-running-process degradation becomes a real concern (e.g. connection-pool exhaustion under sustained load) |

## When to Use It

Reach for a technique once you already know the test *type* and the *scenario set* (per the two
cross-referenced decisions above) and need to generate the actual test cases:

- **Default to the functional 80/20 core** (Positive, Negative, EP, BVA, Decision Table) for any
  new logic with conditions/branches/ranges — this covers the large majority of functional defect
  classes for the least design effort.
- **Reach for State Transition** specifically when the entity/process has an explicit state field
  or step sequence (`status`, `step`, a state machine) — not for stateless CRUD.
- **Reach for Use Case/Scenario** at the E2E tier only, per `testing.md`'s type-selection tiers —
  it's expensive, so it belongs where a unit/integration test genuinely can't reach the risk.
- **Treat Exploratory/Error Guessing as a supplement, never a replacement** for the systematic
  techniques above — it catches what a fixed method won't anticipate, but isn't repeatable or
  reviewable the way a derived test case is.
- **Adopt a non-functional technique when the repo already has infrastructure naming it** (this
  repo's `Stress Tests`/`Load Tests`/`Reliability Tests` CI jobs) rather than inventing new
  non-functional test infrastructure ad hoc.

## Examples

**The Decision Table `findRelatedProducts` should have started from**, reconstructed after the
fact (PR #370 derived its test cases ad hoc, one at a time, rather than from this table up front —
worth citing as the counter-example the technique exists to prevent):

| Same category? | Shares a tag? | Active? | In stock? | Included? | Rank |
|---|---|---|---|---|---|
| Yes | — | Yes | Yes | Yes | 0 (first) |
| No | Yes | Yes | Yes | Yes | 1 |
| No | No | Yes | Yes | No | — |
| Yes | — | **No** | Yes | **No** | — |
| Yes | — | Yes | **No** | **No** | — |

Every row maps directly to one of the 6 scenarios actually added to `ProductRepositoryTest` — the
table would have made the full case set visible in one pass instead of accumulating scenarios
individually.

## Synthesis

Type, scenario, and technique are three separable decisions, not one pipeline: type picks *where*
a test runs, scenario identification picks *which acceptance criteria* need proving and at *which
layer*, and technique picks *how* to systematically generate the specific cases once both of those
are settled. Defaulting to ad hoc case design — as this repo's own #84 did before this article
existed — works until a combination gets missed; a Decision Table or BVA pass makes the omission
visible before a test run does.

## Quick Reference

| Question | Technique |
|---|---|
| Valid input, expected result? | Positive testing |
| Invalid input, expected rejection? | Negative testing |
| Inputs that should behave alike, grouped? | Equivalence Partitioning |
| Off-by-one risk at a range's edge? | Boundary Value Analysis |
| Behavior depends on 2+ independent conditions? | Decision Table |
| Entity/process has explicit states/steps? | State Transition |
| Full multi-step workflow, real infra? | Use Case / E2E |
| Nothing above anticipated it? | Error Guessing / Exploratory |

## Related Articles

- [`.claude/rules/common/testing.md`](../../../.claude/rules/common/testing.md) — test *type*
  selection (unit/integration/E2E), the layer above technique selection
- [A Service Method's Unit Tests Can Look Fully Covered While the Query That Actually Implements
  Its Acceptance Criteria Has Zero Real-Data Test
  Coverage](../../wiki/learned-lessons/service-layer-mocked-unit-tests-can-fully-cover-a-method-while-its-query-logic-stays-untested.md) —
  the concrete case that motivated writing this article
