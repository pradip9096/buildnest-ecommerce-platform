# Load Test Results

## Document Control

| Field | Value |
| :--- | :--- |
| **Related Issue** | #118 |
| **Related RTM Rows** | PR-01, PR-02, PR-03, PR-04 |
| **Test Artifact** | `backend/src/test/java/com/example/buildnest_ecommerce/loadtest/LoadTestSimulation.java` (Gatling) |
| **Date** | 2026-08-02 |

## Scope and Tool Decision

#118 originally requested a new k6 script (`backend/scripts/load-test.js`, P95 < 200ms @ 100 VUs),
citing "SRS NFR-PERF-01" and "SDP §10.3." Neither citation exists in this repo — the actual
governing requirement is **RTM/SRS `PR-01`** (P95 < 500ms at 1,000 concurrent users, tool
specified as Gatling/JMeter), and this repo already has three CI-wired load-testing mechanisms
(Gatling, JMeter, Apache Bench). Introducing k6 would have added a fourth tool with conflicting
thresholds and no gap it uniquely filled.

Per user decision (2026-08-02), #118's scope was changed to a **gap-fill of the existing Gatling
simulation** rather than a new k6 script:
1. Added a **Checkout Flow** scenario (`TC-LOAD-005`) covering product browse → add-to-cart →
   checkout calculate-total → checkout process — the one scenario from #118's AC
   (product list, product detail, search, checkout) that `LoadTestSimulation.java` didn't
   previously cover.
2. Added a `global().responseTime().percentile(95.0).lt(500)` assertion matching **PR-01's**
   official P95 target, replacing the conflicting 200ms figure from #118's original text.
3. Fixed a pre-existing bug found while extending the file: `addToCartChain`'s POST to
   `/api/user/cart/add` was missing the required `userId` query parameter
   (`CartController#addToCart`), so it was hitting Spring's parameter-binding 400 before
   `@PreAuthorize` ever ran.

## Local Verification Run

Run against a local instance booted the same way as `ci-cd-pipeline.yml`'s `load-tests` job
(in-memory H2, `useTestClasspath=true`, real `SecurityConfig` — no test-profile bypass), via
`./mvnw gatling:test -Dusers.ramp=10`.

**First run** (before the checks below were fixed) surfaced a real, previously-undetected defect
in the checkout scenario's own status expectations, not a defect in the endpoints under test:
`Calculate Checkout Total` (`GET /api/checkout/calculate-total/{cartId}`) returned `401`, not one
of the checks' expected `200/403/404`. Root cause: `/api/checkout/**` is **not** covered by
`SecurityConfig`'s `/api/user/**` → `ROLE_USER` rule (see `spring-security.md`'s URL Authorization
Rules), so an anonymous GET falls through to the catch-all `anyRequest().authenticated()` and gets
`401` (`AuthenticationEntryPoint`) — unlike `/api/user/cart/add`, which matches `/api/user/**` and
gets `403` (`AccessDeniedHandler`) via `@PreAuthorize`'s ownership check. `Process Checkout`
(POST) already passed on the first run because CSRF protection rejects the unauthenticated POST
(`403`) before the auth catch-all is ever reached — already covered by the existing check. Fixed
by adding `401` to the GET check's expected-status list.

**Second run** (after the fix):

| Metric | Value |
| :--- | :--- |
| Total requests | 60 (OK=60, KO=0) |
| **P95** | **33 ms** |
| Success rate | 100% |

`BUILD SUCCESS`. A `java-reviewer` pass on the diff then flagged the duplicated `Add to Cart`
request (copy-pasted between `addToCartChain` and `checkoutChain`, with the same request name
merging their stats into one row in Gatling's report) as a drift risk — extracted into a single
shared `addToCartStep` reused by both chains.

**Third run** (after the extraction), full results:

| Metric | Value |
| :--- | :--- |
| Total requests | 60 (OK=60, KO=0) |
| Min response time | 5 ms |
| Max response time | 897 ms |
| Mean response time | 52 ms |
| P50 | 17 ms |
| P75 | 28 ms |
| **P95** | **93 ms** |
| P99 | 897 ms |
| Success rate | 100% |
| VU count | 10 (ramped over 30s) |
| Global assertions | `max < 5000ms`: **PASS** (897ms) · `P95 < 500ms` (PR-01): **PASS** (93ms) · `success > 95%`: **PASS** (100%) |

`BUILD SUCCESS` — full Gatling output and HTML report generated at
`backend/target/gatling/loadtestsimulation-<timestamp>/index.html` (not committed — build
artifact, gitignored).

## What This Run Does *Not* Verify

This local run used H2 in-memory storage, a fresh empty schema (no seed data — every
authenticated request is correctly rejected by design, not because of a data-dependent
business-logic success path), and only 10 virtual users. **It does not constitute a verification
of PR-01's actual target** (P95 < 500ms at **1,000** concurrent users against a real MySQL/Redis
staging deployment) — that requires a real staging environment, which was not available in this
session (no `docker compose`/staging infra running). PR-01's RTM status remains `🔵 Pending Ph-2`
for this reason; it should not be flipped to Implemented off this result alone.

To complete PR-01's actual verification once a staging environment exists:

```bash
./mvnw gatling:test -Dbase.url=https://staging.buildnest.example -Dusers.ramp=1000
```

and record the resulting P95/error-rate against the 500ms/1,000-VU target in this file.
