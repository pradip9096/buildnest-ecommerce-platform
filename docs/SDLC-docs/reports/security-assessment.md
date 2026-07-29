# OWASP Top 10 (2021) Security Assessment

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## Document Information

| Attribute | Value |
| :--- | :--- |
| **Document Title** | OWASP Top 10 (2021) Security Assessment |
| **Related Issue** | #111 — `[security] hardening: complete OWASP Top 10 security assessment` |
| **Traces to** | SRS SEC-15 (added in this issue — see "Requirement Traceability Note" below) |
| **Date** | 2026-07-29 |
| **Author** | Technical Lead |
| **Milestone** | M5 — Production Readiness |

---

## 1. Scope and Methodology

### 1.1 Scope

Full OWASP Top 10 (2021) assessment (A01–A10) against the BuildNest backend
(`com.example.buildnest_ecommerce`), covering:

- Static/code-level review of authentication, authorization, cryptography,
  injection surfaces, configuration, dependency management, logging, and
  outbound-request (SSRF) handling.
- A dynamic active scan (OWASP ZAP `zap-full-scan.py`) against a live,
  locally-running instance of the application.

### 1.2 Requirement Traceability Note

Issue #111 was filed and titled `(SEC-02)`. On verification against the
current SRS (`docs/SDLC-docs/requirement-engineering/software-requirements-specification.md`),
**SEC-02 is an unrelated, already-Implemented requirement** ("The JWT secret
key shall be minimum 512 bits, externalised via `JWT_SECRET` environment
variable, and absent by default") — it has nothing to do with a Top 10
assessment. The issue body's blanket "SRS SEC-01 to SEC-15" citation also
referenced a SEC-15 row that did not exist at filing time (the SRS ended at
SEC-14). This assessment is instead traced to a newly added **SEC-15**,
added to the SRS and RTM in this same change, which is the correct FR for
"a full OWASP Top 10 assessment shall be performed and documented before
the M5 gate." See SRS Amendment 5.7 and RTM Amendment 1.42 for the full
correction record.

### 1.3 Scan Target — Deviation from the Issue's Literal Acceptance Criterion

The issue's acceptance criteria specify "OWASP ZAP active scan against
**staging environment**." This repository has **no staging environment** —
confirmed via `docker-compose.yml`/`docker-compose.override.yml` (local dev
services only: MySQL, Redis, Elasticsearch, Kibana, Logstash, Prometheus)
and `development-workflow.md` step 31 (`deploy`): *"No production
deployment target exists yet (M5 incomplete) — currently not applicable."*

Per explicit user decision (asked via `AskUserQuestion` at the start of this
issue), the ZAP scan was run against the **local dev stack** instead, with
this substitution documented here as a deliberate, approved scope
adjustment rather than a silent deviation.

### 1.4 Dynamic Scan Coverage Caveat

The ZAP scanner ran **unauthenticated** (no JWT/session configured for the
scanner). Its spidering and active-scan coverage was therefore limited to
the application's public/unauthenticated surface. It did **not** exercise
`ROLE_ADMIN`-gated endpoints (e.g. `/api/admin/**`, including the webhook
subscription endpoints where the A10 finding below was found). This is why
the dynamic scan itself returned zero SSRF findings despite a real SSRF gap
existing in an admin-only code path — the dynamic scan and the manual code
review are complementary, not substitutable, consistent with this repo's
own wiki lesson that curl/MockMvc/automated scanners routinely miss
auth-gated code paths (`cors-allowedmethods-restriction-invisible-to-curl-and-mockmvc.md`,
`documented-public-endpoint-not-actually-in-securityconfig-permitall.md`).

---

## 2. Summary of Results

| Metric | Result |
| :--- | :--- |
| Critical findings | **0** |
| High findings | **0** |
| Medium findings | 1 (A10 — remediated in this issue) |
| Low findings | 2 (A05, A08 — documented, non-blocking) |
| ZAP active scan checks run | 141 |
| ZAP FAIL/WARN | 0 |
| ZAP Informational | 1 (non-security: Non-Storable Content) |

**Acceptance criteria status:**
- ✅ OWASP ZAP active scan performed (against local dev stack — see §1.3)
- ✅ All A01–A10 categories assessed
- ✅ Zero Critical findings
- ✅ No open High findings (none identified — remediation-timeline
  requirement is therefore not applicable this cycle)
- ✅ This report stored at `docs/SDLC-docs/reports/security-assessment.md`

---

## 3. Per-Category Findings

### A01:2021 — Broken Access Control

**No findings.** RBAC enforced via both `@PreAuthorize("hasRole('ADMIN')")`
at the service/controller layer and `SecurityConfig`'s
`authorizeHttpRequests()` URL rules (defense in depth, per
`spring-security.md`). Live-verified: unauthenticated `GET
/actuator/env` → 401; unauthenticated `GET /api/admin/webhooks` → 401.
`spring-security.md`'s documented public-endpoint list matches the actual
`permitAll()` configuration (a documented failure mode in this repo per
`documented-public-endpoint-not-actually-in-securityconfig-permitall.md` —
checked directly against the running instance, not just the docs).

### A02:2021 — Cryptographic Failures

**No findings.** `BCryptPasswordEncoder` is the only password encoder in
use. JWT signed with HMAC-SHA256; secret externalised via `${jwt.secret}`
with no production default (`JwtKeyValidator` rejects the dev fallback
in production). JWT payload carries no roles or PII (identity claim
only). HSTS header live-verified present on HTTPS-capable responses.

### A03:2021 — Injection

**No findings.** All repository queries use Spring Data JPA derived
queries or parameterised `@Query` JPQL — grepped the full `repository/`
package for string-concatenated query construction; none found. No raw
`Statement`/native SQL string-building present.

### A04:2021 — Insecure Design

**No findings.** Rate limiting is enforced at two layers (servlet filter
+ MVC interceptor, Redis-backed). Three named circuit breakers
(`redisCircuitBreaker`, `databaseCircuitBreaker`, `elasticsearchCircuitBreaker`)
provide graceful degradation. Soft-delete pattern on `User`/`Order`
prevents destructive data loss via the standard API surface.

### A05:2021 — Security Misconfiguration

**1 Low finding.** The default (dev) `application.properties` sets
`management.endpoint.health.show-details=always` alongside
`management.endpoints.web.exposure.include=health,info,metrics,prometheus,httptrace,loggers`.
The `production` profile correctly overrides this to
`show-details=when-authorized` with a narrower exposure list, so this is
**dev-profile only** and not exploitable in a production deployment.
However, since `/actuator/health/**` is `permitAll()` by design
(`spring-security.md`), an unauthenticated dev-environment caller can see
full DB/Redis/circuit-breaker internals via the health endpoint.

**Recommendation (non-blocking):** invert the default so the safer value
(`when-authorized`) is the base default and local dev opts into `always`
via a dev-only profile, rather than the reverse. Tracked as a Low-priority
follow-up rather than fixed in this issue, since it has no production
impact.

### A06:2021 — Vulnerable and Outdated Components

**No open Critical/High findings.** Local `mvn dependency-check:check`
could not complete in this environment — the NVD API returned 403/404 (no
NVD API key configured locally; a tooling/environment limitation, not a
code finding). Used this repository's own CI evidence instead: the
`security.yml` workflow's "Check Dependencies" job (OWASP Dependency-Check)
passed on the latest `master` run (2026-07-29, run 30473694482).
`backend/owasp-suppressions.xml` already documents one actively-triaged,
NVD-verified false-positive suppression (an `httpcore` 4.x/5.x CVE
misattribution) with a review-until date, showing an existing, working
triage process for this category.

### A07:2021 — Identification and Authentication Failures

**No findings.** JWT access token expiry 15 minutes in production;
refresh-token rotation supported; login rate-limited to 5 requests/60s
(servlet-filter layer) / documented 3-per-5-min at the SRS level (SEC-07);
BCrypt password hashing. Live-verified CSRF bootstrap
(`GET /api/auth/csrf`) correctly sets the `XSRF-TOKEN` cookie.

### A08:2021 — Software and Data Integrity Failures

**1 Low finding.** `backend/Dockerfile` pins base images by tag
(`maven:3.9-eclipse-temurin-21`, `eclipse-temurin:21-jre`) rather than by
digest. Tag pinning is not fully immutable (a tag can be
re-pushed upstream), unlike digest pinning.

**Recommendation (non-blocking):** pin both `FROM` lines to a specific
image digest (`@sha256:...`) for full build reproducibility. Tracked as a
Low-priority follow-up. Liquibase changeset checksums already provide
tamper-evidence for schema migrations (`liquibase.md`). No `curl | bash`
or unpinned script execution found in `.github/workflows/`.

### A09:2021 — Security Logging and Monitoring Failures

**No findings.** `util/SecureLogger.java` provides PII-masking for log
output. Elasticsearch-backed audit log ingestion
(`ElasticsearchIngestionService`) and metrics/alerting
(`ElasticsearchAlertingService`) are wired for observability, gated behind
circuit breakers with graceful degradation per `elasticsearch.md`.

### A10:2021 — Server-Side Request Forgery (SSRF)

**1 Medium finding — remediated in this issue.**

`WebhookServiceImpl.createSubscription()` accepted an admin-supplied
`targetUrl` (via `WebhookAdminController`, `ROLE_ADMIN`-gated) validated
only by a format regex (`^https?://.+`), with no check against the
resolved host being a loopback, link-local, private, or wildcard address.
An admin (or an attacker who compromises an admin session) could set a
webhook target such as `http://169.254.169.254/latest/meta-data/` (cloud
instance metadata) or an internal-only service address, and the server
would make the outbound POST request on their behalf when the
subscribed event fires.

Severity is Medium rather than High/Critical because the endpoint is
already `ROLE_ADMIN`-gated (defense-in-depth gap, not an
unauthenticated exploit path), but it is a genuine SSRF surface worth
closing per OWASP A10 guidance regardless of the auth requirement.

**Fix:** added `util/SsrfUrlValidator.java`, a new `@Component` injected
into `WebhookServiceImpl`, which resolves the target host and rejects it
if `InetAddress` reports it as loopback, link-local, site-local
(RFC 1918 private ranges), wildcard/any-local, or multicast. Called at
`createSubscription()` time (the single write path for `targetUrl`), so
both subscription creation and later delivery are protected. Verified via:
- `SsrfUrlValidatorTest` (11 cases: rejects `127.0.0.1`, `localhost`,
  `169.254.169.254`, `10.x`, `172.16.x`, `192.168.x`, `0.0.0.0`, `::1`;
  accepts public IPs; rejects a hostless URL) — uses literal IP addresses
  throughout so the test performs no real DNS lookup and stays
  deterministic/offline-safe.
- `WebhookServiceImplTest` — new cases proving the validator is invoked
  before every `createSubscription()` save, and that a validator
  rejection prevents the subscription from being persisted at all.

The ZAP dynamic scan's own SSRF check (`Server Side Request Forgery
[40046]`) and Cloud Metadata Exposure check (`[90034]`) both reported PASS
— expected, since (per §1.4) the scanner never reached this
authentication-gated endpoint. This finding was caught only by direct
code review, illustrating why this assessment combines both methods
rather than relying on the dynamic scan alone.

---

## 4. OWASP ZAP Active Scan Detail

- **Tool:** `zaproxy/zap-stable` (Docker), `zap-full-scan.py`
- **Target:** `http://172.31.225.38:8080` (this session's local WSL2
  distro IP — see §1.3 for why `localhost`/`--network host`/
  `host.docker.internal` were not reachable from the Docker Desktop VM
  running the ZAP container, and why the distro's own bridged IP was used
  instead)
