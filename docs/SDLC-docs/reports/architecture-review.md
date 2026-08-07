# Pre-Production Architecture Review

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## Document Information

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Pre-Production Architecture Walkthrough (REVIEW-01) |
| **Related Issue** | #133 — `[chore] review: conduct pre-production code review and architecture walkthrough (REVIEW-01)` |
| **Traces to** | RTM `DC-01`, `DC-07` (existing rows already citing "architecture review" as their verification method) — see Requirement Traceability Note below |
| **Date** | 2026-08-07 |
| **Author** | Technical Lead |
| **Milestone** | M5 — Production Readiness |

---

## 1. Citation Correction

Issue #133 cites "SDP §10.5" as the basis for this review. On verification against the
current SDP (`docs/SDLC-docs/project-planning/software-development-plan.md`), **§10 is the
Risk Management Plan** (10.1 Risk Classification, 10.2 Risk Register, 10.3 Risk Response
Procedures) — it has no §10.5, and nothing in it defines an architecture-review process,
checklist, severity scale, or sign-off requirement.

The section that actually governs architecture review is **SDP §6.3, "Auditing Process"**,
which specifies: *"Architecture review | At each milestone gate | SDD conformance | Technical
Lead."* Related cross-references: §7.3 (Stakeholder Communication) lists Architecture Review
as a Technical-Lead-owned communication covering "SDD conformance, design decisions"; §13.1
sets a standing cadence of "Architecture Review: Technical Lead + Developers, Monthly, 60 min."
None of these define a checklist, a Critical/High remediation gate, or a sign-off template —
that structure is this report's own addition, modeled on the existing
[`security-assessment.md`](security-assessment.md) report format (findings-by-dimension,
severity ratings) and the `docs/ISO-IEC-IEEE/Audit_Review_Report_ISO_9001.md` precedent
(Document Approval sign-off table).

### Requirement Traceability Note

