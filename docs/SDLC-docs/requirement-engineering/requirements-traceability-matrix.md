# Requirements Traceability Matrix (RTM)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Requirements Traceability Matrix (RTM) |
| **Document ID** | RTM-BUILDNEST-001 |
| **Version** | 1.60 |
| **Date** | 2026-08-09 IST |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 29148:2018 §6.2.5 (Traceability) |
| **Related SRS** | SRS-BUILDNEST-001 v5.16 — `docs/SDLC-docs/requirement-engineering/software-requirements-specification.md` |
| **Related SDD** | SDD-BUILDNEST-001 v4.21 — `docs/SDLC-docs/design/software-design-description.md` |
| **Related TP** | TP-BUILDNEST-001 v4.8 — `docs/SDLC-docs/software-testing/test-plan.md` |
| **Baseline Assessment** | `docs/reports/baseline-assessment-2026-06-19.md` |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-06-19 | QA Manager | Initial controlled release — 156 requirements traced from SRS v4.0 through SDD v3.0 design elements, implementation classes, and test classes; status verified against live codebase and Baseline Assessment Report | Pending |
| 1.1 | 2026-07-17 14:02 IST | QA Manager | Corrected the "Open Defects Blocking Phase 1 Exit" section (§9/§11/§12) — all 6 listed defects (DEF-001 through DEF-006, TIR-01–04 and MNT-03) verified already resolved in source; recomputed the Coverage Summary Totals row from its own 24 category rows (#452, PR #454) | Pending |
| 1.2 | 2026-07-17 16:33 IST | QA Manager | Added FR-FE-31 (admin category management UI, tracing to #428); corrected FR-FE-22/24/25's cited paths from fictional `.jsx` files to the real implemented components (`AdminDashboardPage.tsx`, `InventoryTab.tsx`, `OrdersTab.tsx`) and their status to ✅ Implemented; FR-FE-23 confirmed still 🔵 Pending (real path doesn't exist — #425 open); recomputed the Frontend row and Coverage Summary Totals row accordingly (#450) | Pending |
| 1.3 | 2026-07-17 18:00 IST | QA Manager | Full per-requirement audit of §6.10 (`FR-FE-01`–`21`, `26`–`30`): removed the false "frontend is a stub" header, corrected all 25 remaining rows' cited paths from fictional `.jsx` files to their real `.tsx` implementations, and re-derived each row's status from the requirement's actual text, not just file presence — 20 ✅ Implemented, 7 🟡 Partial (form-validation library, Razorpay modal, password-strength indicator, and 4 account/search features shipped as tabs/query-params rather than standalone routes), 4 🔵 Pending (Toast, Footer, Breadcrumb genuinely absent) (#453). Recomputed the Frontend Coverage Summary row (4→20 Implemented, 0→7 Partial, 27→4 Pending) and Coverage Summary Totals row; also recomputed the §12 Phase 2 Frontend Started/Not-Started counts, which surfaced and fixed a pre-existing, unrelated arithmetic error in that table's own row-total sum (81, not the previously stated 80) | Pending |
| 1.4 | 2026-07-17 18:18 IST | QA Manager | Updated the `Related SRS` cross-reference from v4.2 to v4.3 following SRS's own Appendix A endpoint-catalogue re-derivation (#456) — no RTM row content otherwise touched by that fix | Pending |
| 1.5 | 2026-07-17 19:11 IST | QA Manager | Updated the `Related SRS` cross-reference from v4.3 to v4.4 (SRS/SDD `FR-FE-01–31` aggregate-row sync, #470) and the `Related SDD` cross-reference from a long-stale v3.0 to the current v3.3 — the latter had drifted through 3 intervening SDD version bumps without ever being updated here; no RTM row content otherwise touched by this fix | Pending |
| 1.6 | 2026-07-17 20:45 IST | QA Manager | Updated the `Related SDD` cross-reference from v3.3 to v3.4 following SDD's own §4.7.3 API Endpoint Catalogue re-derivation (#471) — no RTM row content otherwise touched by that fix | Pending |
| 1.7 | 2026-07-17 21:15 IST | QA Manager | Found during a fresh RTM/SRS/SDD/Test-Plan verification sweep: `Related SRS` had drifted one version behind (v4.4, SRS is now v4.5 following #474), `Related SDD` similarly (v3.4, SDD is now v3.5 following this same sweep's own fix), and `Related TP` had never been updated at all since the original baseline (stuck at v4.0, Test Plan is now v4.2). Updated all three to current. No RTM row content otherwise touched | Pending |
| 1.8 | 2026-07-17 22:30 IST | QA Manager | FR-FE-23 (admin product management) corrected from 🔵 Pending Ph-2 citing a fictional `AdminProductMgmt.jsx` to ✅ Implemented, citing the real `ProductsTab.tsx`/`ProductFormModal.tsx` and their `ProductsTab.test.tsx` coverage (#425, part of Epic #424 — same pattern as FR-FE-31/#428). Recomputed the Frontend Coverage Summary row (20→21 Implemented, 4→3 Pending), the Coverage Summary Totals row (113→114 Implemented, 51→50 Pending), and the §12 Phase 2 Frontend Started/Not-Started counts (27→28 Started, 4→3 Not Started) and Phase 2 total (35→36 Started, 46→45 Not Started) accordingly | Pending |
| 1.9 | 2026-07-18 10:30 IST | QA Manager | FR-FE-23's own SRS text explicitly names "image upload" as part of this requirement's scope, but its #425-era citation only covered `ProductsTab.tsx`/`ProductFormModal.tsx` (product CRUD, no image capability). Added `ProductImagesModal.tsx`/`ProductImagesModal.test.tsx` to FR-FE-23's Implementation/Test citations now that image upload/reorder/delete genuinely exist (#426, part of Epic #424) — status stays ✅ Implemented (no count changes; #425 had already marked it Implemented, this closes the citation gap rather than changing status) | Pending |
| 1.48 | 2026-08-02 | QA Manager | Extended PRT-01 and DC-06's Implementation citations to include the new production Docker Compose stack (`docker-compose.prod.yml`, `nginx-proxy/`) added for #119 (OPS-01) — resource limits, healthchecks, MySQL named-volume persistence, and TLS termination on top of the existing multi-stage Dockerfiles those rows already covered. No dedicated OPS-* row exists in this RTM's ID scheme; citing the closest existing containerization requirements rather than inventing an unlinked ID. Status unchanged on both rows (existing 🟡 Partial gaps — #124 backend USER directive, PR-08 build-time measurement — are unrelated to this change) | Pending |
| 1.49 | 2026-08-03 IST | QA Manager | Extended PRT-01 and DC-06's Implementation citations again, same no-dedicated-OPS-row pattern as 1.48: `deploy.yml` (#120, OPS-02) now actually builds+pushes both Dockerfiles' images to GHCR and deploys them via SSH+`docker compose` against the #119 production stack, replacing the prior stub whose registry-push/Kubernetes-deploy steps were both hardcoded `if: false` with no real target ever configured. See ADR [0003](../design/adr/0003-ssh-docker-compose-plus-ghcr-as-the-deployment-mechanism.md) for the SSH+Compose-over-Kubernetes decision. Status unchanged on both rows (this closes a citation gap — the Dockerfiles are now genuinely deployed, not just built — the pre-existing 🟡 Partial gaps are unrelated) | Pending |
| 1.50 | 2026-08-03 IST | QA Manager | REL-05 (RPO ≤5 min) updated from 🔵 Pending Ph-2 to 🟢 (partial): #121 (OPS-03) implemented `backend/scripts/backup-db.sh`/`restore-db.sh` (daily mysqldump+gzip, 30-day retention, cron-scheduled) and a live DR drill verified a 30s restore (well under the related REL-04's 15-min RTO target). Corrected the issue's own stale "SRS NFR-AVL-01" citation — no such ID exists; REL-05's Related Component already named "MySQL backup strategy" directly. Not marked fully ✅ Implemented: daily-only backup cadence gives an actual RPO of up to ~24h, not the required ≤5 min — that gap requires point-in-time recovery (binlog/replication), filed as follow-up #675 rather than folded into #121's scope. REL-04 (Kubernetes restart policies) is unrelated to this issue's scope and stays unchanged | Pending |
| 1.51 | 2026-08-03 IST | QA Manager | PRT-01 updated from 🟡 Partial to ✅ Implemented: #124 (OPS-06) closed the last open gap on this row — `backend/Dockerfile` now runs as non-root user `buildnest` on `eclipse-temurin:21-jre-alpine` with container-aware `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` JVM flags, live-verified via a real `docker build`+`docker run` against MySQL/Redis with SSL enabled (actuator health check returned `UP`). That same live verification also surfaced and fixed three pre-existing bugs unrelated to PRT-01's own text but blocking the container from ever starting: a stale `COPY` jar-name glob, an invalid JVM flag, and a Spring-Boot-2.4-incompatible `spring.profiles.include` usage — see CHANGELOG's #124 entry for the full list. DC-06 stays 🟡 Partial (its own remaining gap, frontend build-time/layer-caching per PR-08, is unrelated to #124's scope) but its citation text is updated to note the backend hardening | Pending |
| 1.52 | 2026-08-03 IST | QA Manager | Added COMP-01/02/03 (#128, GDPR data export/erasure/consent) — correcting the issue's own stale "SRS NFR-COMP-01 to NFR-COMP-03" citation (no such IDs existed at filing time), same filing-time traceability-mismatch shape as SEC-15/SEC-16. All three ✅ Implemented: `UserController`/`UserServiceImpl` (export + soft-delete), `AccountAnonymizationScheduler` (30-day PII anonymization, per-row transaction isolation so one collision can't block the cohort), `JwtAuthenticationFilter` (rejects a still-valid JWT for a deactivated account — closes a session-termination gap found during this issue's own security review), `RegisterRequest.consentGiven`. Added new Coverage Summary row "Compliance (COMP)" (3/3/0/0/0/0) and recomputed the Totals row (197→200 total, 138→141 Implemented). Updated `Related SRS` from v5.10 to v5.11 | Pending |
| 1.53 | 2026-08-05 IST | QA Manager | SEC-12 (JWT rotation) corrected from 🟡 Partial to ✅ Implemented, SEC-13 (DB password rotation) corrected from 🔵 Pending Ph-2 to ✅ Implemented (#132, CFG-01): added `docs/operations/secrets-rotation-procedure.md` as the "Operational runbook" both rows' Test Method column previously cited but never linked to a real file — documents where every secret lives, minimum-strength requirements, and rotation steps at the exact 90-day/180-day cadence SEC-12/SEC-13 specify. Also removed a dead, fully-commented-out `backend/docker-compose.yml` block with a hardcoded plaintext password (recoverable from git history, treated as compromised) and documented a real gap an agent-based review pass caught: `docker-compose.yml`'s `MONITORING_PASSWORD` Compose-level fallback default is only caught by `SecurityConfig`'s in-app fail-fast, not prevented at the Compose layer itself. The issue's own acceptance criteria cited "Stripe keys" — this repo has no Stripe integration (Razorpay only); treated as a stale/typo reference to Razorpay per user confirmation. Recomputed the Security (SEC) Coverage Summary row (12→14 Implemented, 1→0 Partial, 3→2 Pending) and the Coverage Summary Totals row (141→143 Implemented, 15→14 Partial, 44→43 Pending). Also recomputed §12 Phase 2 Security row (Started 2→4, Not Started 3→1) and Phase 2 total (Started 40→42, Not Started 41→39) | Pending |
| 1.54 | 2026-08-07 IST | QA Manager | PRT-01's citation text extended (status stays ✅ Implemented, no count change): #364's own AC1/AC2 (fix the stale `COPY` glob, confirm the container starts) were already satisfied by #124's fix cited in row 1.51 — #364 was filed 2026-07-12, before that fix landed, so its premise was partially stale by the time it was picked up. Its remaining, genuinely open AC3 ("confirm no other Dockerfile/CI reference still uses the old `civil-ecommerce` naming") was completed: `backend/README.md`, `kubernetes-deployment-optimized.yaml`, `ElasticsearchMetricsCollectorService`'s `spring.application.name` default, `data.sql`'s seed admin email, `application-test.properties`, and `.github/workflows/security.yml`'s OWASP project label all corrected; live-verified via a fresh `docker build`+`docker run` against real MySQL/Redis that `app.jar` starts and reaches Spring context init (the #124 `COPY` fix holds). A separate, out-of-scope finding — the `@SpringBootApplication` main class `CivilEcommerceApplication`/`CivilEcommerceApplicationTests` is also still stale-named, cited by this RTM's own `FR-SEL-07` row (1.34) — was filed as follow-up #690 rather than folded in here, since renaming the main class is a materially larger/riskier change | Pending |
| 1.55 | 2026-08-07 IST | QA Manager | Added FR-CHK-10 (#88, order return and refund request flow — RET-01/02/03) — correcting the issue's own stale "SRS RET-01 to RET-03" citation (no such IDs existed at filing time), same filing-time traceability-mismatch shape as SEC-15/COMP-01. ✅ Implemented: `UserOrderController.createReturnRequest()`, `AdminReturnController`, `ReturnServiceImpl` (ownership check, 30-day return-window enforcement from a new `Order.deliveredAt` column, pessimistic-locked duplicate-active-return guard, refund via `PaymentService.processRefund` + inventory restoration via `InventoryService.adjustStock`, inventory-then-refund ordering so a DB-only failure never leaves an un-recorded external refund), tested via `ReturnServiceImplTest` (unit) and `ReturnRequestIT` (real H2 round-trip). Recomputed the Checkout & Orders (FR-CHK) Coverage Summary row (9→10 total, 8→9 Implemented). Merged on top of the concurrent v1.53/v1.54 (#132/#364) — recomputed Totals row to reflect both branches' changes together (200→201 total, 141→144 Implemented [+1 mine, +2 theirs], 15→14 Partial, 44→43 Pending, all from #132's own row). Updated `Related SRS` from v5.11 to v5.12 | Pending |
| 1.56 | 2026-08-08 IST | QA Manager | Added FR-AUTH-12 (#91, TOTP-based 2FA — AUTH-02): QR provisioning, TOTP login verification, 8 one-time recovery codes, TOTP-verified disable. The issue's own "SRS AUTH-10" citation pointed at an existing, unrelated requirement (BCrypt hashing) rather than a stale/nonexistent range — a wrong-target citation, distinct from SEC-15/COMP-01/RET-01's wrong-range shape but the same underlying filing-time traceability-mismatch pattern. ✅ Implemented: `TwoFactorController` (`/api/user/2fa/enable|verify|disable`, following the established `/api/user/**` convention over the issue's own unmatched `/api/v1/users/2fa/...` path — same sibling precedent as #88), `TwoFactorServiceImpl` (RFC 6238 via `dev.samstevens.totp`, BCrypt-hashed one-time recovery codes), `AuthServiceImpl.login()` 3-arg overload (returns `twoFactorRequired=true` with no tokens when a 2FA-enabled account omits the code). Tested via `TwoFactorServiceImplTest` (unit, real TOTP code generation/verification against the library, not mocked). Recomputed the Authentication (FR-AUTH) Coverage Summary row (11→12 total, 9→10 Implemented) and the Totals row (201→202 total, 144→145 Implemented). Updated `Related SRS` from v5.12 to v5.13 | Pending |
| 1.57 | 2026-08-08 IST | QA Manager | Added §7.10 Observability Requirements (OBS-02, #108, distributed tracing — Micrometer + OTLP export to Grafana Tempo, see ADR-0004). The issue's own "RTM MON-05" citation referenced an ID that did not exist at filing time (only FR-MON-05, an unrelated Prometheus-metrics requirement, exists); also caught before use: "OPS-01"/"OPS-02" were already informally claimed by SDD's own #119/#120 revision notes for unrelated deployment-topology work, a real ID collision if reused. Adopted "OBS-02" instead, matching the domain code #108's own GitHub issue title carries, consistent with sibling issues #107 (OBS-01) and #109 (OBS-03) — same filing-time traceability-mismatch shape as SEC-15/COMP-01/RET-01/FR-AUTH-12, caught before the wrong ID was actually written into either document. ✅ Implemented: `micrometer-tracing-bridge-otel`/`opentelemetry-exporter-otlp` (`backend/pom.xml`), `management.tracing.*` config, Tempo service + Grafana datasource (`backend/docker-compose.yml`, `backend/tempo/tempo.yaml`, `backend/grafana/provisioning/datasources/tempo.yml`). Tested via `TracingWiringIntegrationTest` (real-context, asserts a genuine `OtelTracer` bean, not a mocked no-op) and live-verified against a running Tempo instance (`GET /api/public/products`, `GET /api/auth/csrf`, and a scheduled `@Async` job all produced real queryable traces). Added new Coverage Summary row "Observability (OBS-02)" (1/1/0/0/0/0) and recomputed the Totals row (202→203 total, 145→146 Implemented). Corrected the stale `Related SRS` pointer (was v5.12, actual current v5.13 before this revision's own v5.14 bump — same cross-reference-mesh drift class already documented elsewhere in this log) to v5.14 | Pending |
| 1.58 | 2026-08-08 IST | QA Manager | #113's own "(SEC-04)" citation pointed at an existing, unrelated requirement — SEC-04 is "CSRF configured for SPA" (row 471), not JWT lifetime/rotation — a wrong-target citation, same underlying filing-time traceability-mismatch pattern as SEC-15/COMP-01/RET-01/FR-AUTH-12/OBS-02 above but discovered before implementation began (not just before the wrong ID was written). #113's actual work maps to the already-`✅ Implemented` FR-AUTH-03 (JWT access-token 15-min expiry) and FR-AUTH-06/FR-AUTH-07 (rotation, logout invalidates refresh token) rows — no new requirement, no status change, only stronger test evidence: extended FR-AUTH-03's Test citation with `AuthenticationAuthorizationSecurityTest`'s new `testGenuinelyExpiredValidlySignedJwtIsRejected` (a validly-signed-but-expired token through the real filter chain, closing a gap where the pre-existing test only used a malformed-signature token) and FR-AUTH-06's `AuthApiTest` citation with `testRefreshTokenRotationRevokesOldToken` (real e2e rotate-then-reuse-rejected flow, no mocks — the pre-existing rotation test only checked a bogus string against a mocked empty lookup). No Coverage Summary/Totals recomputation — citation-only extension, no status changes | Pending |
| 1.59 | 2026-08-08 IST | QA Manager | Added §7.10 Observability Requirements row OBS-05 (#123, Kubernetes readiness/liveness health probes) — the issue's own "SRS NFR-OPS-06" citation both did not exist at filing time and collided with #124's already-used "OPS-06" (revision 1.51 above). Adopted "OBS-05", continuing this section's established `OBS-*` numbering (OBS-01/03/04 remain #107/#109's scope) rather than the mismatched `OPS-*` deployment-topology domain. ✅ Implemented: `ElasticsearchHealthIndicator` added alongside the pre-existing `DatabaseHealthIndicator`/`RedisHealthIndicator`; `management.endpoint.health.group.readiness.include` now genuinely wires all three into the readiness probe's aggregate status (previously only the built-in `readinessState` contributor was in that group, so the pre-existing custom indicators never actually gated readiness). Updated the Coverage Summary row "Observability (OBS-02, OBS-05)" (1→2) and recomputed the Totals row (203→204 total, 146→147 Implemented). Updated `Related SRS` to v5.15 | Pending |
| 1.60 | 2026-08-09 IST | QA Manager | Added §7.6 Maintainability Requirements row MNT-07 (#127, publish the generated OpenAPI 3.1 spec to GitHub Pages via Swagger UI, automated on each release tag) — the issue's own "SRS NFR-MAINT-01" citation referenced an ID that did not exist at filing time (this section uses the `MNT-*` prefix). Adopted `MNT-07`, the next free ID in the existing sequence. 🔵 Pending — implementation complete (`.github/workflows/publish-api-docs.yml`, PR #712) but not yet live-verified via a real tag push at the time of this revision; will be updated to ✅ Implemented once the published GitHub Pages URL is confirmed serving the spec post-merge. Updated the Coverage Summary row "Maintainability (MNT)" (6/6/0/0/0/0 → 7/6/0/1/0/0) and recomputed the Totals row (204→205 total, 43→44 Pending). Updated `Related SRS` to v5.16 | Pending |
| 1.10 | 2026-07-18 20:50 IST | QA Manager | FR-FE-25 (admin order management) already ✅ Implemented but only cited `OrdersTab.tsx` with a generic "Vitest" test reference, predating a dedicated test file. Added `RefundModal.tsx` and named `OrdersTab.test.tsx`/`RefundModal.test.tsx` now that the refund action genuinely exists (#438) — status stays ✅ Implemented (no count changes; closes a citation gap rather than changing status). Updated the `Related SRS` cross-reference from v4.5 to v4.6 following SRS's own FR-FE-25 requirement-text extension in the same fix | Pending |
| 1.11 | 2026-07-18 23:20 IST | QA Manager | FR-ADM-03 (admin manages user accounts — view, update, deactivate) corrected from 🔵 Pending Ph-2 to ✅ Implemented: the backend (`AdminUserController` GET/PUT `/{id}`, `AdminServiceImpl.updateUserByAdmin`) was already fully implemented and tested (`AdminUserControllerTest`), but the frontend `UsersTab.tsx` only wired list + delete; added `UserDetailModal.tsx` (view + edit) and its wiring, with `UserDetailModal.test.tsx`/`UsersTab.test.tsx` coverage (#439). Recomputed the Admin Operations (FR-ADM) Coverage Summary row (3→4 Implemented, 4→3 Pending) and the Coverage Summary Totals row (114→115 Implemented, 50→49 Pending) | Pending |
| 1.12 | 2026-07-19 02:00 IST | QA Manager | FR-PROD-08 and FR-FE-23 were both already ✅ Implemented, backend-only for FR-PROD-08 (variant CRUD had no admin UI) and citation-incomplete for FR-FE-23. Added `ProductVariantsModal.tsx`/`ProductVariantsModal.test.tsx` (the new admin variant-management UI) to both rows' citations, and `ProductVariantRepositoryTest` (new regression test for a live-verified `LazyInitializationException` bug found while building the UI — `ProductVariantRepository.findByProductId`'s `@EntityGraph` was missing `"product"`, plus a related missing-`updatedAt`-on-create NOT NULL bug and a `Product.tags` serialization leak, all fixed in the same change) to FR-PROD-08 (#427). Status stays ✅ Implemented on both rows — citation/evidence gap closure, no count changes | Pending |
| 1.13 | 2026-07-19 06:00 IST | QA Manager | Added FR-ADM-10 (admin CRUD for product tags) — no RTM row existed for tag management at all, matching SRS's newly-added FR-ADM-10 (same fix, v4.7). Backend (`AdminProductTagController`, `ProductTagServiceImpl`) was already complete; added the new frontend `TagsTab.tsx`/`TagFormModal.tsx` consuming it, with `TagsTab.test.tsx` coverage (#429). Recomputed the Admin Operations (FR-ADM) Coverage Summary row (8→9 total, 4→5 Implemented) and the Coverage Summary Totals row (180→181 total, 115→116 Implemented). Updated `Related SRS` from v4.6 to v4.7 | Pending |
| 1.14 | 2026-07-19 09:00 IST | QA Manager | Added FR-ADM-11 (admin CRUD for coupons: list, create, deactivate) — no RTM row existed for coupon admin management, matching SRS's newly-added FR-ADM-11 (same fix, v4.8). `AdminCouponController` had no `GET`/list endpoint at all before this issue (only `POST`/`DELETE`, confirmed via source read) — added in the same change (#435), since a table/list-based admin UI cannot function without one, matching every sibling admin CRUD UI's shape. Live browser verification also surfaced a genuine, pre-existing Liquibase schema-drift defect (two conflicting `coupons`-table changesets) blocking coupon creation entirely, fixed via a new reconciling migration in the same PR. Recomputed the Admin Operations (FR-ADM) Coverage Summary row (9→10 total, 5→6 Implemented) and the Coverage Summary Totals row (181→182 total, 116→117 Implemented). Updated `Related SRS` from v4.7 to v4.8 | Pending |
| 1.15 | 2026-07-19 10:40 IST | QA Manager | Added FR-CHK-09 (apply coupon/discount code during checkout) — the backend endpoint (`MultiStepCheckoutController.applyCoupon`, `CheckoutServiceImpl.applyCoupon`) shipped under #77 and was already documented in SRS Appendix A.10, but no RTM row was ever added for it, unlike its sibling admin-side requirement (FR-ADM-11). Frontend wiring (coupon input on the checkout `ShippingStep`, discount reflected in `CheckoutPage`/`PaymentStep` totals) added in this change (#436), with `ShippingStep.test.tsx`/`PaymentStep.test.tsx` coverage. Live browser verification (Chrome DevTools MCP) caught a genuine bug invisible to the mocked RTL unit tests: the coupon input was originally nested inside `ShippingStep`'s outer `<form>`, an invalid-HTML nesting that caused the "Apply" button to submit the outer form (real page navigation) instead of calling the coupon handler — fixed by converting it to a plain `<div>` with a `type="button"` click handler. Recomputed the Checkout & Orders (FR-CHK) Coverage Summary row (8→9 total, 7→8 Implemented) and the Coverage Summary Totals row (182→183 total, 117→118 Implemented). Updated `Related SRS` from v4.8 to v4.9 and `Related SDD` from v3.5 to v3.6 (SDD's own §4.7.3 endpoint-catalogue row for this endpoint cited the wrong FR, fixed in the same pass) | Pending |
| 1.16 | 2026-07-19 17:45 IST | QA Manager | FR-REV-01 (submit product review with star rating) was already ✅ Implemented but backend-only — `ProductReviewController`/`ProductReviewServiceImpl` existed and were tested, but no frontend write path existed anywhere (#441). Added `frontend/src/components/product/WriteReviewForm.tsx` and extended the citation; test class list extended with `WriteReviewForm.test.tsx`. Status stays ✅ Implemented — citation/evidence gap closure, no count changes (same shape as 1.12/1.13). Live browser verification during this issue also surfaced and fixed four genuine, pre-existing backend defects blocking review submission/listing entirely: `Product.tags`/`variants` cached as non-deserializable Hibernate collection types via Redis (`ProductServiceImpl`), `Inventory.getAvailableQuantity()` (a derived getter) failing the same cache round-trip, `ProductReview.user` never initialized before serialization (`LazyInitializationException`, "no session"), and — most notably — `User.password` having **no `@JsonIgnore` anywhere in the codebase**, a latent password-leak risk that `ProductReview.user` would have been the first endpoint to actually trigger; also found and fixed `Role.users`/`Permission.roles` as unguarded lazy back-references reachable through `User.roles` (EAGER). `Related SRS`/`Related SDD` unchanged (FR-REV-01's requirement text itself did not change, only implementation evidence) | Pending |
| 1.17 | 2026-07-19 22:30 IST | QA Manager | FR-FE-20 (wishlist page) was already ✅-equivalent (🟡 Partial, tab-not-route) but only cited `WishlistTab.tsx`; the issue's own three named endpoints (`contains/{productId}`, `count`, `clear-all`) were unused by the frontend (#442). Extended the citation with `WishlistButton.tsx` (add/remove/contains toggle) and `Navbar.tsx` (count badge). Live browser verification also surfaced and fixed a 6th occurrence of the `raw-entity-with-lazy-collection-...` bug family: `WishlistServiceImpl.getWishlistProducts` returned raw `Product` entities with uninitialized lazy fields, throwing on every real `GET /api/user/wishlist` call — fixed via `Hibernate.initialize()`, new `WishlistServiceImplLazyLoadingTest` regression test added. Status stays 🟡 Partial (the tab-vs-route reason is unchanged) — citation/evidence gap closure, no count changes | Pending |
| 1.18 | 2026-07-20 06:00 IST | QA Manager | FR-INV-01 (#443) — `InventoryStatusController`'s `/status`/`/details`/`/available` endpoints were unused by the frontend; also found not actually reachable by unauthenticated visitors despite the controller's own "Public controller" javadoc (`SecurityConfig` never listed `/api/inventory/**` in `permitAll()`). Widened `SecurityConfig`/`TestSecurityConfig` to `permitAll()` the 3 GET endpoints, wired `/status` into `ProductDetailPage.tsx` (via new `useInventoryStatus.ts`/`api/inventory.ts`); listing-page (`ProductCard`) wiring deliberately deferred — no bulk endpoint exists, would be N+1 per card. Extended FR-INV-01's citation accordingly. Status stays ✅ Implemented (backend requirement was already satisfied; this closes the citation/reachability gap) — no count changes | Pending |
| 1.19 | 2026-07-20 22:20 IST | QA Manager | FR-ADM-07 (admin manages webhook subscriptions) corrected from 🔵 Pending Ph-2 to ✅ Implemented: `WebhookAdminController`/`WebhookServiceImpl` were already fully implemented and tested, but had no frontend consumer (#446, direct 1:1 mirror of the Shipping-methods admin tab pattern, #445/PR #519). Added `WebhookSubscriptionsTab.tsx`/`WebhookSubscriptionFormModal.tsx` (create-only — no full-update endpoint exists on the backend, unlike Shipping/Categories) and extended the citation. Recomputed the Admin Operations (FR-ADM) Coverage Summary row (6→7 Implemented, 3→2 Pending), the Coverage Summary Totals row (118→119 Implemented, 49→48 Pending), and the §12 Phase 2 Admin full-suite Started/Not-Started counts (1→2 Started, 5→4 Not Started) and Phase 2 total (36→37 Started, 45→44 Not Started) | Pending |
| 1.20 | 2026-07-21 12:00 IST | QA Manager | FR-ADM-01 (sales analytics dashboard for admins) corrected from 🔵 Pending Ph-2 to ✅ Implemented: `SalesAnalyticsController`/`SalesAnalyticsServiceImpl` were already fully implemented and tested, but had no frontend consumer (#431, direct 1:1 mirror of the Overview/Webhooks/Users admin-tab stat-card pattern — no chart library, custom CSS bars). Added `SalesAnalyticsTab.tsx` (revenue/conversion/cart-abandonment/AOV stat cards, revenue trend, top products, revenue-by-category, and a customer-lifetime-value lookup) with `SalesAnalyticsTab.test.tsx` coverage, and extended the citation. Recomputed the Admin Operations (FR-ADM) Coverage Summary row (7→8 Implemented, 2→1 Pending), the Coverage Summary Totals row (119→120 Implemented, 48→47 Pending), and the §12 Phase 2 Admin full-suite Started/Not-Started counts (2→3 Started, 4→3 Not Started) and Phase 2 total (37→38 Started, 44→43 Not Started) | Pending |
| 1.21 | 2026-07-21 14:00 IST | QA Manager | FR-ADM-02 (inventory analytics and reports) corrected from 🔵 Pending Ph-2 to ✅ Implemented: `AdminInventoryAnalyticsController`/`InventoryAnalyticsService` were already fully implemented and tested, but had no frontend consumer (#432, direct 1:1 mirror of the FR-ADM-01/#431 `SalesAnalyticsTab.tsx` stat-card + list pattern — no chart library). Added `InventoryAnalyticsTab.tsx` (high-demand/low-stock list, seasonal demand patterns, stock turnover, and an on-demand predictive restocking plan) with `InventoryAnalyticsTab.test.tsx` coverage, and extended the citation. `FR-INV-07` (which additionally covers `InventoryReportService`/`AdminInventoryReportController`, out of #432's scope) stays 🔵 Pending Ph-2 — not marked Implemented by this change. Recomputed the Admin Operations (FR-ADM) Coverage Summary row (8→9 Implemented, 1→0 Pending), the Coverage Summary Totals row (120→121 Implemented, 47→46 Pending), and the §12 Phase 2 Admin full-suite Started/Not-Started counts (3→4 Started, 3→2 Not Started) and Phase 2 total (38→39 Started, 43→42 Not Started) | Pending |
| 1.22 | 2026-07-21 18:10 IST | QA Manager | FR-INV-07 (admin inventory analytics and reports) corrected from 🔵 Pending Ph-2 to ✅ Implemented: `AdminInventoryReportController`/`InventoryReportService` (the report/breach half of FR-INV-07, left explicitly out of scope by #432/1.21) had no frontend consumer (#433, direct 1:1 mirror of the FR-ADM-02/#432 `InventoryAnalyticsTab.tsx` admin-dashboard-tab pattern). Added `InventoryThresholdsTab.tsx` (summary stats, below-threshold products with inline minimum-level editing via `AdminInventoryThresholdController.setProductThreshold`, threshold breach list, frequently low-stock products) with `InventoryThresholdsTab.test.tsx` coverage, and extended the FR-INV-07 citation. FR-ADM-06 (admin configures inventory alert thresholds) citation also extended to the same new component — status stays 🟡 Partial, since only product-level threshold configuration shipped; category-level threshold configuration and the category-inheritance toggle (`AdminInventoryThresholdController.setCategoryThreshold`/`useProductCategoryThreshold`) remain deferred, tracked as a follow-up issue. Note: the issue's own body cited `AdminThresholdController` as an alternate controller name for this feature — verified against source and found incorrect; that controller is unrelated (system-monitoring CPU/memory/error-rate thresholds), not inventory. Recomputed the Inventory (FR-INV) Coverage Summary row (5→6 Implemented, 2→1 Pending), the Coverage Summary Totals row (121→122 Implemented, 46→45 Pending), and the §12 Phase 2 "Auth / Safety / Checkout / Inventory Ph-2" Started/Not-Started counts (0→1 Started, 10→9 Not Started) and Phase 2 total Started/Not-Started (39→40 / 42→41) | Pending |
| 1.23 | 2026-07-22 09:30 IST | QA Manager | FR-INV-04 (admin updates stock quantities): eliminated the `Product.stockQuantity`/`Inventory` dual source of truth (#485, follow-up from #309) — `Product.stockQuantity` is no longer a persisted column, only a derived getter reading `Inventory.quantityInStock`, so there is exactly one writable representation of stock. Confirmed the drift was already live (not hypothetical): `ProductServiceImpl.updateProduct()` wrote `Product.stockQuantity` from the request without touching `Inventory`, desyncing the two on every ordinary edit. `CreateProductRequest.stockQuantity` is now create-only; stock changes on an existing product must go through `AdminInventoryController`'s adjust-inventory endpoint. Added `ProductInventorySingleSourceOfTruthIT` as the regression guard (real H2 persistence, not mocks — proves an update carrying a different `stockQuantity` does not change the persisted `Inventory` row). Extended FR-INV-04's Test citation; status stays ✅ Implemented (no functional capability changed, only the storage/consistency guarantee) — no count changes |
| 1.24 | 2026-07-22 10:30 IST | QA Manager | FR-INV-03 (admin adds stock) / FR-INV-04 (admin updates stock quantities): closed a server-side floor-validation gap (#487, discovered during #440) — `AdminInventoryController.addStock()`/`updateStock()`'s `@RequestParam Integer quantity` had no `@Min(0)`, and `InventoryServiceImpl.addStock()`/`updateStock()` performed no defensive check either, so a negative value (bypassing the frontend's client-side guard, e.g. via a raw API client) could push `Inventory.quantityInStock` negative with no validation error. Fixed via `@Min(0)` + `@Validated` on the controller (added a `ConstraintViolationException` handler to `GlobalExceptionHandler`, which didn't exist before) and a defensive service-layer check in both methods, matching this repo's existing defense-in-depth pattern (`@PreAuthorize` at both URL and service layers). Both Test citations already list `AdminInventoryControllerTest`/`InventoryServiceImplTest` — no new test class added, both extended with negative-quantity cases (2 MockMvc tests proving real bean-validation binding, 3 unit tests including a corrupted-pre-existing-row scenario). Status stays ✅ Implemented for both (no functional capability changed, only the validation guarantee) — no count changes |
| 1.25 | 2026-07-22 16:00 IST | QA Manager | **Marketplace pivot addendum**, RTM counterpart to SRS v5.0/SDD v4.0's FG-11/FG-12 addendum. Added §6.11 (Seller & Marketplace Management, FR-SEL-01–08) and §6.12 (Location-Based Matching, FR-LOC-01–04), all rows ⬜ Not Started — nothing in this addendum is implemented. Added both new categories to the Coverage Summary (§3) as separate rows explicitly excluded from the existing Totals row, matching the same treatment SRS §4.2/SDD §7 already gave this addendum. Also found and fixed pre-existing cross-reference drift independent of this addendum: `Related SDD` was stale at v3.6 (actual current v3.8 before this revision's own v4.0 bump) and `Related TP` was stale at v4.2 (actual current v4.3) — both corrected alongside the `Related SRS` 4.9→5.0 update this addendum itself required | Pending |
| 1.26 | 2026-07-22 17:30 IST | QA Manager | FR-SEL-01 (seller registration, distinct from a buyer account) corrected from ⬜ Not Started to ✅ Implemented (#553, first requirement implemented from the Ph-3 marketplace-pivot addendum): `Seller` entity/Liquibase changeset (1:1 extension of `User`, mirrors `Address`), `SellerServiceImpl.registerSeller`/`SellerController`, granting `ROLE_SELLER` on registration. `district_id` is a deliberately deferred/nullable column — the SDD's own `Seller ──[N:1]──► District` FK depends on ADR #561 (OQ-01/OQ-02), still open; registration does not functionally require it. Recomputed the Seller & Marketplace (FR-SEL) Coverage Summary row (0→1 Implemented, 8→7 Not Started) — Totals row unaffected (this category stays explicitly excluded per 1.25) | Pending |
| 1.27 | 2026-07-22 19:00 IST | QA Manager | FR-SEL-02 (admin verification/approval before a seller can list products) corrected from ⬜ Not Started to ✅ Implemented (#554): `AdminSellerController` (`GET /api/v1/admin/sellers?status=`, `PATCH /{id}/verification-status`, `@PreAuthorize("hasRole('ADMIN')")`, mirroring `AdminOrderController.updateOrderStatus`'s sibling precedent), `SellerServiceImpl.updateVerificationStatus` (PENDING→VERIFIED/REJECTED transition guard, mirroring `OrderServiceImpl`'s `VALID_TRANSITIONS` pattern), `SellerRepository.findByVerificationStatus`, and a seller-facing email notification on decision. Live-verified against a running instance: 403 for a non-admin caller, 200 with correct state change for an admin VERIFIED decision, 400 for an already-VERIFIED→VERIFIED re-attempt, 404 for an unknown seller ID. Recomputed the Seller & Marketplace (FR-SEL) Coverage Summary row (1→2 Implemented, 7→6 Not Started) — Totals row unaffected (this category stays explicitly excluded per 1.25) | Pending |
| 1.28 | 2026-07-22 20:30 IST | QA Manager | FR-SEL-03 (each product associated with exactly one owning seller) / FR-SEL-04 (seller manages own product listings only) corrected from ⬜ Not Started to ✅ Implemented (#555): reactivated the dormant `product.supplier_id`/`fk_product_supplier` FK (already present in the original bootstrap schema, never mapped onto the JPA entity — per SDD v4.0's own finding) as `Product.seller`, no new Liquibase changeset needed. Added `SellerProductController` (`/api/user/seller/products`, `ROLE_SELLER`-gated CRUD) and `ProductServiceImpl.createProductForSeller`/`updateProductForSeller`/`deleteProductForSeller`/`getProductsForSeller`, enforcing (a) only a `VERIFIED` seller may create products, (b) a seller can only read/update/delete their own products (`ProductRepository.findByIdAndSeller_Id`). Verified via `ProductServiceImplTest` (ownership/verification-gate unit tests) and a real H2-backed `ProductRepositoryTest` addition proving the FK mapping itself round-trips (framework-mapping risk, not just service logic). Recomputed the Seller & Marketplace (FR-SEL) Coverage Summary row (2→4 Implemented, 6→4 Not Started) — Totals row unaffected (this category stays explicitly excluded per 1.25) | Pending |
| 1.29 | 2026-07-25 18:00 IST | QA Manager | FR-SEL-05 (seller manages inventory for their own products only) corrected from ⬜ Not Started to ✅ Implemented (#556): mirrors #555's ownership-scoping pattern — added `SellerInventoryController` (`/api/user/seller/inventory`, `ROLE_SELLER`-gated) and `InventoryServiceImpl.getInventoryForSeller`/`adjustStockForSeller`, enforcing ownership via `InventoryRepository.findByProduct_IdAndProduct_Seller_Id` (404 if the product belongs to another seller). No new Liquibase changeset — reuses the existing `inventory`/`product` schema and `Product.seller` mapping from #555. Verified via `InventoryServiceImplTest`/`SellerInventoryControllerTest` (Mockito-mocked; simple derived Spring Data queries, no custom JPQL, no framework-level risk requiring a real-context test). Recomputed the Seller & Marketplace (FR-SEL) Coverage Summary row (4→5 Implemented, 4→3 Not Started) — Totals row unaffected (this category stays explicitly excluded per 1.25) | Pending |
| 1.30 | 2026-07-25 20:00 IST | QA Manager | FR-SEL-06 (seller views/manages orders containing their own products only) moved from ⬜ Not Started to 🟡 In Progress (#578, first of three sub-issues under parent #557): a cart spanning multiple sellers currently produces one shared `Order` — a real architectural decision, confirmed to have no repo precedent via `gh search`, was resolved by explicit user choice to split a multi-seller cart into one `Order` per seller at checkout, linked by a new `OrderGroup` parent (rejected keeping one shared Order with a read-only per-seller item filter, since it leaves order-status/fulfillment ownership ambiguous when sellers ship independently). This revision covers only #578's schema/entity scope: new `order_groups` table + nullable `orders.order_group_id` FK (additive-only, no backfill needed — existing orders keep `order_group_id = NULL`), `OrderGroup` entity, `Order.orderGroup` mapping. #579 (checkout split) and #580 (seller-scoped order API) remain ⬜ Not Started sub-issues; FR-SEL-06 will move to ✅ Implemented only once all three close. Verified via a real H2-backed `OrderRepositoryTest` addition (framework/mapping-level risk per `testing.md`'s third `smoke-sanity-regression-test` sub-case), not a mocked unit test. Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) |
| 1.31 | 2026-07-25 22:45 IST | QA Manager | FR-SEL-06 stays 🟡 In Progress: #579 (second of three sub-issues under parent #557, checkout-split) closes — `CheckoutServiceImpl`'s 3 order-creation call sites (`initiatePayment`, `checkoutCart`, `checkoutWithPayment`) now group cart items by `product.seller` and create one `Order` per seller, linked via #578's `OrderGroup` schema; a single-seller cart still produces exactly one order with no `OrderGroup`. Shipping/discount apportioned proportionally to each seller's subtotal share (documented trade-off, precise seller-negotiated splitting deferred); payment stays a single combined charge against the group total via the primary order. Adds `OrderGroupRepository`, `OrderRepository.findByOrderGroupId`. #580 (seller-scoped order API) remains ⬜ Not Started; FR-SEL-06 moves to ✅ Implemented only once it closes too. Verified via unit tests (service-layer splitting/apportionment logic — no new framework wiring beyond #578's already-tested JPA mapping). Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) | Pending |
| 1.32 | 2026-07-26 09:00 IST | QA Manager | FR-SEL-06 moves to ✅ Implemented: #580 (seller-scoped order API, third and final sub-issue under parent #557) closes — new seller-facing `GET /api/user/seller/orders` (paginated list), `GET /api/user/seller/orders/{id}` (detail), `PATCH /api/user/seller/orders/{id}/status` endpoints, scoped via a new `OrderRepository.findBySellerId`/`findByIdAndSellerId` `EXISTS`-subquery (`Order` carries no direct seller reference; ownership derived transitively via `OrderItem.product.seller` since #579's checkout split guarantees every item in one `Order` belongs to a single seller). Reuses `OrderServiceImpl`'s existing `VALID_TRANSITIONS` state machine, which already excludes `PAID`/`PAYMENT_FAILED` as reachable targets, so a seller can never set a payment-webhook-only status. Verified via unit tests (ownership/transition logic) and a real H2-backed `@DataJpaTest` addition to `OrderRepositoryTest` proving the `EXISTS`-subquery scoping actually filters (query-logic risk — a mocked service-layer test only proves parameter pass-through). All three FR-SEL-06 sub-issues (#578/#579/#580) now closed. Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) | Pending |
| 1.33 | 2026-07-26 10:30 IST | QA Manager | FR-SEL-06 frontend completes: #581 (buyer order-group view + seller order management UI, linked follow-up to closed parent #557) adds `account/OrdersTab.tsx`'s "1 purchase, N shipments" grouping (via a new `orderGroupId` field added to `OrderResponseDTO`, populated by both `OrderServiceImpl` and `CheckoutServiceImpl`) and a new seller-facing `/seller` route/dashboard consuming #580's existing API. FR-SEL-06 status stays ✅ Implemented (already backend-complete since 1.32) — this revision only extends the row's Related Code/Test citations to the frontend layer, since the requirement itself ("seller views/manages orders") is now fully realized end-to-end rather than API-only. Verified via backend unit tests (`orderGroupId` null/non-null mapping) and 11 new/updated frontend RTL tests. Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) | Pending |
| 1.34 | 2026-07-28 17:00 IST | QA Manager | FR-SEL-07 implemented: #558 adds buyer-to-seller ratings (`SellerReview` entity/table, `SellerReviewController`, `SellerReviewServiceImpl` — mirrors the existing `ProductReview` pattern, scoped by seller's User.id per `Product.seller`/`SellerOrderController`'s established convention) and a frontend `SellerReviewPanel` surfaced from a delivered order's detail view. Required adding `sellerId` to `OrderResponseDTO` (backend-only-sub-issue-chain DTO gap, same class as #581's `orderGroupId` fix) so the frontend could link an order to its seller. Status ⬜ Not Started → ✅ Implemented. Verified via 20 backend unit tests, 4 frontend RTL tests, and a real H2-backed Spring context load (`CivilEcommerceApplicationTests`) confirming the new entity/changeset's schema mapping. Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) | Pending |
| 1.35 | 2026-07-28 19:30 IST | QA Manager | FR-SEL-08 implemented: #559 audited the existing FR-SEL-04/05/06 implementations (`SellerProductController`/`SellerInventoryController`/`SellerOrderController`) and confirmed data isolation was already correctly enforced — class-level `@PreAuthorize("hasRole('SELLER')")` plus service-layer ownership-scoped repository queries (`findByIdAndSeller_Id`/`findByIdAndSellerId`/`findByProduct_IdAndProduct_Seller_Id`) throwing `AccessDeniedException`/`ResourceNotFoundException` on mismatch, never fetch-then-check, already fully unit-tested. The real gap: no test previously exercised `@PreAuthorize` through the real Spring Security filter chain/method-security proxy — every existing controller test constructed the controller directly with a mocked service, bypassing the proxy entirely (the same class of risk as the self-invocation-bypasses-@PreAuthorize lesson). Closed via a new `SellerDataIsolationIntegrationTest` (`@SpringBootTest`+MockMvc, real filter chain) proving non-SELLER rejection, owner access, and cross-seller rejection end-to-end for products/inventory/orders. Status ⬜ Not Started → ✅ Implemented. Totals row unaffected (FR-SEL stays explicitly excluded per 1.25) | Pending |
| 1.36 | 2026-07-28 IST | QA Manager | §6.12 (FR-LOC-01–04) updated: OQ-01/OQ-02 resolved via [ADR 0001](../design/adr/0001-district-matching-strategy-for-location-based-seller-buyer-matching.md) (#561) — radius/seller-declared matching (`Seller ──[N:M]──► District`), fixed admin-maintained reference table. Removed the "cannot be finalised" blocking note; FR-LOC-01–04 rows stay ⬜ Not Started (design resolved, not yet implemented — #562/#563/#564 remain the implementation issues). Corrected `Related SRS` (5.1→5.2) and `Related SDD` (stale at 4.1, actual current 4.8 before this revision — same recurring cross-reference-mesh drift class already documented, corrected here as this revision's own required edge rather than deferred to the periodic 15-issue sync) | Pending |
| 1.37 | 2026-07-29 IST | QA Manager | FR-LOC-01/02 implemented (#562): `District`/`SellerDistrict` entities, `seller_districts` join table, buyer's own `users.district_id` derived from `Address`. §6.12 rows moved ⬜ Not Started → ✅ Implemented with real Implementation/Test-Class citations (`DistrictRepositoryTest`, `DistrictServiceImplTest`). Recomputed the Location-Based Matching (FR-LOC) Coverage Summary row (0→2 Implemented, 4→2 Not Started); Totals row unaffected (FR-LOC stays explicitly excluded per 1.25). Verified via a real H2-backed `@DataJpaTest` for the FK/unique-constraint mapping (framework-level risk per `testing.md`'s third `smoke-sanity-regression-test` sub-case) plus Mockito service-layer tests. Updated `Related SRS` (5.2→5.3) and `Related SDD` (4.8→4.9) | Pending |
| 1.38 | 2026-07-29 IST | QA Manager | FR-LOC-03 implemented (#563): district-scoped catalogue/search filtering on the existing Elasticsearch-backed product search (FG-02) — `ProductDocument.districtIds` populated from the owning seller's declared `SellerDistrict` links, `ProductElasticsearchRepository` districtId term-filter/derived-query variants mirroring the existing `isActive:true` filter shape, `ProductSearchServiceImpl`/`ProductControllerV2`'s new `districtId` query param. Elasticsearch path only — the JPA fallback (`elasticsearch.enabled=false`) is unaffected, per the issue's own stated scope, not an unaddressed gap. Row moved ⬜ Not Started → ✅ Implemented with real Implementation/Test-Class citations (`ProductSearchServiceTest`, `ProductControllerV2Test`). Recomputed the Location-Based Matching (FR-LOC) Coverage Summary row (2→3 Implemented, 2→1 Not Started); Totals row unaffected (FR-LOC stays explicitly excluded per 1.25). Verified via mocked unit tests (service-layer parameter threading + repository query construction — no framework/proxy risk requiring a real-context test, per `testing.md`'s tier-2 criterion) | Pending |
| 1.39 | 2026-07-29 IST | QA Manager | FR-LOC-04 implemented (#564), completing FG-12 (Location-Based Matching): checkout-time district enforcement in `CheckoutServiceImpl.validateCheckout` — a seller with declared `SellerDistrict` rows requires the buyer's `User.district` to be among them, fail-closed when the buyer's district can't be determined, unrestricted when the seller has no declared districts. `validateCheckout` gained `@Transactional(readOnly = true)` since the new check lazily loads `Product.seller`, and the pre-existing controller-direct call path (`CheckoutController.validateCheckout`) had no transaction of its own — confirmed via a real, non-ambient-transaction `@SpringBootTest` (`CheckoutValidateNoAmbientTransactionIT`) that this would otherwise throw `LazyInitializationException`. Row moved ⬜ Not Started → ✅ Implemented with real Implementation/Test-Class citations. Recomputed the Location-Based Matching (FR-LOC) Coverage Summary row (3→4 Implemented, 1→0 Not Started); Totals row unaffected (FR-LOC stays explicitly excluded per 1.25). Updated §6.12's own header/status note from "Ph-3, Planned" to "all sub-requirements implemented". Updated `Related SRS` (5.4→5.5) and `Related SDD` (4.10→4.11) | Pending |
| 1.40 | 2026-07-29 IST | QA Manager | FR-FE-10's Implementation/Test citation extended for #516: `client.ts`'s 401-interceptor recursed indefinitely when the refresh request itself returned 401 (no valid refresh cookie — the common case for any unauthenticated visitor), confirmed live at 2,984 `POST /api/auth/refresh` requests in a few seconds. Fixed via a new `RequestOptions.skipAuthInterceptor` flag set on the `apiRefresh()`/`apiLogout()` calls themselves, bypassing the interceptor for exactly the two calls that are part of the refresh machinery. Row stays ✅ Implemented (no status change — a correctness fix to already-implemented behavior, not new coverage) | Pending |
| 1.41 | 2026-07-29 IST | QA Manager | SEC-14 (`#110`, "final verification" of the M3 CSP `unsafe-inline` removal): backend `SecurityConfig`/`SecurityHeaderPolicies.MAIN_CSP` was already `unsafe-inline`-free since #237, but `frontend/security-headers.conf` (the nginx-served CSP for the SPA's own document, a separate origin/response from the backend API's CSP) still carried `style-src 'self' 'unsafe-inline'`. Live-browser CSP verification (a static-HTML CSP probe plus a strict-header production `dist/` serve) confirmed React's `style={{}}` prop sets styles via `node.style[prop] = value` (a JS property assignment, per `react-dom-client.development.js`'s `setValueForStyles`), not via the HTML `style=` attribute — so it is not subject to `style-src`'s inline restriction, and the frontend's 7 `style={{}}` usages across 4 components required no code change. Removed `'unsafe-inline'` from `style-src` in `security-headers.conf`; rebuilt frontend `dist/` and re-verified zero CSP console violations. SEC-14 corrected from 🟡 Partial to ✅ Implemented; recomputed the Security Coverage Summary row (9→10 Implemented, 2→1 Partial) and the Coverage Summary Totals row (122→123 Implemented, 16→15 Partial) accordingly, plus the §12 Phase 2 Security Started/Not-Started counts (1→2 Started, 4→3 Not Started) | Pending |
| 1.42 | 2026-07-29 IST | QA Manager | Added SEC-15 (#111): full OWASP Top 10 (2021) assessment (A01-A10), performed against the local dev stack (no staging environment exists yet — `development-workflow.md` step 31, confirmed via user decision) using OWASP ZAP full active scan (141 automated checks, 0 Fail/Warn, 1 Informational) plus direct code/live-endpoint review. One Medium finding (A10 SSRF: `WebhookServiceImpl`'s admin-supplied `targetUrl` had no private-IP/loopback blocklist) fixed in the same PR via a new `SsrfUrlValidator` component, with dedicated unit tests. Zero Critical/High findings — see `docs/SDLC-docs/reports/security-assessment.md` for the full per-category writeup. Corrected a filing-time traceability mismatch: issue #111 was titled "(SEC-02)", but SEC-02 is an unrelated, already-Implemented requirement (JWT secret length) — SEC-15 is the correct, newly-added FR this issue satisfies. Recomputed the Security Coverage Summary row (14→15 total, 10→11 Implemented) and the Coverage Summary Totals row (183→184 total, 123→124 Implemented) | Pending |
| 1.43 | 2026-07-30 IST | QA Manager | Periodic 15-issue SDLC documentation sync (overdue — last performed at #452, 2026-07-17; 53 issues closed since, well past the 15-issue trigger). Recomputed the Seller & Marketplace (FR-SEL) Coverage Summary row directly from its 8 individual rows (376-383): all 8 are ✅ Implemented, correcting the stale "5 Implemented / 3 Not Started (Ph-3, Planned)" that no single issue's own scope had covered recomputing. Folded FR-SEL (8/8) and FR-LOC (4/4) into the Totals row per this document's own previously-stated fold-in criterion (implementation begun, OQ-01/OQ-02 resolved) — Totals 184→196 total, 124→136 Implemented. Cross-reference mesh sweep: corrected `Related SRS` (5.6→5.8, since SRS's own version is also bumped in this same sync pass) and `Related TP` (4.3→4.5, same reason); `Related SDD` was already current (4.12) before this pass, bumped to 4.13 alongside SDD's own sync edit | Pending |
| 1.44 | 2026-07-30 IST | QA Manager | FR-PAY-05 (Razorpay credentials externalised via env vars) corrected from 🔵 Pending Ph-2 to ✅ Implemented: `application.properties`' `razorpay.key.secret`/`razorpay.webhook.secret` previously carried literal string defaults (`test_key_secret`/`test_webhook_secret`) that applied in production too, since `application-production.properties` never overrode them — a hardcoded-secret-with-default violation, not genuine env-var externalisation (#114, secrets audit against SDP Appendix B / RGAR §11). Removed both defaults so a missing env var now fails startup instead of silently falling back to a known secret; also removed two redundant weak Java-level `@Value` defaults (`JwtTokenProvider.jwtSecret`, `ElasticsearchConfig.password`) masked by properties-level indirection. Added a `gitleaks` CI step (`security.yml`) and `.gitleaks.toml` allowlisting confirmed documentation/test-fixture false positives; fixed a real finding in `backend/kubernetes/buildnest-deployment.yaml` (a Secret manifest with real-looking base64 "example" values, now `stringData` placeholders). Recomputed the Payment (FR-PAY) Coverage Summary row (0→1 Implemented, 2→1 Pending) and the Coverage Summary Totals row (136→137 Implemented, 45→44 Pending) | Pending |
| 1.45 | 2026-08-01 IST | QA Manager | Added SEC-16 (HTTP security headers: HSTS/X-Frame-Options/X-Content-Type-Options/Referrer-Policy/Permissions-Policy, #112) — correcting the issue's own stale "SRS SEC-11, SEC-12" citation (unrelated: search rate limiting, JWT rotation). HSTS/X-Frame-Options/X-Content-Type-Options were already Spring Security defaults (verified via context7 against the 6.5 reference docs); Referrer-Policy and Permissions-Policy were genuinely missing, added to `SecurityHeaderPolicies`/`SecurityConfig`/`TestSecurityConfig`, and covered by 3 new `SecurityHeadersTest` assertions (5/5 pass). Recomputed the Security (SEC) Coverage Summary row (15→16 total, 11→12 Implemented) and the Coverage Summary Totals row (196→197 total, 137→138 Implemented). Updated `Related SRS` from v5.8 to v5.9 | Pending |
| 1.46 | 2026-08-01 IST | QA Manager | UI-01 status corrected from 🔵 Pending Ph-2 to ✅ Implemented — #117 added a real Playwright E2E suite (`frontend/e2e/`) per ADR 0002 (migrating off the pre-existing Selenium suite, which the row's own prior "Vitest / Playwright (Phase 2)" citation had never actually pointed to since Playwright didn't exist in the repo until this change; see #632 for the original tooling-mismatch finding). Coverage Summary Totals not recomputed this row — UI-01 was already counted, only its status/Implementation/Test-Class cells changed | Pending |
| 1.47 | 2026-08-02 IST | QA Manager | FR-PROD-06 (Redis product cache) — fixed a genuine data-integrity defect discovered during #117's Playwright CI investigation and filed as #651: `ProductServiceImpl#getProductById`'s `@Cacheable` entry corrupted the cache, causing every request after the first for a given product to fail with `SerializationException`/`UnrecognizedPropertyException`, surfacing as a false 404. Root cause: `detachCollections()` never touched the lazy `category` (`@ManyToOne`) association and only `Hibernate.initialize()`d `inventory` (`@OneToOne`) without unproxying it — both stayed Hibernate proxy instances at Redis-serialize time. Fixed by also `Hibernate.unproxy()`-ing both after initializing; a second, related defect the new regression test itself caught (`Product.getStockQuantity()`, a derived getter, serializes but can't deserialize) fixed via `@JsonIgnoreProperties(ignoreUnknown = true)` on `Product`. Added `ProductServiceRedisCacheRoundTripIntegrationTest` (real Hibernate proxies + the actual Redis serializer config, no live Redis required) to FR-PROD-06's Test citation. Status stays ✅ Implemented — defect fix + citation extension, no count changes | Pending |

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| QA Manager | _____________ | _____________ | _____________ |

---

## CONFORMANCE STATEMENT

> This document conforms to **ISO/IEC/IEEE 29148:2018** Clause 6.2.5 (Traceability) and the bidirectional traceability requirements of **ISO/IEC/IEEE 12207:2017** Section 6.4.2. Each requirement in SRS-BUILDNEST-001 v4.0 is traced forward to its design element (SDD v3.0), implementation artefact (source code), and verification evidence (test class or inspection method). Status is assessed against the Baseline Assessment Report dated 2026-06-19.

---

## 1. Purpose

This Requirements Traceability Matrix (RTM) establishes and maintains bidirectional traceability across the full software lifecycle artefact chain:

```
Stakeholder Need → SRS Requirement → SDD Design Element → Implementation Artefact → Test / Verification Evidence
```

The RTM serves to:

1. Confirm that every stated requirement has been designed, implemented, and verified.
2. Identify requirements that are unimplemented, partially implemented, or carry known defects.
3. Enable impact analysis — any change to a requirement can be traced to affected design elements, code, and tests.
4. Provide objective compliance evidence for ISO/IEC/IEEE 29148:2018, OWASP ASVS 4.0, and the project quality gate.
5. Inform Phase 1 and Phase 2 completion assessment.

---

## 2. Status Classification

| Status | Symbol | Definition |
| :--- | :--- | :--- |
| **Implemented** | ✅ | Requirement is fully implemented and verified in the current codebase |
| **Partial** | 🟡 | Requirement is partially implemented; gap identified and tracked |
| **Pending (Ph-2)** | 🔵 | Requirement is deferred to Phase 2; not expected in Phase 1 gate |
| **Open Defect** | 🔴 | Implementation exists but is known-broken; listed defect ID applies |
| **Not Started** | ⬜ | No implementation present; deferred or not yet begun |

---

## 3. Coverage Summary

| Requirement Category | Total | ✅ Implemented | 🟡 Partial | 🔵 Pending Ph-2 | 🔴 Open Defect | ⬜ Not Started |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Authentication (FR-AUTH) | 12 | 10 | 0 | 2 | 0 | 0 |
| Product Catalogue (FR-PROD) | 7 | 7 | 0 | 0 | 0 | 0 |
| Shopping Cart (FR-CART) | 6 | 6 | 0 | 0 | 0 | 0 |
| Checkout & Orders (FR-CHK) | 10 | 9 | 0 | 1 | 0 | 0 |
| Payment (FR-PAY) | 5 | 1 | 3 | 1 | 0 | 0 |
| Inventory (FR-INV) | 7 | 6 | 0 | 1 | 0 | 0 |
| Reviews & Wishlists (FR-REV, FR-WISH) | 5 | 5 | 0 | 0 | 0 | 0 |
| Admin Operations (FR-ADM) | 10 | 9 | 1 | 0 | 0 | 0 |
| Monitoring (FR-MON) | 8 | 2 | 1 | 5 | 0 | 0 |
| Frontend (FR-FE) | 31 | 21 | 7 | 3 | 0 | 0 |
| User Interfaces (UI) | 4 | 3 | 0 | 1 | 0 | 0 |
| Software Interfaces (SI) | 6 | 3 | 0 | 3 | 0 | 0 |
| Communication Interfaces (CI) | 5 | 2 | 0 | 3 | 0 | 0 |
| Usability (UR) | 8 | 5 | 0 | 3 | 0 | 0 |
| Performance (PR) | 8 | 3 | 0 | 5 | 0 | 0 |
| Reliability (REL) | 5 | 2 | 0 | 3 | 0 | 0 |
| Availability (AVL) | 4 | 1 | 0 | 3 | 0 | 0 |
| Security (SEC) | 16 | 14 | 0 | 2 | 0 | 0 |
| Compliance (COMP) | 3 | 3 | 0 | 0 | 0 | 0 |
| Maintainability (MNT) | 7 | 6 | 0 | 1 | 0 | 0 |
| Portability (PRT) | 4 | 1 | 0 | 3 | 0 | 0 |
| Scalability (SCL) | 4 | 2 | 0 | 2 | 0 | 0 |
| Safety (SAF) | 3 | 1 | 0 | 2 | 0 | 0 |
| Design Constraints (DC) | 8 | 7 | 1 | 0 | 0 | 0 |
| Test Integrity (TIR) | 5 | 4 | 1 | 0 | 0 | 0 |
| Seller & Marketplace (FR-SEL) | 8 | 8 | 0 | 0 | 0 | 0 |
| Location-Based Matching (FR-LOC) | 4 | 4 | 0 | 0 | 0 | 0 |
| Observability (OBS-02, OBS-05) | 2 | 2 | 0 | 0 | 0 | 0 |
| **Totals** | **205** | **147** | **14** | **44** | **0** | **0** |

> **Phase 1 gate posture**: 93 requirements fully implemented, 0 open defects. TIR-01 through TIR-04 and MNT-03 (previously blocking Phase 1 exit) were verified fixed on 2026-07-17 (#452) — `ProductApiTest`/`OrderApiTest` are `@Tag("e2e")`, `AuthServiceImplTest` mocks `RoleRepository`, both security-test assertions match their actual (correct) HTTP status codes, and MNT-02/TIR-05's coverage-gate values were corrected to their real, higher configured thresholds (85% JaCoCo, 77% PIT). Phase 1 is no longer blocked by test-integrity defects. (Totals recomputed directly from the 24 category rows above — the previous release's Totals row did not actually sum to its own category rows, independent of this fix.)
>
> **Ph-3 (Marketplace Expansion) posture**: all 12 requirements added by the SRS v5.0/SDD v4.0 marketplace-pivot addendum (8 FR-SEL, 4 FR-LOC) are now ✅ Implemented as of #564 (2026-07-29) — folded into the Totals row above per this section's own previously-stated criterion ("once implementation begins and the OQ-01/OQ-02 design questions are resolved"), both conditions now satisfied. Corrected during the periodic 15-issue sync (this revision): the FR-SEL row had drifted to "5 Implemented / 3 Not Started (Ph-3, Planned)" despite all 8 individual FR-SEL-01–08 rows already showing ✅ Implemented since #559 (2026-07-28) — no single issue's own scope covered recomputing this aggregate row, the exact drift class this periodic sync exists to catch.

---

## 4. RTM Legend

| Column | Description |
| :--- | :--- |
| **Req ID** | Unique requirement identifier from SRS v4.0 |
| **Description** | Concise summary of the requirement |
| **Priority** | High / Medium / Low |
| **Phase** | Ph-1 or Ph-2 |
| **SDD Reference** | Design element in SDD v3.0 (section or component name) |
| **Implementation** | Primary Java class(es) or configuration artefact |
| **Test Class(es)** | Test classes that verify this requirement |
| **Verification Method** | Test / Inspection / Analysis / Demonstration / Build |
| **Status** | Implementation + test status (see §2) |

---

## 5. External Interface Requirements

### 5.1 User Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UI-01 | React 19 SPA frontend | High | Ph-2 | §4.2.2, §4.7.4 | `frontend/src/` | Vitest (unit/component) / `frontend/e2e/` (Playwright E2E, #117) | Test | ✅ Implemented |
| UI-02 | Swagger UI at `/swagger-ui.html` | Medium | Ph-1 | §4.7.1 | `pom.xml` (SpringDoc dependency) | `HomeControllerTest`, manual | Inspection | ✅ Implemented |
| UI-03 | OpenAPI spec at `/v3/api-docs` | Medium | Ph-1 | §4.7.1 | SpringDoc AutoConfig | Manual / `HomeControllerTest` | Test | ✅ Implemented |
| UI-04 | Consistent JSON error response structure | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler` | `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | Test | ✅ Implemented |

### 5.2 Software Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SI-01 | MySQL 8.2 via HikariCP / JDBC | High | Ph-1 | §4.1.2, §4.10.3 | `application.properties` HikariCP config | `DatabaseHealthIndicatorTest`, `DatabaseConstraintTest` | Inspection | ✅ Implemented |
| SI-02 | Redis 7 via Jedis client | High | Ph-1 | §4.1.2, §4.10.3 | `CacheConfig`, `RateLimitConfig` | `RedisHealthIndicatorTest`, `RateLimiterServiceTest` | Inspection | ✅ Implemented |
| SI-03 | Elasticsearch 8.10 via Spring Data | Low | Ph-2 | §4.1.2 | `ElasticsearchConfig` | `ElasticsearchIngestionServiceTest` | Test | 🔵 Pending Ph-2 |
| SI-04 | Razorpay payment gateway | High | Ph-2 | §4.4.2, §5.1 | `RazorpayClientAdapter`, `PaymentServiceImpl` | `RazorpayClientAdapterTest`, `PaymentServiceImplTest` | Test | 🔵 Pending Ph-2 |
| SI-05 | Prometheus scrape at `/actuator/prometheus` | Medium | Ph-2 | §4.1.2, §4.10 | Micrometer AutoConfig | `PerformanceMetricsControllerTest` | Test | 🔵 Pending Ph-2 |
| SI-06 | Logstash TCP log shipper | Low | Ph-2 | §4.1.1 | `logback-spring.xml` | `LoggingStandardsTest` | Inspection | 🔵 Pending Ph-2 |

### 5.3 Communication Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| CI-01 | HTTP / HTTPS REST communication | High | Ph-1 | §4.7, §5.1 | Spring MVC controllers | All controller tests | Test | ✅ Implemented |
| CI-02 | HTTPS / TLS enforced in production | High | Ph-2 | §5.1.1 | `HttpsEnforcementFilter`, `SecurityConfig.validateHttpsInProduction()` | `SecurityTest` | Inspection | 🔵 Pending Ph-2 |
| CI-03 | CORS with configurable allowed origins | High | Ph-1 | §5.1.1 | `SecurityConfig` (CORS config) | `AuthenticationAuthorizationSecurityTest` | Test | ✅ Implemented |
| CI-04 | Health endpoint for K8s probes | High | Ph-2 | §4.1.2, §4.10.1 | Spring Boot Actuator, K8s manifests | `HealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| CI-05 | Prometheus metrics endpoint | Medium | Ph-2 | §4.1.2 | Micrometer / Actuator | `PerformanceMetricsControllerTest` | Test | 🔵 Pending Ph-2 |

---

## 6. Functional Requirements

### 6.1 Authentication and Identity Management (FG-01)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-AUTH-01 | User registration with username, email, password | High | Ph-1 | §4.3.2, §4.7.3 | `AuthController.register()`, `AuthServiceImpl.register()` | `AuthControllerTest`, `AuthServiceImplTest`, `AuthApiTest` | Test | ✅ Implemented |
| FR-AUTH-02 | Username / password authentication returning JWT + refresh token | High | Ph-1 | §4.8.1, §4.3.2 | `AuthController.login()`, `AuthServiceImpl.authenticate()`, `JwtTokenProvider` | `AuthControllerTest`, `AuthServiceImplTest`, `JwtTokenProviderTest` | Test | ✅ Implemented |
| FR-AUTH-03 | JWT access token expires after 15 minutes (900,000 ms, configurable) | High | Ph-1 | §5.1.2 | `JwtTokenProvider` — `@Value("${jwt.expiration:86400000}")` + `application.properties` (900,000) | `JwtTokenProviderTest`, `AuthenticationAuthorizationSecurityTest` (#113: real filter-chain test with a validly-signed, genuinely-expired token, distinct from the pre-existing malformed-signature test) | Test | ✅ Implemented |
| FR-AUTH-04 | JWT refresh token expires after 30 days (configurable) | High | Ph-1 | §4.9.5, §5.1.2 | `RefreshTokenServiceImpl.createRefreshToken()` | `RefreshTokenServiceTest` | Test | ✅ Implemented |
| FR-AUTH-05 | Minimum 512-bit JWT secret; fail fast on absent / weak key | High | Ph-1 | §5.1.2 | `JwtKeyValidator`, `SecurityConfig.@PostConstruct` | `JwtKeyValidatorTest`, `JwtTokenProviderTest` | Test | ✅ Implemented |
| FR-AUTH-06 | Token refresh without re-authentication; rotation on use | High | Ph-1 | §4.8.3, §4.9.5 | `AuthController.refreshToken()`, `RefreshTokenServiceImpl` | `AuthControllerTest`, `RefreshTokenServiceTest`, `AuthApiTest` (#113: `testRefreshTokenRotationRevokesOldToken` — real e2e rotate-then-reuse-rejected flow, no mocks) | Test | ✅ Implemented |
| FR-AUTH-07 | Logout invalidates refresh token | High | Ph-1 | §4.9.5 | `AuthController.logout()`, `RefreshTokenServiceImpl.deleteByUserId()` | `AuthControllerTest` | Test | ✅ Implemented |
| FR-AUTH-08 | Password reset via email (tokens expire 1 hour, configurable via `password.reset.token.expiration`; OWASP ASVS 2.5.6 secure recovery mechanism — see #339 for token-lifetime tightening) | Medium | Ph-2 | §4.7.3 | `PasswordResetController`, `PasswordResetServiceImpl`, `INotificationService.sendPasswordResetEmail()` | `PasswordResetControllerTest`, `PasswordResetServiceImplTest` | Test | 🟡 Partial (#327: real email send wired via existing `INotificationService`; live E2E delivery not yet verified; #339: 1hr expiry is looser than OWASP best-practice guidance) |
| FR-AUTH-09 | RBAC with `USER` and `ADMIN` roles | High | Ph-1 | §5.1.3, §4.3.2 | `SecurityConfig`, `RolePermissionEvaluator`, `@PreAuthorize`, `@Secured` | `RBACTest`, `RolePermissionEvaluatorTest`, `AuthenticationAuthorizationSecurityTest` | Test | ✅ Implemented |
| FR-AUTH-10 | BCrypt password hashing (minimum 10 rounds) | High | Ph-1 | §5.1.1 | `AuthServiceImpl` — `BCryptPasswordEncoder(10)` | `AuthServiceImplTest` | Inspection | ✅ Implemented |
| FR-AUTH-11 | OAuth2 client integration (Google, GitHub) | Medium | Ph-2 | §4.3.2 | Not yet implemented | — | Test | 🔵 Pending Ph-2 |
| FR-AUTH-12 | Optional TOTP-based 2FA: QR provisioning, TOTP login verification, 8 one-time recovery codes, TOTP-verified disable | High | Ph-2 | §3.2.1 | `TwoFactorController`, `TwoFactorServiceImpl`, `AuthServiceImpl.login()` (3-arg overload) | `TwoFactorServiceImplTest` | Test | ✅ Implemented |

### 6.2 Product Catalogue Management (FG-02)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-PROD-01 | Paginated product listing | High | Ph-1 | §4.7.3 | `ProductControllerV2.getAllProducts()`, `ProductServiceImpl.getAllProducts()` | `ProductControllerV2Test`, `ProductServiceImplTest`, `ProductApiTest` | Test | ✅ Implemented |
| FR-PROD-02 | Product detail by ID | High | Ph-1 | §4.7.3 | `ProductControllerV2.getProductById()`, `ProductServiceImpl.getProductById()` | `ProductControllerV2Test`, `ProductServiceImplTest`, `ProductApiTest` | Test | ✅ Implemented |
| FR-PROD-03 | Product categorisation and filter | Medium | Ph-1 | §4.5.1, §4.7.3 | `ProductControllerV2.getProductsByCategory()`, `CategoryServiceImpl` | `ProductControllerV2Test`, `CategoryManagementTest` | Test | ✅ Implemented |
| FR-PROD-04 | Admin product CRUD | High | Ph-1 | §4.7.3, §5.1.3 | `AdminProductController`, `AdminServiceImpl` | `AdminProductControllerTest` | Test | ✅ Implemented |
| FR-PROD-05 | Versioned APIs (v1 deprecated; v2 current) with sunset headers | Medium | Ph-1 | §4.7.2, §4.6.1 | `ProductControllerV1`, `ProductControllerV2`, `ApiSunsetInterceptor` | `ProductControllerV1Test`, `ApiSunsetInterceptorTest` | Inspection | ✅ Implemented |
| FR-PROD-06 | Redis product cache with 5-min TTL | Medium | Ph-1 | §4.5.4, §4.6.1 | `ProductServiceImpl` — `@Cacheable("products")`, `CacheConfig` | `ProductServiceImplTest`, `CacheMetricsUtilTest`, `ProductServiceRedisCacheRoundTripIntegrationTest` | Test | ✅ Implemented |
| FR-PROD-08 | Product variants (size, colour) with independent per-variant inventory; cart items pinned to a variant | High | Ph-1 | §4.7.3 | `ProductVariant`, `ProductVariantServiceImpl`, `AdminProductController` (nested `/variants` endpoints), `CartServiceImpl.addToCart(userId, productId, variantId, quantity)`; admin UI: `frontend/src/components/admin/ProductVariantsModal.tsx` | `ProductVariantServiceImplTest`, `AdminProductVariantControllerIntegrationTest`, `ProductVariantRepositoryTest`, `ProductVariantsModal.test.tsx` | Test | ✅ Implemented (#81 backend, #427 frontend UI) |
| FR-PROD-09 | Multi-image product gallery: upload, reorder, delete; primary image kept in sync with legacy `Product.imageUrl` | Medium | Ph-1 | §4.7.3 | `ProductImage`, `ProductImageServiceImpl`, `AdminProductController` (nested `/images` endpoints, upload endpoint's semantics changed from #82) | `ProductImageServiceImplTest`, `AdminProductImageControllerIntegrationTest`, `AdminProductControllerIntegrationTest` (upload) | Test | ✅ Implemented (#82) |
| FR-PROD-07 | Redis category cache with 1-hour TTL | Low | Ph-1 | §4.5.4 | `CategoryServiceImpl` — `@Cacheable("categories")`, `CacheConfig` | `CategoryServiceImplTest` | Test | ✅ Implemented |

### 6.3 Shopping Cart Operations (FG-03)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-CART-01 | Add item to cart (authenticated) | High | Ph-1 | §4.7.3 | `CartController.addToCart()`, `CartServiceImpl.addToCart()` | `CartControllerTest`, `CartServiceImplTest`, `CartApiTest` | Test | ✅ Implemented |
| FR-CART-02 | Retrieve cart contents | High | Ph-1 | §4.7.3 | `CartController.getCart()`, `CartServiceImpl.getCartByUserId()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-03 | Remove individual cart item | High | Ph-1 | §4.7.3 | `CartController.removeCartItem()`, `CartServiceImpl.removeItem()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-04 | Clear entire cart | Medium | Ph-1 | §4.7.3 | `CartController.clearCart()`, `CartServiceImpl.clearCart()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-05 | Calculate cart total | High | Ph-1 | §4.7.3 | `CartController.getCartTotal()`, `CartServiceImpl.calculateTotal()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-06 | One cart per user (1:1 relationship) | High | Ph-1 | §4.5.1 | `Cart` entity — `@OneToOne User`, `CartRepository.findByUser()` | `CartRepositoryTest`, `CartTest` | Inspection | ✅ Implemented |

### 6.4 Checkout and Order Processing (FG-04)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-CHK-01 | Validate cart before checkout | High | Ph-1 | §4.7.3 | `CheckoutController.validateCart()`, `CheckoutServiceImpl.validateCart()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-02 | Calculate checkout total | High | Ph-1 | §4.7.3 | `CheckoutController.calculateTotal()`, `CheckoutServiceImpl.calculateCartTotal()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-03 | Checkout without payment (cash / COD flow) | High | Ph-1 | §4.8.2 | `CheckoutController.processCheckout()`, `CheckoutServiceImpl.processCheckout()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-04 | Checkout with Razorpay payment | High | Ph-2 | §4.8.2 | `CheckoutController.processCheckoutWithPayment()`, `PaymentServiceImpl` | `CheckoutControllerTest`, `PaymentProcessingTest` | Test | 🔵 Pending Ph-2 |
| FR-CHK-05 | Create Order + OrderItems on checkout | High | Ph-1 | §4.5.3, §4.8.2 | `CheckoutServiceImpl.createOrderFromCart()`, `OrderRepository.save()` | `CheckoutServiceImplTest`, `OrderServiceImplTest`, `OrderServiceIntegrationTest` | Test | ✅ Implemented |
| FR-CHK-06 | Deduct inventory on order placement | High | Ph-1 | §4.8.2, §4.9.3 | `CheckoutServiceImpl.deductInventoryFromCart()`, `InventoryServiceImpl.deductStock()` | `CheckoutServiceImplTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-07 | User views order history | Medium | Ph-1 | §4.7.3 | `UserOrderController.getUserOrders()`, `OrderServiceImpl.getUserOrders()` | `UserOrderControllerTest`, `OrderServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-08 | Admin views and manages all orders | Medium | Ph-1 | §4.7.3, §5.1.3 | `AdminOrderController`, `AdminServiceImpl.getAllOrders()` | `AdminOrderControllerTest` | Test | ✅ Implemented |
| FR-CHK-09 | Apply coupon/discount code during checkout | Medium | Ph-1 | §4.7.3 | `MultiStepCheckoutController.applyCoupon()`, `CheckoutServiceImpl.applyCoupon()` (#77); frontend consumption via `ShippingStep.tsx`, `CheckoutPage.tsx` (#436) | `ShippingStep.test.tsx`, `PaymentStep.test.tsx` | Test | ✅ Implemented (#77 backend, #436 frontend) |
| FR-CHK-10 | Order return and refund request flow (RET-01/02/03): user requests a return within 30 days of delivery; admin approves/rejects; approval triggers refund + inventory restoration | Medium | Ph-2 | §4.7.3 | `UserOrderController.createReturnRequest()`, `AdminReturnController`, `ReturnServiceImpl` (#88) | `ReturnServiceImplTest`, `ReturnRequestIT` | Test | ✅ Implemented (#88) |

### 6.5 Payment Processing (FG-05)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-PAY-01 | Razorpay payment order creation | High | Ph-2 | §4.4.2, §4.8.2 | `PaymentServiceImpl.initiatePayment()`, `RazorpayClientAdapter.createOrder()` | `PaymentServiceImplTest`, `RazorpayClientAdapterTest` | Test | 🟡 Partial (code present; full E2E deferred to Ph-2) |
| FR-PAY-02 | Razorpay signature verification | High | Ph-2 | §4.8.2 | `PaymentServiceImpl.verifyPaymentSignature()`, `PaymentSignatureValidationService` | `PaymentSignatureValidationServiceTest`, `PaymentServiceImplTest` | Test | 🟡 Partial (logic implemented; Razorpay live test deferred) |
| FR-PAY-03 | Payment transaction recording with status tracking | High | Ph-2 | §4.5.1, §4.9.2 | `Payment` entity, `PaymentRepository`, `PaymentServiceImpl` | `PaymentEntityTest`, `PaymentRepositoryTest`, `PaymentServiceImplTest` | Test | 🟡 Partial (entity and repo ready; end-to-end flow Ph-2) |
| FR-PAY-04 | Razorpay webhook event handling | Medium | Ph-2 | §4.6.2 | `WebhookServiceImpl.processWebhookEvent()` | `WebhookServiceImplTest`, `WebhookAdminControllerTest` | Test | 🔵 Pending Ph-2 |
| FR-PAY-05 | Razorpay credentials externalised via env vars | High | Ph-2 | §8 Appendix A | `application.properties` — `${RAZORPAY_KEY_ID}`, `${RAZORPAY_KEY_SECRET}` (no default on secrets, #114) | `RazorpayClientAdapterTest` (env check) | Inspection | ✅ Implemented |

### 6.6 Inventory Management (FG-06)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-INV-01 | User checks product inventory status | High | Ph-1 | §4.7.3 | `InventoryStatusController.getInventoryByProduct()`, `InventoryServiceImpl`, `ProductDetailPage.tsx`, `useInventoryStatus.ts`, `api/inventory.ts` | `InventoryStatusControllerTest`, `InventoryServiceImplTest`, `InventoryStatusPublicAccessIntegrationTest`, `inventory.test.ts` | Test | ✅ Implemented |
| FR-INV-02 | User checks product availability | High | Ph-1 | §4.7.3 | `InventoryStatusController.checkAvailability()`, `InventoryServiceImpl.isAvailable()` | `InventoryStatusControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-03 | Admin adds stock | High | Ph-1 | §4.7.3 | `AdminInventoryController.addStock()`, `InventoryServiceImpl.addStock()` | `AdminInventoryControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-04 | Admin updates stock quantities | High | Ph-1 | §4.7.3 | `AdminInventoryController.updateStock()`, `InventoryServiceImpl.updateStock()` | `AdminInventoryControllerTest`, `InventoryServiceImplTest`, `ProductInventorySingleSourceOfTruthIT` | Test | ✅ Implemented (#485) |
| FR-INV-05 | Inventory status tracking (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`, `DISCONTINUED`) | Medium | Ph-1 | §4.9.3 | `Inventory` entity — `InventoryStatus` enum, `InventoryServiceImpl.updateStatus()` | `InventoryTest`, `InventoryServiceImplTest`, `InventoryManagementTest` | Test | ✅ Implemented |
| FR-INV-06 | Emit `InventoryThresholdBreachEvent` on low stock | Medium | Ph-2 | §4.6.2 | `InventoryServiceImpl`, `DomainEventPublisher.publishEvent(InventoryThresholdBreachEvent)` | `InventoryMonitoringServiceTest`, `InventoryThresholdManagementServiceTest` | Test | 🔵 Pending Ph-2 |
| FR-INV-07 | Admin inventory analytics and reports | Medium | Ph-2 | §4.7.3 | `AdminInventoryAnalyticsController`, `InventoryAnalyticsService`, `AdminInventoryReportController`, `InventoryReportService`, `frontend/src/components/admin/InventoryThresholdsTab.tsx` | `AdminInventoryAnalyticsControllerTest`, `InventoryAnalyticsServiceTest`, `InventoryReportServiceTest`, `InventoryThresholdsTab.test.tsx` | Test | ✅ Implemented (#433) |

### 6.7 Reviews and Wishlists (FG-07)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-REV-01 | Submit product review with star rating | Medium | Ph-1 | §4.7.3 | `ProductReviewController.createReview()`, `ProductReviewServiceImpl.createReview()`, `frontend/src/components/product/WriteReviewForm.tsx` | `ProductReviewControllerTest`, `ProductReviewServiceImplTest`, `WriteReviewForm.test.tsx` | Test | ✅ Implemented (#441 — was backend-only; frontend write UI added) |
| FR-REV-02 | View product reviews (public) | Medium | Ph-1 | §4.7.3 | `ProductReviewController.getProductReviews()`, `ProductReviewServiceImpl.getReviewsByProduct()` | `ProductReviewControllerTest` | Test | ✅ Implemented |
| FR-REV-03 | User updates / deletes own reviews | Medium | Ph-1 | §4.7.3 | `ProductReviewController.updateReview()` / `deleteReview()`, `ProductReviewServiceImpl` | `ProductReviewControllerTest`, `ProductReviewServiceImplTest` | Test | ✅ Implemented |
| FR-WISH-01 | Add products to wishlist (authenticated) | Low | Ph-1 | §4.7.3 | `WishlistController.addToWishlist()`, `WishlistServiceImpl.addToWishlist()` | `WishlistControllerTest`, `WishlistServiceImplTest` | Test | ✅ Implemented |
| FR-WISH-02 | View and manage wishlist | Low | Ph-1 | §4.7.3 | `WishlistController.getWishlist()` / `removeFromWishlist()`, `WishlistServiceImpl` | `WishlistControllerTest`, `WishlistServiceImplTest` | Test | ✅ Implemented |

### 6.8 Admin Operations (FG-08)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-ADM-01 | Sales analytics dashboard for admins | Medium | Ph-2 | §4.7.3 | `SalesAnalyticsController`, `SalesAnalyticsServiceImpl`, `frontend/src/components/admin/SalesAnalyticsTab.tsx` | `SalesAnalyticsControllerTest`, `SalesAnalyticsServiceImplTest`, `AnalyticsReportingTest`, `SalesAnalyticsTab.test.tsx` | Test | ✅ Implemented (#431) |
| FR-ADM-02 | Inventory analytics and reports | Medium | Ph-2 | §4.7.3 | `AdminInventoryAnalyticsController`, `InventoryAnalyticsService`, `frontend/src/components/admin/InventoryAnalyticsTab.tsx` | `AdminInventoryAnalyticsControllerTest`, `InventoryAnalyticsServiceTest`, `InventoryAnalyticsTab.test.tsx` | Test | ✅ Implemented (#432) |
| FR-ADM-03 | Admin manages user accounts (view, update, deactivate) | Medium | Ph-2 | §4.7.3 | `AdminUserController`, `AdminServiceImpl`, `frontend/src/components/admin/UsersTab.tsx`, `UserDetailModal.tsx` | `AdminUserControllerTest`, `UserDetailModal.test.tsx`, `UsersTab.test.tsx` | Test | ✅ Implemented (#439) |
| FR-ADM-04 | Tamper-evident audit log of all admin actions | High | Ph-1 | §4.3.5, §4.6.1 | `AuditAspect` (`@Around @Auditable`), `AuditLogService`, `AuditLogController` | `AuditAspectTest`, `AuditLogServiceTest`, `AuditLogControllerTest` | Test | ✅ Implemented |
| FR-ADM-05 | Admin reporting endpoints | Medium | Ph-2 | §4.7.3 | `AdminReportController` | `AdminReportControllerTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-06 | Admin configures inventory alert thresholds | Medium | Ph-2 | §4.7.3 | `AdminInventoryThresholdController`, `InventoryThresholdManagementService`, `frontend/src/components/admin/InventoryThresholdsTab.tsx` (product-level threshold set) | `AdminInventoryThresholdControllerTest`, `InventoryThresholdManagementServiceTest`, `InventoryThresholdsTab.test.tsx` | Test | 🟡 Partial (#433 added product-level threshold UI; category-level threshold + inheritance toggle UI still deferred, tracked as a follow-up) |
| FR-ADM-07 | Admin manages webhook subscriptions | Low | Ph-2 | §4.7.3 | `WebhookAdminController`, `WebhookServiceImpl`; frontend consumption via `WebhookSubscriptionsTab.tsx`, `WebhookSubscriptionFormModal.tsx` | `WebhookAdminControllerTest`, `WebhookServiceImplTest`, `WebhookSubscriptionsTab.test.tsx` | Test | ✅ Implemented (#446 frontend UI; backend was already complete) |
| FR-ADM-08 | All `/api/admin/**` requires `ADMIN` role | High | Ph-1 | §5.1.3 | `SecurityConfig` — `.requestMatchers("/api/admin/**").hasRole("ADMIN")` | `AuthenticationAuthorizationSecurityTest`, `RBACTest` | Test | ✅ Implemented |
| FR-ADM-09 | Admin CRUD for product categories with hierarchical parent/child support; deletion blocked while products or subcategories still reference the category | Medium | Ph-1 | §4.7.3 | `Category` (`parentCategory`/`subcategories`), `CategoryServiceImpl`, `AdminCategoryController`; frontend consumption via `CategoriesTab`, `CategoryFormModal` | `CategoryServiceImplTest`, `CategoryTest`, `AdminCategoryControllerIntegrationTest`, `CategoriesTab.test.tsx` | Test | ✅ Implemented (#68 backend, #428 frontend UI) |
| FR-ADM-10 | Admin CRUD for product tags (create, view, update, delete) | Medium | Ph-1 | §A.19 | `ProductTag`, `ProductTagServiceImpl`, `AdminProductTagController`; frontend consumption via `TagsTab`, `TagFormModal` | `TagsTab.test.tsx` | Test | ✅ Implemented (#429) |
| FR-ADM-11 | Admin CRUD for coupons (list, create, deactivate) | Medium | Ph-1 | §A.20 | `Coupon`, `CouponServiceImpl`, `AdminCouponController`; frontend consumption via `CouponsTab`, `CouponFormModal` | `AdminCouponControllerTest`, `CouponServiceImplTest`, `CouponsTab.test.tsx` | Test | ✅ Implemented (#435) |

### 6.9 Monitoring and Observability (FG-09)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-MON-01 | Health endpoint at `/actuator/health` | High | Ph-1 | §4.7.3, §4.10.1 | Spring Boot Actuator AutoConfig | `HealthIndicatorTest` | Test | ✅ Implemented |
| FR-MON-02 | MySQL health indicator | High | Ph-2 | §4.10.1 | `DatabaseHealthIndicator` | `DatabaseHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-03 | Redis health indicator | High | Ph-2 | §4.10.1 | `RedisHealthIndicator` | `RedisHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-04 | Circuit breaker state indicators | Medium | Ph-2 | §4.9.4, §4.10 | Resilience4j Actuator integration | `ReliabilityTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-05 | Prometheus metrics at `/actuator/prometheus` | Medium | Ph-2 | §4.10.1 | Micrometer / Prometheus Actuator endpoint | `PerformanceMetricsControllerTest`, `PoolMetricsControllerTest` | Test | 🟡 Partial (endpoint present; Prometheus rules deferred to Ph-2) |
| FR-MON-06 | K8s liveness and readiness probes | High | Ph-2 | §4.10.1 | `kubernetes/buildnest-deployment.yaml` | Environment validation | Test | 🔵 Pending Ph-2 |
| FR-MON-07 | Elasticsearch event indexing and alerting | Low | Ph-2 | §4.1.2 | `ElasticsearchIngestionService`, `ElasticsearchAlertingService` | `ElasticsearchIngestionServiceTest`, `ElasticsearchAlertingServiceTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-08 | 13 Prometheus alert rules | Medium | Ph-2 | §4.10, Appendix B | `kubernetes/prometheus-rules.yaml` | Configuration audit | Inspection | 🔵 Pending Ph-2 |

### 6.10 Frontend Application (FG-10)

> Per-requirement audit completed 2026-07-17 (#453), superseding the prior "stub, no
> implementation exists" claim: `frontend/src/` has 71 real `.ts`/`.tsx` source files with
> working pages/components for every FR-FE-* row below. Each row was individually checked
> against the real source (not just path existence) — see the Status column for whether the
> requirement's actual text (not just the file's presence) is satisfied, partially satisfied,
> or genuinely still missing.

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-FE-01 | React 19 SPA | High | Ph-2 | §4.2.2 | `frontend/src/App.tsx`, `frontend/src/main.tsx` (React 19.2.6) | Vitest | Inspection | ✅ Implemented |
| FR-FE-02 | Axios / Fetch with JWT injection interceptors | High | Ph-2 | §4.3.6 | `frontend/src/api/client.ts` (hand-rolled `fetch` wrapper, not Axios — CSRF header injection + 401 handling) | `client.test.ts` | Inspection | ✅ Implemented |
| FR-FE-03 | React Router v6+ client-side routing | High | Ph-2 | §4.7.4 | `frontend/src/App.tsx` (react-router-dom v7, inline `<Routes>`/`<Route>`, not a separate router file) | Vitest, Playwright | Test | ✅ Implemented |
| FR-FE-04 | Responsive design (mobile / tablet / desktop) | High | Ph-2 | §4.10.5 | CSS / Tailwind v4, `sm:`/`md:`/`lg:` breakpoint classes throughout (e.g. `pages/HomePage.tsx`) | Playwright viewport tests | Demonstration | ✅ Implemented |
| FR-FE-05 | React Context / Redux global state | High | Ph-2 | §4.3.6 | `frontend/src/contexts/AuthContext.tsx` (cart state uses `hooks/useCart.ts`, a hook, not a second Context — no `CartContext`) | Vitest | Inspection | ✅ Implemented |
| FR-FE-06 | Protected routes redirect unauthenticated users | High | Ph-2 | §4.7.4 | `frontend/src/components/common/RequireAuth.tsx` (also supports role-gating, e.g. `role="ADMIN"` — more capable than stated) | `RequireAuth.test.tsx`, Playwright | Test | ✅ Implemented |
| FR-FE-07 | Loading indicators during async API calls | Medium | Ph-2 | §4.3.6 | Inline Tailwind spinners (e.g. `RequireAuth.tsx`) + `frontend/src/components/product/LoadingSkeleton.tsx` (no single dedicated `Spinner` component) | Playwright | Demonstration | ✅ Implemented |
| FR-FE-08 | Toast notifications for success/error | Medium | Ph-2 | §4.3.6 | None found — no toast component or library anywhere in `frontend/src/` or `package.json` | Vitest | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-09 | Client-side form validation (React Hook Form + Yup) | Medium | Ph-2 | §4.3.6 | Manual inline `validate()` functions per form (e.g. `pages/RegisterPage.tsx`) — neither `react-hook-form` nor `yup` is a dependency | Vitest | Test | 🟡 Partial (validation exists but not via the specified libraries — manual state/error handling per form instead) |
| FR-FE-10 | Silent JWT refresh on 401 response | High | Ph-2 | §4.3.6 | `frontend/src/api/client.ts` (401 triggers one silent refresh via a registered handler, guarded against recursing into itself via `skipAuthInterceptor` on the refresh/logout calls themselves — #516) + `contexts/AuthContext.tsx` (registers it) | Vitest (`client.test.ts` incl. #516 recursion-guard regression tests), Playwright | Test | ✅ Implemented |
| FR-FE-11 | Home page | High | Ph-2 | §4.7.4 | `frontend/src/pages/HomePage.tsx` | Playwright | Demonstration | ✅ Implemented |
| FR-FE-12 | Product listing with pagination / sort / filter | High | Ph-2 | §4.7.4 | `frontend/src/pages/ProductListingPage.tsx` (uses `components/filters/SortDropdown.tsx`, `components/common/Pagination.tsx`) | Vitest, Playwright | Test | ✅ Implemented |
| FR-FE-13 | Product detail page | High | Ph-2 | §4.7.4 | `frontend/src/pages/ProductDetailPage.tsx` | Playwright | Demonstration | ✅ Implemented |
| FR-FE-14 | Shopping cart page | High | Ph-2 | §4.7.4 | `frontend/src/pages/CartPage.tsx` | Vitest, Playwright | Test | ✅ Implemented |
| FR-FE-15 | Checkout page with Razorpay modal | High | Ph-2 | §4.7.4 | `frontend/src/pages/CheckoutPage.tsx`, `components/checkout/PaymentStep.tsx` — backend Razorpay order is created and its ID displayed, but no client-side Razorpay `checkout.js` modal invocation (`new Razorpay(...)`) exists; the page currently only says "you will be redirected" | Playwright | Test | 🟡 Partial (checkout flow + backend order creation implemented; the actual Razorpay JS modal is not yet wired client-side) |
| FR-FE-16 | Login page | High | Ph-2 | §4.7.4 | `frontend/src/pages/LoginPage.tsx` | Vitest, Playwright | Test | ✅ Implemented |
| FR-FE-17 | Registration page with password strength indicator | High | Ph-2 | §4.7.4 | `frontend/src/pages/RegisterPage.tsx` — registration works with a minimum-length check, but no visual strength meter/indicator | Vitest, Playwright | Test | 🟡 Partial (registration implemented; the password-strength indicator specifically is missing) |
| FR-FE-18 | User profile page | Medium | Ph-2 | §4.7.4 | `frontend/src/components/account/ProfileTab.tsx` — a tab inside `pages/AccountPage.tsx`, not a standalone page/route | Vitest | Test | 🟡 Partial (functionality implemented; delivered as an account-page tab rather than its own route) |
| FR-FE-19 | Order history page | Medium | Ph-2 | §4.7.4 | `frontend/src/components/account/OrdersTab.tsx` — a tab inside `pages/AccountPage.tsx`, not a standalone page/route | Vitest | Test | 🟡 Partial (functionality implemented; delivered as an account-page tab rather than its own route) |
| FR-FE-20 | Wishlist page | Low | Ph-2 | §4.7.4 | `frontend/src/components/account/WishlistTab.tsx` (Clear All), `frontend/src/components/product/WishlistButton.tsx` (add/remove/contains toggle), `frontend/src/components/common/Navbar.tsx` (count badge) — a tab inside `pages/AccountPage.tsx`, not a standalone page/route | Vitest | Test | 🟡 Partial (functionality implemented; delivered as an account-page tab rather than its own route) |
| FR-FE-21 | Search results page | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/ProductListingPage.tsx` — search is integrated via a `?search=` query param on the product listing route, not a separate page/route | Vitest | Test | 🟡 Partial (functionality implemented; delivered as a mode of the listing page rather than a dedicated route) |
| FR-FE-22 | Admin dashboard | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/AdminDashboardPage.tsx` | Playwright | Demonstration | ✅ Implemented |
| FR-FE-23 | Admin product management | Medium | Ph-2 | §4.7.4 | `frontend/src/components/admin/ProductsTab.tsx`, `ProductFormModal.tsx`, `ProductImagesModal.tsx`, `ProductVariantsModal.tsx` | `ProductsTab.test.tsx`, `ProductImagesModal.test.tsx`, `ProductVariantsModal.test.tsx` | Test | ✅ Implemented (#425, #426, #427) |
| FR-FE-24 | Admin inventory page | Medium | Ph-2 | §4.7.4 | `frontend/src/components/admin/InventoryTab.tsx` | Vitest | Test | ✅ Implemented |
| FR-FE-25 | Admin order management | Medium | Ph-2 | §4.7.4 | `frontend/src/components/admin/OrdersTab.tsx`, `RefundModal.tsx` | `OrdersTab.test.tsx`, `RefundModal.test.tsx` | Test | ✅ Implemented |
| FR-FE-26 | Navbar on all pages | High | Ph-2 | §4.3.6 | `frontend/src/components/common/Navbar.tsx` | Playwright | Demonstration | ✅ Implemented |
| FR-FE-27 | Footer | Low | Ph-2 | §4.3.6 | None found — no Footer component exists anywhere in `frontend/src/` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-28 | ProductCard component | High | Ph-2 | §4.3.6 | `frontend/src/components/product/ProductCard.tsx` | `ProductCard.test.tsx` | Demonstration | ✅ Implemented |
| FR-FE-29 | Breadcrumb navigation | Low | Ph-2 | §4.3.6 | None found — no Breadcrumb component exists anywhere in `frontend/src/` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-30 | ErrorBoundary with fallback UI | Medium | Ph-2 | §4.3.6 | `frontend/src/components/common/ErrorBoundary.tsx` (class component, `getDerivedStateFromError`/`componentDidCatch`, renders a real fallback UI with reload button) | `ErrorBoundary.test.tsx` | Test | ✅ Implemented |
| FR-FE-31 | Admin category management | Medium | Ph-2 | §4.7.4 | `frontend/src/components/admin/CategoriesTab.tsx`, `frontend/src/components/admin/CategoryFormModal.tsx` | Vitest | Test | ✅ Implemented (#428) |

### 6.11 Seller & Marketplace Management (FG-11) *(Ph-3, Planned)*

> No implementation exists for any row below — added as part of the marketplace-pivot addendum (SRS v5.0, SDD v4.0). See SDD §4.5.2's note on the existing dormant `product.supplier_id` FK as the likely reactivation target for FR-SEL-03, once the `Seller`/`District` design is finalised.

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-SEL-01 | Seller registration, distinct from a buyer (USER) account | High | Ph-3 | §4.3.3, §4.5.1, §4.5.2 | `Seller` entity/changeset, `SellerServiceImpl.registerSeller`, `SellerController` (#553). `district_id` deliberately deferred/nullable pending ADR #561 (OQ-01/OQ-02) | `SellerServiceImplTest`, `SellerControllerTest`, `SellerRegistrationIT` | Test | ✅ Implemented |
| FR-SEL-02 | Admin verification/approval before a seller can list products | High | Ph-3 | §4.5.2 | `AdminSellerController` (GET/PATCH verification-status), `SellerServiceImpl.updateVerificationStatus`, `SellerRepository.findByVerificationStatus` (#554) | `SellerServiceImplTest`, `AdminSellerControllerTest` | Test | ✅ Implemented |
| FR-SEL-03 | Each product associated with exactly one owning seller | High | Ph-3 | §4.5.2 (dormant `product.supplier_id` FK) | `Product.seller` (`@ManyToOne` to `User` via reactivated `supplier_id`/`fk_product_supplier`), `SellerProductController`, `ProductServiceImpl.createProductForSeller`/`buildAndSaveProduct` (#555) | `ProductServiceImplTest`, `ProductRepositoryTest` (H2 FK round-trip) | Test | ✅ Implemented |
| FR-SEL-04 | Seller manages their own product listings only | High | Ph-3 | §4.5.1, §4.5.2 | `SellerProductController` (`/api/user/seller/products`, `ROLE_SELLER`), `ProductServiceImpl.updateProductForSeller`/`deleteProductForSeller`/`getProductsForSeller` (ownership scoped via `ProductRepository.findByIdAndSeller_Id`) (#555) | `ProductServiceImplTest`, `ProductRepositoryTest` | Test | ✅ Implemented |
| FR-SEL-05 | Seller manages inventory for their own products only | High | Ph-3 | §4.5.1, §4.5.2 | `SellerInventoryController` (`/api/user/seller/inventory`, `ROLE_SELLER`), `InventoryServiceImpl.getInventoryForSeller`/`adjustStockForSeller` (ownership scoped via `InventoryRepository.findByProduct_IdAndProduct_Seller_Id`) (#556) | `InventoryServiceImplTest`, `SellerInventoryControllerTest` | Test | ✅ Implemented |
| FR-SEL-06 | Seller views/manages orders containing their own products only | High | Ph-3 | §4.5.1 | `OrderGroup` entity + `Order.orderGroup` schema (#578), checkout-time order splitting in `CheckoutServiceImpl` (#579), `SellerOrderController`/`OrderServiceImpl` seller-scoped list/detail/status endpoints via `OrderRepository.findBySellerId`/`findByIdAndSellerId` (#580, sub-issues of #557); frontend `components/seller/OrdersTab.tsx` + `SellerDashboardPage`/`/seller` route + `account/OrdersTab.tsx`'s order-group grouping, both consuming `OrderResponseDTO.orderGroupId` (#581) | `OrderRepositoryTest` (H2 FK round-trip; seller-scoping `EXISTS`-subquery); `CheckoutServiceImplTest`/`OrderServiceImplTest` (seller-split + `orderGroupId` mapping unit tests); `SellerOrderControllerTest` (seller ownership/transition/status unit tests); `seller/OrdersTab.test.tsx`/`account/OrdersTab.test.tsx` (frontend RTL tests) | Test | ✅ Implemented |
| FR-SEL-07 | Buyers rate/review individual sellers | Medium | Ph-3 | §4.5.1 | `SellerReview` entity (`seller_review` table, seller_id/user_id both FK to `users` — mirrors `Product.seller`'s User-based convention), `SellerReviewController` (`/api/sellers/{sellerId}/reviews`, GET public), `SellerReviewServiceImpl` (create/update/delete with ownership check, average rating, distribution) (#558); frontend `SellerReviewPanel.tsx` surfaced from a delivered order's detail view, consuming `OrderResponseDTO.sellerId` (new field, #558) | `SellerReviewServiceImplTest` (20 unit tests); `SellerReviewPanel.test.tsx` (4 RTL tests); `CivilEcommerceApplicationTests` (H2 schema-validation round-trip for the new entity/changeset) | Test | ✅ Implemented |
| FR-SEL-08 | Seller cannot access/modify another seller's data (defense in depth) | High | Ph-3 | Security Overlay (pattern per `spring/spring-security.md`) | Audited existing FR-SEL-04/05/06 implementations (`SellerProductController`/`SellerInventoryController`/`SellerOrderController`, class-level `@PreAuthorize("hasRole('SELLER')")` + service-layer ownership-scoped repository queries `findByIdAndSeller_Id`/`findByIdAndSellerId`/`findByProduct_IdAndProduct_Seller_Id` throwing `AccessDeniedException`/`ResourceNotFoundException` on mismatch) — confirmed already correctly isolated and unit-tested; the real gap closed was that no test previously exercised `@PreAuthorize` through the real Spring Security filter chain (#559) | `SellerDataIsolationIntegrationTest` (`@SpringBootTest`+MockMvc, real filter chain: non-SELLER rejected, owner allowed, cross-seller access rejected end-to-end for products/inventory/orders) | Test | ✅ Implemented |

### 6.12 Location-Based Matching (FG-12) *(Ph-3, all sub-requirements implemented)*

> FR-LOC-01/02 (district reference-data model) are implemented (#562). FR-LOC-03 (catalogue/search district filtering) is implemented (#563). FR-LOC-04 (checkout-time enforcement) is implemented (#564). OQ-01/OQ-02 are resolved via [ADR 0001](../design/adr/0001-district-matching-strategy-for-location-based-seller-buyer-matching.md) (#561) — radius/seller-declared matching, fixed admin-maintained reference table — see SDD §4.5.6 for the finalized design.

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-LOC-01 | Record a district (or equivalent) for each seller | High | Ph-3 | §4.5.1, §4.5.2 | `District`, `SellerDistrict` entities, `SellerServiceImpl.updateSellerDistricts`, `PUT /api/user/seller/districts` (#562) | `DistrictRepositoryTest`, `DistrictServiceImplTest` | Test | ✅ Implemented |
| FR-LOC-02 | Record a district (or equivalent) for each buyer | High | Ph-3 | §4.5.1, §4.5.2 | `User.district` (nullable FK), `DistrictServiceImpl.deriveBuyerDistrict` called from `AddressServiceImpl` on address create/update/set-default (#562) | `DistrictServiceImplTest` | Test | ✅ Implemented |
| FR-LOC-03 | Restrict buyer's catalogue view to district-matching sellers | High | Ph-3 | §4.5.6 | `ProductDocument.districtIds` (populated from the owning seller's `SellerDistrict` links via `SellerDistrictRepository.findAllBySeller_User_Id`), `ProductElasticsearchRepository` districtId term-filter/derived-query variants, `ProductSearchServiceImpl.search`/`doSearch` `districtId` param, `GET /api/v2/products/search?districtId=` (Elasticsearch path only — JPA fallback out of scope, per issue text) (#563) | `ProductSearchServiceTest`, `ProductControllerV2Test` | Test | ✅ Implemented |
| FR-LOC-04 | Prevent checkout from a seller outside the buyer's matching radius | High | Ph-3 | §4.5.6 | `CheckoutServiceImpl.validateCheckout`/`allItemsWithinBuyerDistrict` — per cart item, a seller with declared `SellerDistrict` rows requires the buyer's `User.district` to be among them; fail-closed when the buyer's district can't be determined; sellers with no declared districts are unrestricted (#564). `validateCheckout` gained `@Transactional(readOnly = true)` — the new check lazily loads `Product.seller`, which the pre-existing controller-direct call path had no transaction to support | `CheckoutServiceImplTest` (6 new unit tests), `CheckoutValidateNoAmbientTransactionIT` (real `@SpringBootTest`, no ambient transaction — proves the lazy-load fix against the actual controller call shape) | Test | ✅ Implemented |

---

## 7. Non-Functional Requirements

### 7.1 Usability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UR-01 | Consistent JSON response structure with standard HTTP status codes | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler`, all controller response types | `GlobalExceptionHandlerTest`, all controller tests | Test | ✅ Implemented |
| UR-02 | Error responses with machine-readable code and human-readable message | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler.ErrorResponse` | `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | Test | ✅ Implemented |
| UR-03 | Auto-generated OpenAPI / Swagger documentation | Medium | Ph-1 | §4.7.1 | SpringDoc OpenAPI dependency, controller annotations | Manual verification | Inspection | ✅ Implemented |
| UR-04 | API v1 / v2 backward compatibility | Medium | Ph-1 | §4.7.2 | `ProductControllerV1`, `ProductControllerV2` | `ProductControllerV1Test`, `ProductControllerV2Test` | Test | ✅ Implemented |
| UR-05 | Deprecated v1 endpoints include `Sunset` + `Deprecation` headers | Low | Ph-1 | §4.7.2 | `ApiSunsetInterceptor` | `ApiSunsetInterceptorTest` | Inspection | ✅ Implemented |
| UR-FE-01 | WCAG 2.1 AA accessibility compliance | High | Ph-2 | §4.3.6 | Frontend components | axe-core | Inspection | 🔵 Pending Ph-2 |
| UR-FE-02 | LCP < 2.5 seconds | High | Ph-2 | §4.10.5 | Frontend build optimisation, Nginx caching | Lighthouse | Analysis | 🔵 Pending Ph-2 |
| UR-FE-03 | Visual feedback on interactive elements | Medium | Ph-2 | §4.3.6 | CSS focus/hover states | Playwright | Demonstration | 🔵 Pending Ph-2 |

### 7.2 Performance Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| PR-01 | API P95 response ≤ 500 ms under normal load | High | Ph-2 | §4.10 | Full stack + HikariCP + Redis caching | `PerformanceTest`, `LoadTestSimulation` (Gatling) | Analysis | 🔵 Pending Ph-2 |
| PR-02 | Sustain 1,000 concurrent users | High | Ph-2 | §4.10, §4.6.1 | Kubernetes HPA, stateless JWT | `StressTest`, `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-03 | Throughput > 10,000 req/min | High | Ph-2 | §4.10 | Full stack | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-04 | Error rate < 0.1% under load | High | Ph-2 | §4.10, §5.3 | Resilience4j, circuit breaker | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-05 | HikariCP max pool 20 (dev) / 30 (prod); min idle 10 (dev) / 15 (prod) | Medium | Ph-1 | §4.10.3 | `application.properties` HikariCP config | `PoolMetricsControllerTest`, `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| PR-06 | Connection timeout ≤ 30 seconds | Medium | Ph-1 | §4.10.3 | `spring.datasource.hikari.connection-timeout=30000` | `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| PR-07 | Product cache TTL = 5 min (300,000 ms, configurable) | Medium | Ph-1 | §4.5.4 | `CacheConfig` — `products` region TTL | `CacheMetricsUtilTest` | Test | ✅ Implemented |
| PR-08 | Docker image build time ≤ 2 min with layer caching | Low | Ph-2 | §4.10.5 | Multi-stage `Dockerfile` | CI build timing | Analysis | 🔵 Pending Ph-2 |

### 7.3 Reliability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| REL-01 | 99.9% production uptime | High | Ph-2 | §4.10.1 | Kubernetes 3-replica Deployment, HPA | SRE monitoring | Analysis | 🔵 Pending Ph-2 |
| REL-02 | Redis circuit breaker — 70% failure threshold | High | Ph-1 | §4.9.4, §5.3.1 | `ResilienceConfig` — `redis-circuit-breaker` (70%, 3 s timeout) | `ReliabilityTest` | Inspection | ✅ Implemented |
| REL-03 | Database circuit breaker — 50% failure threshold | High | Ph-1 | §4.9.4, §5.3.1 | `ResilienceConfig` — `database-circuit-breaker` (50%, 8 s timeout) | `ReliabilityTest` | Inspection | ✅ Implemented |
| REL-04 | RTO ≤ 15 minutes | High | Ph-2 | §4.10.1 | Kubernetes restart policies, graceful shutdown | DR drill | Test | 🔵 Pending Ph-2 |
| REL-05 | RPO ≤ 5 minutes | High | Ph-2 | §4.10.1 | `backend/scripts/backup-db.sh` (#121, OPS-03) | DR drill | Analysis | 🟢 Backup/restore tooling implemented and DR-drill-verified (30s restore); daily-only cadence gives RPO ≤24h today, not yet ≤5min — see follow-up issue for tightening backup frequency |

### 7.4 Availability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| AVL-01 | Kubernetes liveness + readiness probes | High | Ph-2 | §4.10.1 | `kubernetes/buildnest-deployment.yaml` | Environment validation | Inspection | 🔵 Pending Ph-2 |
| AVL-02 | Health checks covering MySQL, Redis, circuit breakers | High | Ph-2 | §4.7.3 | Composite `HealthIndicator` (`DatabaseHealthIndicator`, `RedisHealthIndicator`) | `DatabaseHealthIndicatorTest`, `RedisHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| AVL-03 | Graceful shutdown with 30-second drain | High | Ph-2 | §4.10.2 | `server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase=30s` | `ReliabilityHATest` | Test | 🔵 Pending Ph-2 |
| AVL-04 | HikariCP auto-recovers from transient DB failures | High | Ph-1 | §4.10.3, §5.3 | HikariCP `connectionTimeout`, `maxLifetime`, circuit breaker | `ReliabilityTest`, `DatabaseHealthIndicatorTest` | Test | ✅ Implemented |

### 7.5 Security Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SEC-01 | BCrypt password hashing (minimum 10 rounds) | High | Ph-1 | §5.1.1 | `AuthServiceImpl` — `BCryptPasswordEncoder(10)` | `AuthServiceImplTest`, `SecurityTest` | Inspection | ✅ Implemented |
| SEC-02 | JWT secret ≥ 512 bits; externalised; no default value | High | Ph-1 | §5.1.2 | `JwtTokenProvider` — `@Value("${jwt.secret}")` (no default), `JwtKeyValidator` | `JwtKeyValidatorTest`, `JwtTokenProviderTest` | Inspection | ✅ Implemented |
| SEC-03 | HTTPS / TLS enforced in production; startup fails without SSL | High | Ph-2 | §5.1.1, §4.4.1 | `HttpsEnforcementFilter`, `SecurityConfig.validateHttpsInProduction()` | `SecurityTest` | Test | 🔵 Pending Ph-2 |
| SEC-04 | CSRF configured for SPA (stateless; disabled for REST) | High | Ph-2 | §5.1.1 | `SecurityConfig` — `csrf.disable()` (stateless API) | `AuthenticationAuthorizationSecurityTest` | Inspection | 🔵 Pending Ph-2 |
| SEC-05 | CORS restricted to configured domains only | High | Ph-1 | §5.1.1 | `SecurityConfig` CORS allowedOrigins | `SecurityTest` | Inspection | ✅ Implemented |
| SEC-06 | SQL injection prevented via JPA parameterised queries | High | Ph-1 | §4.3.3, §5.1.1 | All `@Repository` interfaces (Spring Data JPA derived queries) | `InputValidationSecurityTest`, `DataValidationTest` | Inspection | ✅ Implemented |
| SEC-07 | Login rate-limited to 3 req / 5 min per source | High | Ph-1 | §5.1.5 | `RateLimiterService.checkLoginRateLimit()`, Bucket4j Redis | `RateLimiterServiceTest`, `AdminRateLimitFilterTest` | Test | ✅ Implemented |
| SEC-08 | Password reset rate-limited to 3 req / hr per source | High | Ph-1 | §5.1.5 | `RateLimiterService.checkPasswordResetRateLimit()`, Bucket4j Redis | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-09 | Admin endpoints rate-limited to 50 req / min | Medium | Ph-1 | §5.1.5 | `AdminRateLimitFilter`, Bucket4j Redis | `AdminRateLimitFilterTest` | Test | ✅ Implemented |
| SEC-10 | User endpoints rate-limited to 500 req / min | Medium | Ph-1 | §5.1.5 | `RateLimiterService`, Bucket4j Redis | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-11 | Product search rate-limited to 60 req / min | Medium | Ph-1 | §5.1.5 | `RateLimiterService.checkSearchRateLimit()` | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-12 | JWT secret rotation every 90 days | Medium | Ph-2 | §5.1.2 | `JwtTokenProvider` — dual-key (`jwt.secret.previous`) | `docs/operations/secrets-rotation-procedure.md` | Inspection | ✅ Implemented (#132 — dual-key mechanism + documented rotation procedure/cadence; no automated schedule enforcement, tracked manually) |
| SEC-13 | Database password rotation every 180 days | Medium | Ph-2 | Appendix A | HikariCP env var `${SPRING_DATASOURCE_PASSWORD}` | `docs/operations/secrets-rotation-procedure.md` | Inspection | ✅ Implemented (#132 — documented rotation procedure/cadence; no automated schedule enforcement, tracked manually) |
| SEC-14 | CSP must not contain `unsafe-inline` | Medium | Ph-2 | §5.1.4 | `SecurityConfig`/`SecurityHeaderPolicies.MAIN_CSP` (backend API, #237); `frontend/security-headers.conf` (frontend document CSP, #110 — removed `unsafe-inline` from `style-src`; React's `style={{}}` prop sets styles via JS property assignment, not the HTML `style` attribute, so it is not subject to the `style-src` inline restriction) | `SecurityTest`, live-browser CSP verification (#110) | Inspection + Test | ✅ Implemented |
| SEC-15 | Full OWASP Top 10 (2021) assessment (A01–A10) performed and documented before M5 gate; zero open Critical, High findings have a remediation timeline | High | Ph-2 | §5.1 (new) | `docs/SDLC-docs/reports/security-assessment.md`; `WebhookServiceImpl`/`SsrfUrlValidator` (A10 SSRF remediation) | `SsrfUrlValidatorTest`, `WebhookServiceImplTest`, OWASP ZAP full active scan (local, #111) | Inspection + Test | ✅ Implemented |
| SEC-16 | HSTS (max-age ≥31536000, includeSubDomains), X-Frame-Options: DENY, X-Content-Type-Options: nosniff, Referrer-Policy, Permissions-Policy on all API responses | High | Ph-2 | §5.1 (new) | `SecurityConfig`/`SecurityHeaderPolicies` (`REFERRER_POLICY`, `PERMISSIONS_POLICY` — #112; HSTS/X-Frame-Options/X-Content-Type-Options already Spring Security defaults) | `SecurityHeadersTest` (5/5) | Inspection + Test | ✅ Implemented |
| COMP-01 | Users shall be able to export all personal data associated with their account as JSON (GDPR right to access) | High | Ph-2 | §3.8.4 (new) | `UserController.exportMyData`, `UserServiceImpl.exportUserData`, `UserDataExportDTO` | `UserServiceImplTest#testExportUserData`, `UserControllerTest#exportMyData_*` | Inspection + Test | ✅ Implemented |
| COMP-02 | Users shall be able to request account deletion; account deactivated immediately, PII irreversibly anonymised within 30 days (GDPR right to erasure) | High | Ph-2 | §3.8.4 (new) | `UserController.deleteMyAccount`, `UserServiceImpl.deleteUser` (isActive=false + token revocation), `AccountAnonymizationScheduler`, `JwtAuthenticationFilter` (`isEnabled()` check), `20260803-001-alter-users-add-gdpr-consent-columns.xml` | `UserServiceImplTest#testDeleteUser`, `AccountAnonymizationSchedulerTest`, `JwtAuthenticationFilterTest#skipsAuthenticationWhenAccountIsDisabled` | Inspection + Test | ✅ Implemented |
| COMP-03 | Consent to the privacy policy shall be captured (with timestamp) at registration, a precondition of account creation | High | Ph-2 | §3.8.4 (new) | `RegisterRequest.consentGiven` (`@AssertTrue`), `AuthServiceImpl.register`, `PrivacyPolicyPage.tsx`, `RegisterPage.tsx` consent checkbox | `AuthServiceImplTest#testRegisterSetsUserFieldsAndValidatesPassword` | Inspection + Test | ✅ Implemented |

### 7.6 Maintainability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| MNT-01 | 100% Javadoc coverage enforced by Maven Javadoc Plugin | High | Ph-1 | §6.1 | `pom.xml` (Javadoc plugin config) | Build gate | Build | ✅ Implemented |
| MNT-02 | JaCoCo line coverage ≥ 70% | High | Ph-2 | §17 (TP) | JaCoCo in `pom.xml` — PACKAGE/INSTRUCTION `jacoco-check` rule enforces 0.85 minimum, exceeding the 0.70 target | `./mvnw verify` JaCoCo gate | Build | ✅ Implemented (verified 2026-07-17, #452 — gate is 85%, not the previously-recorded 40%) |
| MNT-03 | All unit tests: 0 failures, 0 errors | High | Ph-1 | §15 (TP) | `./mvnw test -P unit-tests` | All test classes | Build | ✅ Implemented (verified 2026-07-17, #452 — `@Tag("e2e")`, `RoleRepository` mock, and both security-test status-code assertions all confirmed fixed in source) |
| MNT-04 | All DDL changes via Liquibase changesets | High | Ph-1 | §4.5 | `src/main/resources/db/changelog/` | `DatabaseConstraintTest` | Inspection | ✅ Implemented |
| MNT-05 | Structured JSON logging via SLF4J / Logback + Logstash encoder | High | Ph-1 | §4.2.1 | `logback-spring.xml`, `@Slf4j` on 87 classes | `LoggingStandardsTest`, `SecureLoggerTest` | Inspection | ✅ Implemented |
| MNT-06 | No `System.out` or `printStackTrace` in production code | High | Ph-1 | §4.2.1 | All production `.java` files | `LoggingStandardsTest`, `DeadCodeAnalyzerTest` | Inspection | ✅ Implemented |
| MNT-07 | Generated OpenAPI 3.1 spec published as browsable API docs (Swagger UI) via GitHub Pages, kept current on each release tag | Medium | Ph-5 | N/A | `.github/workflows/publish-api-docs.yml` | N/A — tier-0 CI-config change, verified via real workflow execution + live Pages URL check, not a test class | Inspection | 🔵 Pending — live verification in progress (#127) |

### 7.7 Portability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| PRT-01 | Docker multi-stage containerisation | High | Ph-2 | §4.10 | `backend/Dockerfile`, `frontend/Dockerfile` (both multi-stage: build → runtime); `docker-compose.prod.yml` + `nginx-proxy/` now orchestrate both as a production stack with resource limits, healthchecks, and TLS termination (#119); `.github/workflows/deploy.yml` builds+pushes both images to GHCR and deploys them via SSH+`docker compose` (#120) | Docker build; live `docker run` against real MySQL/Redis with SSL enabled | Inspection, Test | ✅ Implemented (#124: backend now runs as non-root `buildnest` user on `eclipse-temurin:21-jre-alpine`, with `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`; frontend non-root already confirmed via `nginx-unprivileged`, #125; #364: remaining stale `civil-ecommerce` naming references across README/K8s manifest/CI/app config swept, re-verified via a fresh `docker build`+`docker run`) |
| PRT-02 | Kubernetes deployment manifests | High | Ph-2 | §4.10, Appendix B | `kubernetes/` (7+ manifest files) | `kubectl apply` dry run | Inspection | 🔵 Pending Ph-2 |
| PRT-03 | Terraform IaC for AWS | Medium | Ph-2 | §4.10 | `terraform/` | Terraform plan | Inspection | 🔵 Pending Ph-2 |
| PRT-04 | All configuration via environment variables (12-Factor) | High | Ph-1 | Appendix A, §6.1 | `application.properties` — all secrets via `${ENV_VAR}` | Config audit | Inspection | ✅ Implemented |

### 7.8 Scalability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SCL-01 | Stateless JWT enables horizontal pod scaling | High | Ph-1 | §5.1.2, §4.3.1 | `JwtTokenProvider` (stateless; no session store) | `JwtTokenProviderTest` | Inspection | ✅ Implemented |
| SCL-02 | HikariCP pool size configurable per environment | Medium | Ph-1 | §4.10.3 | `application.properties` HikariCP config | `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| SCL-03 | Redis-backed rate limiting shared across pods | Medium | Ph-2 | §5.1.5 | `RateLimiterService` + Bucket4j Redis backend (shared key per IP) | `RateLimiterServiceTest`, integration test | Test | 🔵 Pending Ph-2 |
| SCL-04 | Sustain ≥ 1,000 concurrent users (Gatling) | High | Ph-2 | §4.10.1 | Kubernetes HPA (CPU 75% trigger), 3 replicas base | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |

### 7.9 Safety Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SAF-01 | No unauthorised financial transaction on system failure | High | Ph-2 | §5.1.2, §4.8.2 | Razorpay signature verification before `Payment.status=SUCCESS` | `PaymentSignatureValidationServiceTest`, `PaymentServiceImplTest` | Test | 🔵 Pending Ph-2 |
| SAF-02 | No charge recorded without associated order confirmation | High | Ph-2 | §4.9.2 | `@Transactional` on `CheckoutServiceImpl.processCheckoutWithPayment()` | `CheckoutServiceImplTest`, `PaymentProcessingTest` | Test | 🔵 Pending Ph-2 |
| SAF-03 | Inventory data integrity under concurrent orders (ACID) | High | Ph-1 | §4.5.3, §4.9.3 | `InventoryServiceImpl.deductStock()` — `@Transactional` + optimistic locking (`@Version`) | `InventoryManagementTest`, `EdgeCaseAndBoundaryTest`, `DatabaseConstraintTest` | Test | ✅ Implemented |

### 7.10 Observability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| OBS-02 | Distributed trace context propagated across HTTP requests and async tasks; traces exported via OTLP for visualization | Medium | Ph-2 | §3.8.9 (new) | `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp` (`backend/pom.xml`), `management.tracing.*`/`management.otlp.tracing.endpoint` (`application.properties`), Grafana Tempo (`backend/docker-compose.yml`, `backend/tempo/tempo.yaml`, `backend/grafana/provisioning/datasources/tempo.yml`), see ADR-0004 | `TracingWiringIntegrationTest` (real-context: asserts a genuine `OtelTracer`-backed bean and a non-zero trace ID, not a mocked no-op fallback) | Test | ✅ Implemented (#108, live-verified: `GET /api/public/products`, `GET /api/auth/csrf`, and a scheduled `@Async` job all produced real traces queryable from Tempo's search API end-to-end) |
| OBS-05 | Kubernetes readiness/liveness health probes; readiness reflects real MySQL/Redis/(optional)Elasticsearch reachability | Medium | Ph-2 | §3.8.9 | `DatabaseHealthIndicator`, `RedisHealthIndicator`, `ElasticsearchHealthIndicator` (gated by `elasticsearch.enabled`) — `com.example.buildnest_ecommerce.actuator`; `management.endpoint.health.group.readiness.include`/`.group.liveness.include` (`application.properties`, `application-production.properties`); `/actuator/health/**` already `permitAll()` in `SecurityConfig` | `ElasticsearchHealthIndicatorTest` (unit, mocked `ElasticsearchClient`), `HealthEndpointIntegrationTest` (real-context: `/actuator/health/liveness`+`/readiness` structure, db/redis component presence, elasticsearch component absence when disabled) | Test | ✅ Implemented (#123) |

> **ID note**: #108's own issue body cited "RTM MON-05" (an ID that did not exist — only the
> unrelated FR-MON-05 metrics requirement does) and, separately, "OPS-01"/"OPS-02" were already
> informally claimed by SDD's own #119/#120 revision notes for deployment-topology work. This row
> instead adopts "OBS-02", matching the domain code #108's own GitHub issue title carries,
> consistent with sibling issues #107 (OBS-01) and #109 (OBS-03) — see SRS §3.8.9 for the full
> reasoning. OBS-01/OBS-03/OBS-04 are #107/#109's own scope and are not added here.
>
> **ID note (OBS-05, #123)**: the issue's own body cited "SRS NFR-OPS-06", an ID colliding with
> #124's already-used "OPS-06" (RTM revision 1.51) and mismatched to the wrong domain (`OPS-*` is
> deployment-topology, #119–#121/#124; this requirement is observability). Adopted "OBS-05",
> continuing this table's established `OBS-*` numbering — see SRS §3.8.9 for the full reasoning.

---

## 8. Design Constraints

| Req ID | Constraint | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| DC-01 | Layered monolith: Controller → Service → Repository → Model | High | Ph-1 | §4.3.1 | Full package structure; controllers never call repos | `DeadCodeAnalyzerTest`, architecture review | Inspection | ✅ Implemented |
| DC-02 | RESTful API design — HTTP methods and status codes | High | Ph-1 | §4.7.1 | All `@RestController` classes | All controller tests | Test | ✅ Implemented |
| DC-03 | All config externalised via env vars; no secrets in source | High | Ph-1 | Appendix A | `application.properties` — all secrets via `${ENV_VAR}` | `SecureLoggerTest`, config audit | Inspection | ✅ Implemented |
| DC-04 | Stateless JWT; no server-side sessions | High | Ph-1 | §5.1.2, §4.3.1 | `JwtTokenProvider`, `SecurityConfig` (session = STATELESS) | `JwtTokenProviderTest`, `SecurityTest` | Inspection | ✅ Implemented |
| DC-05 | Graceful shutdown 30-second drain | High | Ph-2 | §4.10.2 | `server.shutdown=graceful` + `lifecycle.timeout=30s` | `ReliabilityHATest` | Test | 🔵 Pending Ph-2 |
| DC-06 | Multi-stage Docker builds | Medium | Ph-2 | §4.10, §4.10.5 | `backend/Dockerfile`, `frontend/Dockerfile`; `docker-compose.prod.yml` (#119) adds production orchestration (resource limits, healthchecks, MySQL named-volume persistence, nginx-proxy TLS termination) on top of both; `.github/workflows/deploy.yml` (#120) builds+pushes both to GHCR and deploys via SSH+`docker compose` | Docker build | Inspection | 🟡 Partial (both services now build multi-stage, and backend is now non-root/Alpine/container-aware JVM per #124; frontend build-time/layer-caching per PR-08 not yet measured) |
| DC-07 | Repository access only from service layer | High | Ph-1 | §4.3.1 | Package dependencies: no `@Repository` injection in `@RestController` | Architecture review | Inspection | ✅ Implemented |
| DC-08 | Explicit JPA fetch strategy on all relationships | High | Ph-1 | §4.3.3, Appendix C | `User.roles` (EAGER), all `@OneToMany` (LAZY) | `OrderTest`, `CartTest` | Inspection | 🟡 Partial (`Category.products` and `Order.orderItems` missing explicit `FetchType.LAZY` — Baseline F-09; tracked in Appendix C of SDD) |

---

## 9. Test Integrity Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TIR-01 | E2E tests must be excluded from `unit-tests` profile; tagged `@Tag("e2e")` | High | Ph-1 | §15 (TP) | `pom.xml` excludedGroups; `@Tag("e2e")` on `ProductApiTest`, `OrderApiTest` (now in a dedicated `e2e/` test package) | `./mvnw test` must show 0 failures from missing server | Build | ✅ Implemented (verified 2026-07-17, #452 — both classes confirmed `@Tag("e2e")`-annotated) |
| TIR-02 | All `@InjectMocks` services must have `@Mock` for every dependency | High | Ph-1 | §15 (TP) | `AuthServiceImplTest` — `@Mock RoleRepository roleRepository` | `AuthServiceImplTest` — 0 NullPointerExceptions | Test | ✅ Implemented (verified 2026-07-17, #452) |
| TIR-03 | Security tests assert 403 (authenticated+unauthorised) vs 401 (unauthenticated) | Medium | Ph-1 | §15 (TP) | `AuthenticationAuthorizationSecurityTest.testRoleHierarchyEnforcement()` | Correct HTTP status assertion | Inspection | ✅ Implemented (verified 2026-07-17, #452 — asserts `status().isForbidden()`) |
| TIR-04 | Input validation tests accept 400/415 as well as 401 | Medium | Ph-1 | §15 (TP) | `InputValidationSecurityTest.testXSSPrevention()`, `testFileUploadValidation()` | Correct status code range assertions | Inspection | ✅ Implemented (verified 2026-07-17, #452 — asserts `isBadRequest()`/`isUnsupportedMediaType()`) |
| TIR-05 | PIT mutation score ≥ 75% (`service.*` and `security.*`) | Medium | Ph-2 | §15 (TP) | `pom.xml` `pitest-maven` — `mutationThreshold` currently 77 (M4 ratchet: 77% mid-M4 → 79% end-M4) | `./mvnw pitest:mutationCoverage -P coverage` | Build | 🟡 Partial (configured and enforced above the 75% requirement floor; ratchet still climbing toward its own 79% end-M4 target, verified 2026-07-17, #452) |

---

## 10. Bidirectional Traceability Index

### 10.1 Implementation Class → Requirements

| Implementation Class | Requirements Satisfied |
| :--- | :--- |
| `AuthController` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-06, FR-AUTH-07, FR-AUTH-12 |
| `AuthServiceImpl` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-09, FR-AUTH-10, FR-AUTH-12, SEC-01 |
| `TwoFactorController`, `TwoFactorServiceImpl` | FR-AUTH-12 |
| `JwtTokenProvider` | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-05, SEC-02, SCL-01, DC-04 |
| `JwtAuthenticationFilter` | FR-AUTH-09, SEC-01, DC-04 |
| `JwtKeyValidator` | FR-AUTH-05, SEC-02 |
| `RefreshTokenServiceImpl` | FR-AUTH-04, FR-AUTH-06 |
| `PasswordResetController` + `PasswordResetServiceImpl` | FR-AUTH-08 |
| `SecurityConfig` (`@Profile("!test")`) | FR-AUTH-09, SEC-02, SEC-03, SEC-05, SEC-07, DC-04 |
| `AdminRateLimitFilter` | SEC-09, SEC-07 |
| `RateLimiterService` + Bucket4j | SEC-07, SEC-08, SEC-09, SEC-10, SEC-11 |
| `RolePermissionEvaluator` | FR-AUTH-09 |
| `ProductControllerV1` | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-05 |
| `ProductControllerV2` | FR-PROD-01, FR-PROD-02, FR-PROD-03 |
| `AdminProductController` | FR-PROD-04, FR-ADM-08 |
| `ProductServiceImpl` | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-04, FR-PROD-06 |
| `ProductVariantServiceImpl`, `AdminProductController` (variant endpoints) | FR-PROD-08 |
| `ProductImageServiceImpl`, `AdminProductController` (image endpoints) | FR-PROD-09 |
| `CategoryServiceImpl`, `AdminCategoryController` | FR-ADM-09, FR-ADM-08 |
| `ProductTagServiceImpl`, `AdminProductTagController` | FR-ADM-10, FR-ADM-08 |
| `CouponServiceImpl`, `AdminCouponController` | FR-ADM-11, FR-ADM-08 |
| `ApiSunsetInterceptor` | FR-PROD-05, UR-05 |
| `CartController` + `CartServiceImpl` | FR-CART-01 to FR-CART-05 |
| `Cart` entity | FR-CART-06 |
| `CartRepository` | FR-CART-02, FR-CART-06 |
| `CheckoutController` + `CheckoutServiceImpl` | FR-CHK-01 to FR-CHK-06, FR-CHK-07, SAF-02 |
| `MultiStepCheckoutController` | FR-CHK-09 |
| `AdminOrderController` | FR-CHK-08, FR-ADM-08 |
| `OrderServiceImpl` | FR-CHK-05, FR-CHK-07, FR-CHK-08 |
| `PaymentServiceImpl` + `RazorpayClientAdapter` | FR-PAY-01, FR-PAY-02, FR-PAY-03, SAF-01 |
| `WebhookServiceImpl` | FR-PAY-04, FR-ADM-07 |
| `InventoryStatusController` | FR-INV-01, FR-INV-02 |
| `AdminInventoryController` | FR-INV-03, FR-INV-04, FR-ADM-08 |
| `InventoryServiceImpl` | FR-INV-01 to FR-INV-05, FR-CHK-06, SAF-03 |
| `InventoryThresholdManagementService` | FR-INV-06, FR-ADM-06 |
| `InventoryAnalyticsService` + `InventoryReportService` | FR-INV-07, FR-ADM-02 |
| `ProductReviewController` + `ProductReviewServiceImpl` | FR-REV-01 to FR-REV-03 |
| `WishlistController` + `WishlistServiceImpl` | FR-WISH-01, FR-WISH-02 |
| `AuditLogController` + `AuditLogService` | FR-ADM-04 |
| `AuditAspect` + `@Auditable` | FR-ADM-04, MNT-05 |
| `SalesAnalyticsController` + `SalesAnalyticsServiceImpl` | FR-ADM-01 |
| `AdminReportController` | FR-ADM-05 |
| `AdminUserController` | FR-ADM-03, FR-ADM-08 |
| `GlobalExceptionHandler` | UR-01, UR-02 |
| `ResilienceConfig` | REL-02, REL-03 |
| `CacheConfig` | FR-PROD-06, FR-PROD-07, PR-07 |
| `PerformanceMonitoringInterceptor` | FR-MON-05, PR-01 |
| `HttpsEnforcementFilter` | SEC-03, CI-02 |
| `SecurityConfig.validateHttpsInProduction()` | SEC-02, SEC-03, FR-AUTH-05 |
| HikariCP config in `application.properties` | PR-05, PR-06, SCL-02, AVL-04 |
| Liquibase changelogs | MNT-04, DC-08, SAF-03 |
| `application.properties` (env vars) | DC-03, PRT-04, SEC-02, FR-AUTH-03, FR-AUTH-04 |
| `logback-spring.xml` + `@Slf4j` | MNT-05, MNT-06 |
| `kubernetes/` manifests | AVL-01, DC-05, PRT-02, SCL-04, FR-MON-06 |
| `kubernetes/prometheus-rules.yaml` | FR-MON-08 |
| `DomainEventPublisher` + `DomainEventListener` | FR-INV-06, FR-PAY-04, FR-NOT-01 to FR-NOT-03 |

### 10.2 Test Class → Requirements

| Test Class | Requirements Verified |
| :--- | :--- |
| `AuthServiceImplTest` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-10, SEC-01 (**TIR-02 defect present**) |
| `AuthControllerTest` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-06, FR-AUTH-07, FR-AUTH-09 |
| `JwtTokenProviderTest` | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-05, SEC-02 |
| `JwtKeyValidatorTest` | FR-AUTH-05, SEC-02 |
| `JwtAuthenticationFilterTest` | FR-AUTH-09, DC-04 |
| `RefreshTokenServiceTest` | FR-AUTH-04, FR-AUTH-06 |
| `PasswordResetControllerTest`, `PasswordResetServiceImplTest` | FR-AUTH-08 |
| `AuthenticationAuthorizationSecurityTest` | FR-AUTH-09, FR-ADM-08, SEC-01, UR-01, UR-02 (**TIR-03 defect present**) |
| `InputValidationSecurityTest` | SEC-06, UR-01 (**TIR-04 defect present**) |
| `SecurityTest`, `RBACTest` | FR-AUTH-09, SEC-05, DC-04 |
| `RolePermissionEvaluatorTest` | FR-AUTH-09 |
| `AdminRateLimitFilterTest` | SEC-09, FR-ADM-08 |
| `RateLimiterServiceTest` | SEC-07, SEC-08, SEC-09, SEC-10, SEC-11 |
| `ProductControllerV1Test`, `ProductControllerV2Test` | FR-PROD-01 to FR-PROD-05, UR-04 |
| `ProductServiceImplTest` | FR-PROD-01 to FR-PROD-07 |
| `ProductRepositoryTest`, `ProductRepositoryDefaultMethodTest` | FR-PROD-01, FR-PROD-02 |
| `ApiSunsetInterceptorTest` | FR-PROD-05, UR-05 |
| `ProductApiTest` (E2E) | FR-PROD-01 to FR-PROD-05 (**TIR-01 defect present**) |
| `ProductVariantServiceImplTest`, `AdminProductVariantControllerIntegrationTest` | FR-PROD-08 |
| `ProductImageServiceImplTest`, `AdminProductImageControllerIntegrationTest` | FR-PROD-09 |
| `CategoryServiceImplTest`, `CategoryTest`, `AdminCategoryControllerIntegrationTest` | FR-ADM-09 |
| `TagsTab.test.tsx` | FR-ADM-10 |
| `AdminCouponControllerTest`, `CouponServiceImplTest`, `CouponsTab.test.tsx` | FR-ADM-11 |
| `CartControllerTest` | FR-CART-01 to FR-CART-05 |
| `CartServiceImplTest`, `CartServiceImplEnhancedTest` | FR-CART-01 to FR-CART-05 |
| `CartRepositoryTest` | FR-CART-02, FR-CART-06 |
| `CartApiTest` (E2E) | FR-CART-01 to FR-CART-05 |
| `CheckoutControllerTest` | FR-CHK-01 to FR-CHK-04 |
| `CheckoutServiceImplTest` | FR-CHK-01 to FR-CHK-06, SAF-02 |
| `OrderServiceImplTest`, `OrderProcessingComprehensiveTest` | FR-CHK-05, FR-CHK-07 |
| `OrderServiceIntegrationTest` | FR-CHK-05, FR-CHK-06, SAF-03 |
| `OrderApiTest` (E2E) | FR-CHK-05, FR-CHK-07 (**TIR-01 defect present**) |
| `AdminOrderControllerTest` | FR-CHK-08, FR-ADM-08 |
| `ShippingStep.test.tsx`, `PaymentStep.test.tsx` | FR-CHK-09 |
| `PaymentServiceImplTest` | FR-PAY-01, FR-PAY-02, FR-PAY-03 |
| `PaymentSignatureValidationServiceTest` | FR-PAY-02, SAF-01 |
| `RazorpayClientAdapterTest` | FR-PAY-01 |
| `PaymentProcessingTest` | FR-PAY-01 to FR-PAY-03, SAF-01, SAF-02 |
| `WebhookServiceImplTest`, `WebhookAdminControllerTest` | FR-PAY-04, FR-ADM-07 |
| `InventoryStatusControllerTest` | FR-INV-01, FR-INV-02 |
| `InventoryServiceImplTest`, `InventoryServiceImplEnhancedTest` | FR-INV-01 to FR-INV-05, FR-CHK-06 |
| `InventoryManagementTest` | FR-INV-01 to FR-INV-05, SAF-03 |
| `InventoryThresholdManagementServiceTest` | FR-INV-06, FR-ADM-06 |
| `InventoryAnalyticsServiceTest`, `InventoryReportServiceTest` | FR-INV-07, FR-ADM-02 |
| `AdminInventoryControllerTest` | FR-INV-03, FR-INV-04, FR-ADM-08 |
| `ProductReviewControllerTest`, `ProductReviewServiceImplTest` | FR-REV-01 to FR-REV-03 |
| `WishlistControllerTest`, `WishlistServiceImplTest` | FR-WISH-01, FR-WISH-02 |
| `AuditAspectTest` | FR-ADM-04, MNT-05 |
| `AuditLogServiceTest`, `AuditLogControllerTest` | FR-ADM-04 |
| `SalesAnalyticsControllerTest`, `SalesAnalyticsServiceImplTest`, `AnalyticsReportingTest` | FR-ADM-01 |
| `AdminDashboardTest` | FR-ADM-01, FR-ADM-02 |
| `AdminUserControllerTest` | FR-ADM-03, FR-ADM-08 |
| `AdminReportControllerTest` | FR-ADM-05 |
| `AdminInventoryThresholdControllerTest` | FR-ADM-06, FR-ADM-08 |
| `HealthIndicatorTest` | FR-MON-01 |
| `DatabaseHealthIndicatorTest` | FR-MON-02, AVL-02 |
| `RedisHealthIndicatorTest` | FR-MON-03, AVL-02 |
| `PerformanceMetricsControllerTest` | FR-MON-05 |
| `PoolMetricsControllerTest` | FR-MON-05, PR-05 |
| `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | UR-01, UR-02 |
| `ReliabilityTest`, `ReliabilityHATest` | REL-02, REL-03, AVL-04 |
| `PerformanceTest`, `PerformanceBaselineTest` | PR-05, PR-06 |
| `LoadTestSimulation` (Gatling) | PR-01, PR-02, PR-03, SCL-04 |
| `DatabaseConstraintTest` | MNT-04, SAF-03 |
| `LoggingStandardsTest` | MNT-05, MNT-06 |
| `SecureLoggerTest` | SEC-04, MNT-05 |
| `CacheMetricsUtilTest` | FR-PROD-06, PR-07 |
| `EdgeCaseAndBoundaryTest` | SAF-03, FR-CART-05, FR-CHK-02 |
| `DatabaseQueryOptimizationPatternsTest` | DC-08, PR-01 |
| `ValidationUtilTest`, `DataValidationTest`, `InputValidationTest` | SEC-06, UR-01 |
| `CategoryServiceImplTest`, `CategoryManagementTest`, `CategoryRepositoryTest` | FR-PROD-03, FR-PROD-07 |
| `ApiIntegrationTest`, `OrderServiceIntegrationTest` | FR-CHK-01 to FR-CHK-07 (integration) |
| Entity tests (all `*Test.java` in `model.entity`) | FR-CART-06, FR-CHK-05, DC-07 |
| Elasticsearch tests | FR-MON-07, FR-ADM-04 (ES path) |

---

## 11. Resolved Defects (Formerly Blocking Phase 1 Exit)

**All six defects below are resolved** — verified directly against source on 2026-07-17 (#452). This section previously blocked Phase 1 exit; retained here as a historical record rather than deleted outright, per §13's maintenance procedure (remove from "open" listing once resolved and verified).

| Defect ID | TIR / Req | File | Symptom (as originally recorded) | Root Cause | Resolution — Verified 2026-07-17 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| DEF-001 | TIR-02 / MNT-03 | `AuthServiceImplTest.java` | `NullPointerException` in 3 test methods (`testRegisterSuccess`, `testRegisterPublishesEvent`, `testRegisterSetsUserFieldsAndValidatesPassword`) | `RoleRepository roleRepository` not declared as `@Mock`; field is null when `@InjectMocks` creates `AuthServiceImpl` | ✅ `@Mock RoleRepository roleRepository` is present |
| DEF-002 | TIR-01 / MNT-03 | `ProductApiTest.java` | HTTP 500 in 4 test methods (`testGetProductsByCategory`, `testSearchProducts`, `testGetProductDetails`, `testGetDeprecatedV1Products`) | E2E test class runs in `unit-tests` Maven profile; no server is running; RestAssured connection fails | ✅ `@Tag("e2e")` present; class now lives in a dedicated `e2e/product/` package |
| DEF-003 | TIR-01 / MNT-03 | `OrderApiTest.java` | HTTP 500 (presumed, consistent with ProductApiTest) | Same root cause as DEF-002 | ✅ `@Tag("e2e")` present; class now lives in a dedicated `e2e/order/` package |
| DEF-004 | TIR-03 / MNT-03 | `AuthenticationAuthorizationSecurityTest.java:246` | Test asserts HTTP 401; receives HTTP 403 | `testRoleHierarchyEnforcement` tests an authenticated user accessing an admin endpoint; the correct response is 403 (Forbidden), not 401 (Unauthorized) | ✅ Assertion is `status().isForbidden()` |
| DEF-005 | TIR-04 / MNT-03 | `InputValidationSecurityTest.java:168` | Test asserts HTTP 401; receives HTTP 400 | `testXSSPrevention` sends XSS payload; Spring's `MethodArgumentNotValidException` (400) fires before JWT authentication filter | ✅ Assertion is `status().isBadRequest()` |
| DEF-006 | TIR-04 / MNT-03 | `InputValidationSecurityTest.java:303` | Test asserts HTTP 401; receives HTTP 415 | `testFileUploadValidation` sends wrong Content-Type; Spring's `HttpMediaTypeNotSupportedException` (415) fires before JWT filter | ✅ Assertion is `status().isUnsupportedMediaType()` |

---

## 12. Phase Completion Summary

### Phase 1 — Stabilization

| Category | Implemented | Open Defects | Pending Ph-2 | Phase 1 Completion |
| :--- | :--- | :--- | :--- | :--- |
| Functional (FR-AUTH, FR-PROD, FR-CART, FR-CHK, FR-INV, FR-REV, FR-WISH, FR-ADM partial) | 46 | 0 | 0 | ✅ |
| Security (SEC-01 to SEC-11) | 9 | 0 | 0 | ✅ |
| Design Constraints (DC-01 to DC-07) | 7 | 0 | 0 | ✅ |
| Reliability (REL-02, REL-03) | 2 | 0 | 0 | ✅ |
| Availability (AVL-04) | 1 | 0 | 0 | ✅ |
| Maintainability (MNT-01, MNT-03, MNT-04, MNT-05, MNT-06) | 5 | 0 | 0 | ✅ |
| Portability (PRT-04) | 1 | 0 | 0 | ✅ |
| Scalability (SCL-01, SCL-02) | 2 | 0 | 0 | ✅ |
| Safety (SAF-03) | 1 | 0 | 0 | ✅ |
| Test Integrity (TIR-01 to TIR-04) | 4 | 0 | 0 | ✅ |

> **Phase 1 is no longer blocked.** All six defects (DEF-001 through DEF-006) across TIR-01 to TIR-04 and MNT-03 were verified resolved on 2026-07-17 (#452) — see §11 for the resolution record. TIR-05 (Ph-2, PIT mutation score) remains 🟡 Partial, not a Phase 1 gate item.

### Phase 2 — Production Readiness

| Category | Total Ph-2 Requirements | Started | Not Started |
| :--- | :--- | :--- | :--- |
| Frontend (FR-FE-01 to FR-FE-31) | 31 | 28 (21 ✅ Implemented + 7 🟡 Partial) | 3 |
| Security (SEC-03, SEC-04, SEC-12, SEC-13, SEC-14) | 5 | 4 (SEC-12 ✅ #132, SEC-13 ✅ #132, SEC-14 ✅ #110) | 1 |
| Monitoring (FR-MON-02 to FR-MON-08) | 6 | 1 (FR-MON-05 partial) | 5 |
| Payment full flow (FR-PAY-01 to FR-PAY-05) | 5 | 3 (partial) | 2 |
| Performance / Scalability / Reliability (PR, REL, SCL) | 13 | 0 | 13 |
| Availability (AVL-01 to AVL-03) | 3 | 0 | 3 |
| Admin full suite (FR-ADM-01 to FR-ADM-07) | 6 | 4 (FR-ADM-01 ✅ #431, FR-ADM-02 ✅ #432, FR-ADM-06 partial, FR-ADM-07 ✅) | 2 |
| Maintainability (MNT-02, TIR-05) | 2 | 2 (MNT-02 now ✅ at 85% JaCoCo gate, corrected 2026-07-17 from a stale 40% record; TIR-05 at 77% PIT, ratcheting to 79% end-M4) | 0 |
| Auth / Safety / Checkout / Inventory Ph-2 | 10 | 1 (FR-INV-07 ✅ #433) | 9 |
| **Phase 2 total** | **81** | **42*** | **39** |

*\* The MNT-02/TIR-05 row was corrected 2026-07-17 (#452); the Frontend row was corrected 2026-07-17 (#453, full per-requirement audit — see §6.10). Recomputing this table from its own corrected rows also fixed a pre-existing, unrelated arithmetic error: the row totals had always summed to 81, not the 80 this table previously stated (31+5+6+5+13+3+6+2+10 = 81) — a separate, mechanical off-by-one that predates and is independent of the Frontend staleness this issue targeted. The Admin row was updated 2026-07-21 (#431, FR-ADM-01 sales analytics dashboard UI implemented; #432, FR-ADM-02 inventory analytics dashboard UI implemented). The Auth/Safety/Checkout/Inventory row was updated 2026-07-21 (#433, FR-INV-07 inventory threshold & breach reporting UI implemented). The Security row was updated 2026-08-05 (#132, SEC-12/SEC-13 secrets rotation procedure documented — Started 2→4, Not Started 3→1, Phase 2 total Started 40→42, Not Started 41→39).*

---

## 13. RTM Maintenance Procedure

Per ISO/IEC/IEEE 29148:2018 §6.2.5 and ISO/IEC/IEEE 12207:2017 §6.4.2:

1. **On new requirement**: Add row to the appropriate RTM section; status = ⬜ Not Started. Assign SDD design element before implementation begins.
2. **On design change**: Update SDD Reference column; verify design element still satisfies requirement; flag for re-inspection if design changes test assumptions.
3. **On implementation**: Update Implementation column; set status to 🟡 Partial. Assign or create test class before marking ✅ Implemented.
4. **On defect discovery**: Set status to 🔴 Open Defect; add row to §11; file issue tracker entry. Do not change status to ✅ until defect is resolved and verified.
5. **On defect resolution**: Remove from §11; set status to ✅ Implemented; update test class column.
6. **On phase gate review**: Update §12 Phase Completion Summary; obtain sign-off from Test Manager and Technical Lead.
7. **RTM version**: Increment patch version on any row-level change; increment minor version on structural changes; increment major version on re-baseline.

---

**— End of Document —**

*This RTM was prepared in conformance with ISO/IEC/IEEE 29148:2018 §6.2.5 (Traceability) for the BuildNest E-Commerce Platform. All requirement IDs, statuses, implementation references, and test class assignments are traceable to SRS-BUILDNEST-001 v4.0, SDD-BUILDNEST-001 v3.0, TP-BUILDNEST-001 v4.0, and the Baseline Assessment Report dated 2026-06-19. This document shall be maintained throughout the software lifecycle and updated on every requirement, design, implementation, or test change.*