- **Scan duration cap:** 8 minutes (`-m 8`)
- **Result:** 141 automated checks run (injection families — SQLi, NoSQLi,
  XXE, SSTI, command injection, XPath, LDAP; RCE families — Log4Shell,
  Spring4Shell, Text4Shell; SSRF; Cloud Metadata Exposure; CSRF token
  checks; CORS; security headers; source-code disclosure). **0 FAIL, 0
  WARN, 1 INFO** (`Non-Storable Content` — expected, given this API's
  deliberate `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`
  headers on every response).
- **Reports:** raw HTML/JSON reports generated at scan time
  (`zap-full-report.html`/`.json`) — not committed to the repository (scan
  artifacts, not source), summarized in this document instead.

---

## 5. Non-Blocking Follow-Up Items

Both items below are Low severity, dev-only or reproducibility-only in
impact, and do not block the M5 gate per this issue's own acceptance
criteria (zero Critical, High-with-timeline). Tracked here rather than
filed as separate GitHub issues, since both are small enough to fold into
routine hardening work; promote to standalone issues if not addressed
within the M5 milestone:

1. **A05** — default (dev) `application.properties` actuator
   `show-details=always` should invert to the safer default, with `always`
   opted into by dev profile only.
2. **A08** — `backend/Dockerfile` base images should be pinned by digest,
   not tag, for full build reproducibility.

---

## 6. Conclusion

The BuildNest backend passes this OWASP Top 10 (2021) assessment with
**zero Critical and zero High findings**. One genuine Medium-severity SSRF
gap (A10) was identified via code review and remediated in the same
change, with dedicated test coverage. Two Low-severity, non-blocking
configuration-hardening opportunities (A05, A08) are documented for
future follow-up. SEC-15 (SRS/RTM) is satisfied.