This review is a **verification activity against already-specified design constraints**, not
new specified behavior — so it does not require a new SRS/RTM row. RTM rows `DC-01` ("Layered
monolith: Controller → Service → Repository → Model") and `DC-07` ("Repository access only from
service layer") already name "architecture review" as their verification method. This report is
the executed instance of that verification method for the M5 production-readiness gate, per SDP
§6.3.

---

## 2. Scope and Methodology

Six dimensions were reviewed against the actual BuildNest backend source
(`com.example.buildnest_ecommerce`) and its documented conventions in `.claude/rules/spring/*.md`
and `.claude/rules/common/security.md`, cross-checked against the SDD's Design Views (§4) and this
repo's own git history for prior findings on the same subsystems: **security controls**, **data
flows**, **error handling**, **observability**, **scalability**, and **compliance**. Each item was
verified by reading the actual source (filter chains, transaction boundaries, exception handlers,
entity mappings, rate-limit wiring), not by re-reading the conventions documents alone.

---

## 3. Summary of Results

| Dimension | Result |
| :--- | :--- |
| Security controls | Pass (1 informational note) |
| Data flows | 1 Medium finding (remediated) |
| Error handling | Pass |
| Observability | Pass |
| Scalability | Pass |
| Compliance | Pass |

**No Critical or High findings.** One Medium finding was identified and remediated in this same
change (see §4.2). Per the issue's acceptance criteria ("All Critical and High findings
remediated"), this criterion is satisfied vacuously — no Critical/High findings exist.

---

## 4. Findings

### 4.1 Security Controls — Pass

- **Filter chain order**: `SecurityConfig.java` confirms the documented 3-tier order —
  `@Order(0)` `actuatorMonitoringSecurityFilterChain`, `@Order(1)` `swaggerSecurityFilterChain`,
  `@Order(2)` main `filterChain` — matching `spring-security.md`.
- **JWT**: `JwtTokenProvider` uses HMAC-SHA256, no roles in the token payload (subject/issuedAt/
  expiration only). `JwtAuthenticationFilter` always calls `filterChain.doFilter()` — no early
  short-circuit on failure.
- No `System.out.println`/`printStackTrace` in the `security/` package. No hardcoded secrets in
  `src/main`; `jwt.secret` has no default value (per #114).
- CORS `allowedOrigins`/`allowedMethods` match documented configuration.
- **Informational (not a finding)**: `SecurityConfig.java` disables CSRF on the actuator-
  monitoring and Swagger chains only, each with an explicit `// NOSONAR java:S4502` and inline
  justification (stateless machine credential / read-only docs UI) — this is the documented,
  intentional exemption pattern from `spring-security.md`, not a deviation.

### 4.2 Data Flows — 1 Medium Finding (Remediated)

**Finding (Medium):** `CheckoutServiceImpl` lacked the class-level `@Transactional(readOnly =
true)` default that `jpa.md` requires ("Set `@Transactional(readOnly = true)` at class level —
it enables Hibernate flush-mode optimization and read replicas"). Every mutating method already
carried its own explicit `@Transactional` override (`setAddress`, `applyCoupon`,
`selectShipping`, `initiatePayment`, `confirmCheckout`, `checkoutCart`,
`checkoutWithPayment`), and self-invocation was already correctly avoided — so this was a
missing-default gap, not a functional bug, but a real deviation from the documented convention.

**Remediation:** Added `@Transactional(readOnly = true)` at the class level in
`backend/src/main/java/com/example/buildnest_ecommerce/service/checkout/CheckoutServiceImpl.java`.
Verified every existing mutating method retains its own explicit non-readOnly override (no
behavior change for writes), and the one previously-unannotated method (`calculateFinalTotal`) is
a pure read/calculation, safe to inherit the new readOnly default. Verified via a full backend
compile and the existing real-context test suite for this class (`CheckoutServiceImplTest`,
`CheckoutFlowIntegrationTest`, `CheckoutValidateNoAmbientTransactionIT`,
`CheckoutControllerTest`, `MultiStepCheckoutControllerTest` — 84 tests total, 0 failures), which
already exercises the class's transactional behavior in a real Spring context.

**Informational (not a finding)**: `ProductControllerV1` returns raw `Product`/`Page<Product>`
entities directly, which would otherwise violate `jpa.md`'s "never return a JPA entity directly
from a controller" rule — but this controller is explicitly labeled Legacy/Deprecated (sunset
2026-12-31 per its own Javadoc), with `ProductControllerV2` as the DTO-based replacement already
in place. Not remediated here — deprecation is the existing, correct remediation plan.

### 4.3 Error Handling — Pass

`GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` and maps every exception type to
a generic `ErrorResponse` (status/message/path) with no stack traces or internal details leaked.
No silent `catch (Exception e) {}` blocks found in the service layer.

### 4.4 Observability — Pass

`AuditAspect` (`@Auditable`) is wired across 56 controller methods. `ElasticsearchConfig` is
present for audit-log ingestion, gated correctly behind `elasticsearch.enabled`. Actuator
monitoring uses a dedicated `@Order(0)` chain with its own `ROLE_MONITORING` credential, isolated
from the real `ROLE_ADMIN` account per the #359 design.

### 4.5 Scalability — Pass

No `FetchType.EAGER` found anywhere in `src/main/java` outside the documented `User.roles`
exception (zero grep matches for any other entity). Two-layer rate limiting confirmed:
`AdminRateLimitFilter` (servlet filter, `/api/admin/**`) and `RateLimitHeaderInterceptor` (MVC
interceptor, all paths), with `AuthController`/`PasswordResetController` correctly calling
`RateLimiterService` directly for login/password-reset — matching `rate-limiting.md` exactly.

### 4.6 Compliance — Pass

GDPR data export/erasure/consent (issue #128) is a real, wired implementation —
`UserDataExportDTO`, export logic in `UserServiceImpl`/`UserController`,
`AccountAnonymizationScheduler` for erasure, and Liquibase changeset
`20260803-001-alter-users-add-gdpr-consent-columns.xml` for consent tracking. CSRF is enabled via
`CookieCsrfTokenRepository` (wrapped in `NonClearingCsrfTokenRepository`, the documented GH-12141
workaround) on the main filter chain, with only the narrowly-scoped, justified exemptions noted in
§4.1.

---

## 5. Non-Blocking Follow-Up Items

The following were surfaced during this review but are out of scope for #133 itself — filed as
separate follow-up issues (priority/milestone/Project #9 set at creation) per
`development-workflow.md`'s Mid-Implementation Scope Discovery (SEPARATE-concern branch):

1. **SDP §10 mis-citation** (priority: low) — [#687](https://github.com/pradip9096/buildnest-ecommerce-platform/issues/687) —
   the SDP itself (or its issue-authoring convention) should be corrected so future issues don't
   cite a non-existent §10.5 for architecture review; the correct section is §6.3.
2. **`master` branch has an unresolved rebase conflict** (priority: medium) —
   [#688](https://github.com/pradip9096/buildnest-ecommerce-platform/issues/688) — between local
   commit `21e1cb9` (Sr. No. manifest-column restructuring across the wiki-lessons/KB-project
   READMEs) and `origin/master`'s `eb35ffa` (a new manifest row added under the old table shape) —
   discovered while branching for #133, deliberately not resolved here since it requires
   re-deriving numbering across multiple unrelated files.

---

## 6. Document Approval

| Role | Name | Signature | Date | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Technical Lead** | _____ | _____ | _____ | Pending |
| **Project Manager** | _____ | _____ | _____ | Pending |

---

## 7. Conclusion

The BuildNest architecture, as implemented, largely conforms to its own documented conventions
across all six reviewed dimensions. One Medium-severity conformance gap (missing class-level
`@Transactional(readOnly = true)` default on `CheckoutServiceImpl`) was found and remediated in
this same change. No Critical or High findings were identified. The platform's architecture is
assessed as ready to proceed toward the M5 production-readiness gate from an architecture-review
standpoint, pending stakeholder sign-off above.
