# Software Development Plan (SDP)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Software Development Plan (SDP) |
| **Document ID** | SDP-BUILDNEST-001 |
| **Version** | 1.4 |
| **Date** | 2026-08-03 IST |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 12207:2017 — Software Life Cycle Processes; ISO/IEC/IEEE 15288:2023 — System Life Cycle Processes; IEEE Std 1058:2016 — Software Project Management Plans |
| **Related SRS** | SRS-BUILDNEST-001 v5.8 — `docs/SDLC-docs/requirement-engineering/software-requirements-specification.md` |
| **Related SDD** | SDD-BUILDNEST-001 v4.13 — `docs/SDLC-docs/design/software-design-description.md` |
| **Related TP** | TP-BUILDNEST-001 v4.5 — `docs/SDLC-docs/software-testing/test-plan.md` |
| **Related RTM** | RTM-BUILDNEST-001 v1.43 — `docs/SDLC-docs/requirement-engineering/requirements-traceability-matrix.md` |
| **Baseline Assessment** | `docs/reports/baseline-assessment-2026-06-19.md` |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-06-19 | Project Manager | Initial controlled release — evidence-based brownfield SDP derived from Baseline Assessment, SRS v4.0, SDD v3.0, TP v4.0, RTM v1.0, and live CI/CD workflow analysis; covers two-phase delivery (Ph-1 Stabilization → Ph-2 Production Readiness) with five milestones | Pending |
| 1.1 | 2026-07-17 14:41 IST | Project Manager | Corrected stale test/source-file counts (256→352 source files, 173→195 test files, 1,538→1,735 executions, 14 failures→0), the false "frontend stub only" claim (71 real source files, substantial working SPA), Jedis→Lettuce client, and Elasticsearch 8.10→8.17 throughout. Resolved the "Elasticsearch EOL" risk entry — the required 8.17+ upgrade already happened (`docker-compose.yml`'s active service runs 8.17.6) — and corrected the JaCoCo/PIT gate targets to reflect the actual enforced values (85% JaCoCo, 77% PIT, both already exceeding Ph-1/Ph-2 targets) (#461) | Pending |
| 1.2 | 2026-07-17 21:15 IST | Project Manager | Found during a fresh RTM/SRS/SDD/Test-Plan verification sweep: both the header `Related SRS/SDD/TP/RTM` fields and Appendix D's "Document Suite Summary" table still cited every sibling document at its original 2026-06-19 baseline version (SRS v4.0, SDD v3.0, TP v4.0, RTM v1.0) despite each having since moved through several real content-driven version bumps — this document's own cross-references had never been updated even once since the initial release. Updated all to current (SRS v4.5, SDD v3.5, TP v4.2, RTM v1.7, SDP itself v1.2) | Pending |
| 1.3 | 2026-07-30 IST | Project Manager | Periodic 15-issue SDLC documentation sync (overdue — last full sync at #452, 2026-07-17; 53 issues closed since). Same drift class as 1.2, recurred: header `Related SRS/SDD/TP/RTM` fields and Appendix D's Document Suite Summary table had again gone stale since the last sync (SRS v4.5→v5.8, SDD v3.5→v4.13, TP v4.2→v4.5, RTM v1.7→v1.43) — this document's cross-references are a pure mirror of the other four documents' own version numbers and drift every time any of them bumps without a dedicated sync pass. Updated both the header and Appendix D to current | Pending |
| 1.4 | 2026-08-03 IST | Project Manager | §5.7.3's CI/CD Workflow Inventory table corrected `deploy.yml`'s row — it previously described a K8s deploy that never existed; now describes the real GHCR-push + SSH/`docker compose` mechanism built for #120 (OPS-02, ADR 0003). §5.7.2's ASCII pipeline diagram (Deploy Pipeline / Production stages, still describing K8s + Argo Rollouts blue-green) was found stale by the same read but left untouched — it's pre-existing aspirational content unrelated to this issue's own scope; filed as a separate follow-up rather than rewritten here | Pending |

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| QA Manager | _____________ | _____________ | _____________ |
| Configuration Manager | _____________ | _____________ | _____________ |

### Document Change Procedure

1. **Change Request (CR)**: Submit CR with rationale, impact assessment, and reference to affected SDP section.
2. **Impact Assessment**: Technical Lead assesses impact on schedule, resources, scope, and dependent documents.
3. **Review and Approval**: Project Manager approves or rejects within 3 business days.
4. **Implementation**: Approved changes are applied, version incremented, and document re-baselined.
5. **Distribution**: Updated document is committed to `docs/SDLC-docs/project-planning/` and stakeholders notified.

---

## CONFORMANCE STATEMENT

> This document conforms to:
> - **ISO/IEC/IEEE 12207:2017** — *Systems and software engineering — Software life cycle processes* (Technical, Project, and Organisational processes)
> - **IEEE Std 1058:2016** — *Standard for Software Project Management Plans* (SPMP structure and content)
> - **ISO/IEC/IEEE 15288:2023** — *Systems and software engineering — System life cycle processes* (life cycle model and process framework)
>
> Sections map to IEEE 1058:2016 clauses as documented in §17 (Standard Alignment Matrix).

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Overview](#2-project-overview)
3. [Life Cycle Model](#3-life-cycle-model)
4. [Development Process Definitions](#4-development-process-definitions)
5. [Technical Processes](#5-technical-processes)
6. [Supporting Processes](#6-supporting-processes)
7. [Organisational Processes](#7-organisational-processes)
8. [Project Schedule](#8-project-schedule)
9. [Resource Plan](#9-resource-plan)
10. [Risk Management Plan](#10-risk-management-plan)
11. [Quality Assurance Plan](#11-quality-assurance-plan)
12. [Configuration Management Plan](#12-configuration-management-plan)
13. [Communication and Coordination Plan](#13-communication-and-coordination-plan)
14. [Measurement and Metrics Plan](#14-measurement-and-metrics-plan)
15. [Training Plan](#15-training-plan)
16. [Supplier and Dependency Management](#16-supplier-and-dependency-management)
17. [Standard Alignment Matrix](#17-standard-alignment-matrix)
18. [Appendices](#18-appendices)

---

## 1. Introduction

### 1.1 Purpose

This Software Development Plan (SDP) defines the complete technical, organisational, and management processes that govern the development of the **BuildNest E-Commerce Platform**. It establishes the life cycle model, development methodology, resource allocation, schedule, quality gates, configuration management strategy, risk management approach, and all supporting processes required to deliver the system to the acceptance criteria defined in SRS-BUILDNEST-001 v4.0.

This SDP serves as the single authoritative reference for:

- How the project is structured and governed
- How software is developed, tested, integrated, and released
- How quality, configuration, and risk are managed
- How progress is measured and communicated
- What obligations apply at each milestone gate

**Context**: BuildNest is a **brownfield** project — the codebase exists, the architecture is established, and the Baseline Assessment Report (2026-06-19) has characterised the current state. This SDP therefore governs the **Remediation and Delivery** phases of a brownfield SDLC workflow, not a greenfield development initiation.

### 1.2 Scope

| Scope Item | Included | Reference |
| :--- | :--- | :--- |
| Backend REST API (Spring Boot 3.5.10, Java 21) | Yes | SRS §1.2, SDD §4.2.1 |
| Frontend SPA (React 19.2, Vite 8.0) | Yes — Phase 2 | SRS §3.2.10 |
| Infrastructure (Docker, Kubernetes, Terraform) | Yes — Phase 2 | SRS §3.8.5 |
| CI/CD pipeline (GitHub Actions — 6 workflows) | Yes — both phases | §5.9 |
| Third-party service internals (Razorpay, Logstash) | No | SRS §1.2 |
| Native mobile applications | No | SRS §2.6 |
| Physical infrastructure management | No | SRS §1.2 |

### 1.3 Audience

| Audience | Usage of This Document |
| :--- | :--- |
| Project Manager | Schedule, resource plan, risk register, milestone gate decisions |
| Technical Lead | Process definitions, architectural constraints, technical standards |
| Developers | Development conventions, branching strategy, CI gate requirements |
| QA Engineers | Quality assurance activities, test strategy linkage, defect management |
| DevOps Engineers | Deployment process, environment configuration, release procedures |
| Configuration Manager | CM plan, branching model, baseline management |
| Auditors / Reviewers | Standards compliance, process evidence, traceability |

### 1.4 Definitions and Acronyms

| Term | Definition |
| :--- | :--- |
| **Baseline** | A formally approved, versioned configuration item that can only be changed through change control |
| **CI Gate** | A mandatory automated check in the CI pipeline that must pass before a change is accepted |
| **Brownfield** | A development context where an existing codebase is being assessed, remediated, and extended |
| **Milestone** | A named point in the project schedule where specific exit criteria must be satisfied |
| **Sprint** | A time-boxed iteration (2 weeks) of development, test, and review activities |
| **SDP** | Software Development Plan (this document) |
| **SCM** | Software Configuration Management |
| **CR** | Change Request — formal record of a proposed change to a baselined artefact |
| **NCR** | Non-Conformance Report — formal record of a deviation from a plan, standard, or requirement |
| **CAPA** | Corrective and Preventive Action |
| **WBS** | Work Breakdown Structure |
| **DoD** | Definition of Done — criteria a task must satisfy before it is marked complete |
| **DoR** | Definition of Ready — criteria a task must satisfy before it enters a sprint |

### 1.5 References

| ID | Document | Version |
| :--- | :--- | :--- |
| REF-01 | ISO/IEC/IEEE 12207:2017 — Software Life Cycle Processes | 2017 |
| REF-02 | ISO/IEC/IEEE 15288:2023 — System Life Cycle Processes | 2023 |
| REF-03 | IEEE Std 1058:2016 — Software Project Management Plans | 2016 |
| REF-04 | ISO/IEC 25010:2011 — Software Quality Models | 2011 |
| REF-05 | ISO/IEC/IEEE 29148:2018 — Requirements Engineering | 2018 |
| REF-06 | OWASP ASVS 4.0 — Application Security Verification Standard | 4.0 |
| REF-07 | SRS-BUILDNEST-001 v4.0 | 2026-06-19 |
| REF-08 | SDD-BUILDNEST-001 v3.0 | 2026-06-19 |
| REF-09 | TP-BUILDNEST-001 v4.0 | 2026-06-19 |
| REF-10 | RTM-BUILDNEST-001 v1.0 | 2026-06-19 |
| REF-11 | BuildNest Baseline Assessment Report | 2026-06-19 |
| REF-12 | 12-Factor App Methodology | 1.0 |

---

## 2. Project Overview

### 2.1 Product Description

BuildNest is a web-based e-commerce platform specialising in home construction materials and décor products. It provides a REST API backend and a React SPA frontend supporting the full e-commerce lifecycle: user authentication, product browsing, cart management, checkout, payment processing, inventory management, and administrative operations.

| Attribute | Current State | Target State |
| :--- | :--- | :--- |
| Backend | Spring Boot 3.5.10 / Java 21 — 352 source files, compiles, 100% test pass rate (verified 2026-07-17, #461) | 0 test failures, ≥ 70% JaCoCo coverage, 75% PIT mutation score — both already exceeded (85% JaCoCo, 77% PIT) |
| Frontend | React 19.2 / Vite 8.0 — 71 real source files, substantial working SPA (home, product listing/detail, cart, checkout, login/register, account, admin dashboard) verified 2026-07-17; full FR-FE-01–30 per-requirement audit tracked separately (#453, #459) | Fully functional SPA covering all FR-FE-01 to FR-FE-30 |
| Infrastructure | Docker, Kubernetes, Terraform manifests present | Fully validated staging and production deployment |
| Test Suite | 195 test files, 1,735 executions — 0 failures / errors (verified 2026-07-17, #461) | 0 failures, complete E2E coverage of critical paths — already achieved |
| CI/CD | 6 GitHub Actions workflows present | All gates passing on every push to `develop` and `main` |

### 2.2 Project Context — Brownfield Workflow

This project follows a **three-phase brownfield SDLC workflow**:

```
Phase 1 — Discovery (COMPLETE)
  ├── System Operational State Assessment (compile, test, measure)
  ├── Baseline Assessment Report
  ├── SRS v4.0 (target state definition)
  ├── SDD v3.0 (design description with corrections)
  ├── Test Plan v4.0
  └── RTM v1.0

Phase 2 — Remediation (CURRENT)
  ├── Milestone 1: Stabilization (Ph-1 gate)
  ├── Milestone 2: Quality Foundation
  └── Milestone 3: Technical Debt Reduction

Phase 3 — Deployment and Readiness (FUTURE)
  ├── Milestone 4: Feature Development (frontend)
  └── Milestone 5: Production Readiness (Ph-2 gate)
```

### 2.3 Delivery Phases

| Phase | Name | Success Criterion | Gate Owner |
| :--- | :--- | :--- | :--- |
| **Ph-1** | Stable | `./mvnw test` → 0 failures, 0 errors; E2E tests isolated; CI gate green | Technical Lead |
| **Ph-2** | Production Ready | JaCoCo ≥ 70%; PIT ≥ 75%; frontend functional; staging validated; all SEC-* and NFR-* met | Project Manager + Technical Lead |

### 2.4 Project Constraints

| Constraint | Detail | Source |
| :--- | :--- | :--- |
| Language | Java 21 LTS; no downgrade | CON-01 |
| Framework | Spring Boot 3.5.10; no version downgrade without CR | CON-02 |
| Database | MySQL 8.2; schema via Liquibase only | CON-03, CON-12 |
| Cache | Redis 7 with Lettuce client | CON-04 |
| Payment | Razorpay only; no alternative gateway | CON-05 |
| Security | JWT HMAC-SHA512 ≥ 512 bits; HTTPS in production | CON-06 |
| Build | Apache Maven with Maven Wrapper (`mvnw`) | CON-07 |
| Deployment | Docker + Kubernetes; Terraform IaC | CON-08 |
| Regulatory | PCI-DSS v4.0 (payment); GDPR 2018 (personal data) | CON-09 |
| Frontend | React 19.2 + Vite 8.0 | CON-11 |
| Schema | `ddl-auto=validate`; all DDL via Liquibase | CON-12 |

---

## 3. Life Cycle Model

### 3.1 Selected Model

BuildNest adopts a **hybrid iterative-incremental model** within the brownfield context:

- **Iterative**: Each sprint produces a potentially releasable increment of quality improvement. Defects discovered in one iteration inform the next.
- **Incremental**: New features (frontend, payment full flow) are added in complete vertical slices.
- **Phase-gated**: Each of the five milestones has explicit, measurable exit criteria. No milestone can be declared complete without all exit criteria satisfied.

This model was selected over a pure Waterfall model because the codebase is already developed and the primary risk is **stability and quality**, not requirements uncertainty. It was selected over a pure Agile model because the brownfield context requires formal baseline documentation (SRS, SDD, RTM) before remediation begins.

### 3.2 Life Cycle Phases

| Phase | Name | Duration | Primary Outputs |
| :--- | :--- | :--- | :--- |
| **Discovery** | Baseline + Planning | Completed | Baseline Assessment, SRS v4.0, SDD v3.0, TP v4.0, RTM v1.0, SDP v1.0 |
| **Remediation** | Stabilization + Quality | Milestones 1–3 | Defect-free build, coverage gate, CI green |
| **Development** | Feature + Integration | Milestone 4 | Frontend, payment full flow, K8s validated deployment |
| **Readiness** | Production Hardening | Milestone 5 | Performance, security, accessibility, DR validation |

### 3.3 Iteration Structure

Each sprint is **2 weeks** long:

| Sprint Day | Activity |
| :--- | :--- |
| 1 | Sprint planning: backlog refinement, task assignment, DoR check |
| 1–9 | Development, unit testing, code review, CI gate adherence |
| 9 | Integration testing, SIT sign-off |
| 10 | Sprint review, retrospective, DoD check, milestone gate assessment |

### 3.4 Milestone Gate Criteria

Gates are binary — all criteria must be met. Partial completion does not permit gate passage.

| Milestone | Name | Phase Gate | Key Exit Criteria |
| :--- | :--- | :--- | :--- |
| M1 | Stabilization | Ph-1 prerequisite | 0 unit test failures; 0 unit test errors; E2E tests isolated (TIR-01 to TIR-04 resolved) |
| M2 | Quality Foundation | Ph-1 exit | JaCoCo gate at ≥ 50%; all CI pipelines green; `all-tests` profile clean |
| M3 | Technical Debt Reduction | Pre-Ph-2 | DC-08 fetch strategy corrected; SAF-03 Optional.get() guarded; performance baselines established |
| M4 | Feature Development | Ph-2 prerequisite | Frontend FR-FE-01 to FR-FE-30 implemented; payment full flow active; admin suite complete |
| M5 | Production Readiness | Ph-2 exit | JaCoCo ≥ 70%; PIT ≥ 75%; SEC-14 CSP fixed; performance SLOs met; DR validated; staging clean |

---

## 4. Development Process Definitions

### 4.1 Definition of Ready (DoR)

A task may not enter a sprint unless all the following are true:

| Criterion | Description |
| :--- | :--- |
| Requirement traced | Task references at least one SRS requirement ID |
| Acceptance criteria defined | Clear, testable pass/fail criteria stated |
| Design consulted | SDD section identified (or new design element proposed and reviewed) |
| RTM entry exists | Row in RTM-BUILDNEST-001 created with status ⬜ Not Started |
| No blocking dependency | All prerequisite tasks are complete or explicitly scheduled |
| Test plan entry | Test class named and test scenarios identified |
| Estimate agreed | Story points / effort estimate reviewed by Technical Lead |

### 4.2 Definition of Done (DoD)

A task is **Done** only when all of the following are true:

| Criterion | Description |
| :--- | :--- |
| Code complete | Feature or fix is implemented per the SDD design element |
| Unit tests written | New or modified logic covered by at least one positive and one negative test case |
| Tests passing | `./mvnw test` reports 0 failures; the new test class is in the correct Maven profile |
| CI gate green | All CI pipeline jobs pass on the branch before merge |
| Code review approved | At minimum one peer review approval; Technical Lead review for security-touching changes |
| RTM updated | RTM row status updated to ✅ Implemented or 🟡 Partial |
| Javadoc complete | All public methods and classes have Javadoc per MNT-01 |
| No regressions | Existing passing tests continue to pass; JaCoCo coverage does not decrease |
| No new Checkstyle violations | `./mvnw checkstyle:check` passes |
| Logging correct | All log statements use SLF4J `@Slf4j`; no `System.out` or `printStackTrace` |
| No secrets in code | No hardcoded credentials, tokens, keys, or passwords |

### 4.3 Coding Standards

All Java source code shall conform to the following (enforced by `checkstyle.xml` in CI):

| Rule | Standard |
| :--- | :--- |
| Naming | `camelCase` for fields and methods; `PascalCase` for classes; `UPPER_SNAKE_CASE` for constants |
| Indentation | 4 spaces; no tabs |
| Line length | Maximum 120 characters |
| Imports | No wildcard imports; alphabetically ordered by group |
| Braces | Allman style for class and method bodies; K&R for control flow |
| Comments | Javadoc on all public members; inline comments only for non-obvious business rules |
| Logging | SLF4J + `@Slf4j`; parameterised messages only; no string concatenation in log calls |
| Exception handling | Specific exception types; no empty `catch` blocks; all caught exceptions logged or rethrown |
| `@Transactional` | Declared on service implementation methods; never on controller or repository layer |
| Fetch strategy | All JPA relationships must declare explicit `FetchType`; no implicit lazy defaults (DC-08) |
| Immutability | DTOs and payload objects shall use Lombok `@Builder` and `@Value` where applicable |
| Secrets | Never hardcoded; always via `@Value("${ENV_VAR_NAME}")` with no default for security-critical values |

Frontend code (React 19.2 / JavaScript / TypeScript) shall conform to the ESLint configuration in `frontend/.eslintrc.js`.

### 4.4 Branching Strategy

The project uses **GitHub Flow** with a protected `main` branch:

```
main (protected)
│
├── develop (integration branch)
│     │
│     ├── feature/TASK-ID-short-description
│     ├── fix/DEF-ID-short-description
│     └── chore/short-description
│
└── release/vX.Y.Z (created from main at milestone gate)
```

| Branch | Protection Rules | Merge Requirement |
| :--- | :--- | :--- |
| `main` | Push restricted; require PR; require CI pass; require 1 reviewer | Technical Lead approval; all CI gates green |
| `develop` | Require PR; require CI pass | 1 peer reviewer; CI gates green |
| `feature/*`, `fix/*` | No direct push to main/develop | Must be merged via PR |
| `release/*` | Created at milestone gate; no new features | Hotfix only; merged back to main and develop |

### 4.5 Commit Message Convention

All commits shall follow the **Conventional Commits** specification (v1.0.0):

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

| Type | Usage |
| :--- | :--- |
| `feat` | New feature (FR-* requirement implementation) |
| `fix` | Bug or defect fix (DEF-* reference) |
| `test` | Test additions or corrections (TIR-* reference) |
| `refactor` | Code restructuring without behaviour change |
| `chore` | Build, dependency, or configuration change |
| `docs` | Documentation-only change |
| `ci` | CI/CD workflow change |
| `perf` | Performance improvement |
| `security` | Security-focused change (SEC-* reference) |

Example: `fix(auth): resolve NPE in AuthServiceImplTest by adding @Mock RoleRepository (DEF-001, TIR-02)`

### 4.6 Pull Request Process

1. **Create PR** from `feature/*` or `fix/*` into `develop`.
2. **PR description** must reference: task ID, SRS requirement IDs, test classes added/modified, RTM status update.
3. **CI pipeline** must pass all jobs: build, unit tests, integration tests, Checkstyle, JaCoCo gate, security scan.
4. **Review**: minimum 1 approved review; security-related changes require Technical Lead review.
5. **Merge**: squash-merge preferred for feature branches; merge commit for release branches.
6. **RTM update**: RTM must be updated in the same PR or in an immediately following commit.

---

## 5. Technical Processes

### 5.1 Requirements Management Process

Per ISO/IEC/IEEE 12207:2017 §6.4.2:

| Activity | Responsible | Artefact | Frequency |
| :--- | :--- | :--- | :--- |
| Requirements elicitation | Technical Lead + Stakeholders | SRS v4.0 | At project initiation; on scope change CR |
| Requirements analysis and classification | Technical Lead + QA Manager | SRS §3 (priority, phase) | At initiation; on CR |
| Requirements traceability maintenance | QA Engineer | RTM-BUILDNEST-001 | On every merged PR that touches an implementation or test |
| Requirements change control | Project Manager | CR + SRS version increment | On stakeholder change request |
| Requirements verification | QA Engineer | RTM Status column | At each milestone gate |

### 5.2 System Analysis and Design Process

Per ISO/IEC/IEEE 12207:2017 §6.4.5:

| Activity | Responsible | Artefact | Frequency |
| :--- | :--- | :--- | :--- |
| Architectural design maintenance | Technical Lead | SDD v3.0 | On design change CR |
| Detailed design (new features) | Developer + Technical Lead | SDD updated section | Before implementation begins |
| Design review | Technical Lead | Code review, architecture walkthrough | Per sprint; at M3 milestone |
| Design conformance check | QA Engineer | RTM SDD Reference column | At milestone gate |

**Design change procedure**: Any deviation from SDD v3.0 design elements requires a CR. The CR must identify the SDD section, the proposed change, the rationale, and the impact on the RTM. Design changes that affect SRS requirements must also update the SRS.

### 5.3 Implementation Process

Per ISO/IEC/IEEE 12207:2017 §6.4.6:

| Activity | Responsible | Tooling | Standard |
| :--- | :--- | :--- | :--- |
| Feature implementation | Developer | IntelliJ IDEA / VS Code, Java 21 | §4.3 Coding Standards |
| Unit test authoring | Developer | JUnit 5, Mockito | TP v4.0 §4.1 |
| Controller layer testing | Developer | `@WebMvcTest`, MockMvc | TP v4.0 §4.2 |
| Repository layer testing | Developer | `@DataJpaTest`, H2 | TP v4.0 §4.3 |
| Lombok usage | Developer | Lombok 1.18.x | `@Builder`, `@Getter`, `@Slf4j`, `@Value` |
| Liquibase changeset | Developer | Liquibase 4.x | MNT-04; `ddl-auto=validate` |
| Javadoc | Developer | Maven Javadoc Plugin | MNT-01 |
| Code review | Peer Developer | GitHub PR review | DoD §4.2 |

### 5.4 Integration Process

Per ISO/IEC/IEEE 12207:2017 §6.4.7:

| Activity | Responsible | Tooling | Standard |
| :--- | :--- | :--- | :--- |
| Component integration testing | QA Engineer | `@SpringBootTest`, H2, MockMvc | TP v4.0 §4.4 |
| Integration test execution | CI Pipeline | `./mvnw test -P all-tests` | 0 failures gate |
| Database integration (H2) | Developer | H2 in-memory, Liquibase | Test profile — H2 datasource |
| Redis integration (mocked) | Developer | Mockito `@MockBean CacheManager` | Unit tests only |
| Razorpay integration (mocked) | Developer | Mockito | `RazorpayClientAdapterTest` |
| E2E integration (staging only) | QA Engineer | RestAssured, `e2e-tests` profile | Running server required |

### 5.5 Verification Process

Per ISO/IEC/IEEE 12207:2017 §6.4.9:

| Verification Activity | Method | Tool | Pass Criterion |
| :--- | :--- | :--- | :--- |
| Static analysis | Inspection | Checkstyle, GitHub dependency-check workflow | Zero violations; CVSS < 7 |
| Unit test execution | Test | JUnit 5, `./mvnw test` | 0 failures, 0 errors |
| Integration test execution | Test | `./mvnw test -P all-tests` | 0 failures, 0 errors |
| Coverage measurement | Analysis | JaCoCo 0.8.11 | ≥ 40% line (M2); ≥ 70% line (M5) |
| Mutation testing | Analysis | PIT 1.15.x | ≥ 75% mutation score (M5) |
| Security scan | Inspection | OWASP Dependency Check (security workflow) | CVSS 7 threshold; zero critical |
| Performance validation | Analysis | Gatling, `LoadTestSimulation` | P95 ≤ 500 ms; 0 errors < 0.1% |
| Accessibility audit | Demonstration | axe-core, Playwright | 0 WCAG 2.1 AA violations |
| API contract | Test | SpringDoc OpenAPI + `./mvnw test` | All controller tests pass |
| RTM completeness | Inspection | RTM-BUILDNEST-001 | All Ph-1 rows = ✅ at M2; all Ph-2 rows = ✅ at M5 |

### 5.6 Validation Process

Per ISO/IEC/IEEE 12207:2017 §6.4.10:

| Validation Activity | Method | Environment | Pass Criterion |
| :--- | :--- | :--- | :--- |
| E2E test suite | Test | Staging — running server + MySQL + Redis | `./mvnw test -P e2e-tests` → 0 failures |
| User acceptance (backend API) | Demonstration | Staging | All SRS functional requirements demonstrated against staging API |
| Frontend UAT | Demonstration | Staging | All FR-FE-* scenarios completed by nominated test user |
| Payment flow validation | Test | Razorpay test-mode | FR-PAY-01 to FR-PAY-05 verified end-to-end in staging |
| Security validation | Test | Staging | OWASP ASVS 4.0 Level 2 checklist signed off |
| Performance validation | Analysis | Staging | Gatling report: P95 < 500 ms; error rate < 0.1% at 100 concurrent |
| Disaster recovery drill | Test | Staging | RTO ≤ 15 min; RPO ≤ 5 min (REL-04, REL-05) |

### 5.7 Infrastructure and Deployment Process

Per ISO/IEC/IEEE 12207:2017 §6.4.8:

#### 5.7.1 Local Development Environment

```bash
# Prerequisites: Java 21 JDK, Maven 3.9+, Docker

# Start infrastructure
cd backend/
cp .env.example .env          # populate environment variables
docker compose up -d mysql redis elasticsearch

# Start application
./mvnw spring-boot:run

# Run tests
./mvnw test                   # unit-tests profile (default)
./mvnw test -P all-tests      # integration tests
./mvnw verify                 # with JaCoCo report
```

#### 5.7.2 Environment Promotion Pipeline

```
Developer Workstation
     │ feature/* branch push
     ▼
CI Pipeline (GitHub Actions — ci.yml)
  ├── Build: ./mvnw clean package -DskipTests
  ├── Test: ./mvnw test (unit-tests profile)
  ├── Coverage: JaCoCo report + gate
  ├── Security: OWASP dependency-check (security.yml)
  └── Quality Gates: CI gate check
     │ All jobs green → PR approved → merge to develop
     ▼
Integration CI (develop branch)
  ├── All-tests profile: ./mvnw test -P all-tests
  └── Coverage verification: ./mvnw verify
     │ Merge to main (release)
     ▼
Deploy Pipeline (GitHub Actions — deploy.yml)
  ├── Docker image build: multi-stage Dockerfile
  ├── Image tag: git SHA + semantic version
  ├── Push to container registry
  ├── K8s deploy: kubectl apply (staging)
  ├── Smoke tests: E2E profile against staging
  └── Production deploy: manual approval gate
     ▼
Production
  ├── Blue-green deployment (Argo Rollouts — kubernetes/buildnest-rollout.yaml)
  ├── Health probe validation (/actuator/health)
  ├── Prometheus alert rules active (13 rules)
  └── Rollback: kubectl rollout undo (if health probe fails within 5 min)
```

#### 5.7.3 CI/CD Workflow Inventory

| Workflow File | Trigger | Purpose |
| :--- | :--- | :--- |
| `ci.yml` ("Quality Gate Pipeline", renamed #407) | Push / PR to `main`, `master`, `develop`; schedule (weekly Mon 02:00 UTC) | Primary CI: build, test, JaCoCo coverage, PR comment, failure issue creation |
| `ci-cd-pipeline.yml` ("Full Test Matrix & Docker Publish", renamed #407) | Push / PR to `main`, `master`, `develop` | Full test matrix (unit/integration/PIT/reliability/load/stress/e2e) + real Docker build/push |
| `security.yml` | Push / PR to `main`, `master`, `develop`; schedule (weekly Sun 00:00 UTC) | OWASP Dependency Check (CVSS 7 threshold), SARIF upload, code scanning |
| `performance.yml` | Manual trigger / schedule | Gatling load simulation |
| `deploy.yml` | After `ci.yml` ("Quality Gate Pipeline") success on `master` (staging); `v*` tag push (production); manual (`workflow_dispatch`) | Docker image build + push to GHCR, deploy via SSH + `docker compose` against `docker-compose.prod.yml` (#120, OPS-02, ADR 0003 — not K8s; no cluster exists) |

### 5.8 Maintenance Process

Per ISO/IEC/IEEE 12207:2017 §6.4.12:

| Activity | Trigger | Responsible | Process |
| :--- | :--- | :--- | :--- |
| Defect fix | Failed test, NCR, or user report | Developer | DEF-ID created → branch `fix/DEF-ID-*` → PR → DoD check → merge |
| Dependency update | Monthly review or CVE notification | Developer | Check for CVE; update `pom.xml`; run full test suite; security scan |
| JWT secret rotation | Every 90 days (SEC-12) | DevOps | `jwt.secret.previous` populated; new secret deployed; drain window; old secret cleared |
| DB password rotation | Every 180 days (SEC-13) | DevOps | K8s secret updated; HikariCP reconnects within `maxLifetime` window |
| Liquibase migration | Schema change required | Developer | New changeset; DB context tag; validate against H2 and MySQL |
| Performance review | Monthly or on P95 regression alert | DevOps + QA | Gatling run; compare to baseline; file CR if regression |

---

## 6. Supporting Processes

### 6.1 Documentation Management Process

Per ISO/IEC/IEEE 12207:2017 §6.3.2:

All SDLC documents are version-controlled in Git at `docs/SDLC-docs/`:

| Document | ID | Location | Owner | Update Trigger |
| :--- | :--- | :--- | :--- | :--- |
| Baseline Assessment | — | `docs/reports/baseline-assessment-2026-06-19.md` | Technical Lead | At project initiation; re-run at each milestone gate |
| Software Development Plan | SDP-BUILDNEST-001 | `docs/SDLC-docs/project-planning/software-development-plan.md` | Project Manager | On scope, schedule, or process change |
| Software Requirements Specification | SRS-BUILDNEST-001 | `docs/SDLC-docs/requirement-engineering/software-requirements-specification.md` | Technical Lead | On requirement change CR |
| Software Design Description | SDD-BUILDNEST-001 | `docs/SDLC-docs/design/software-design-description.md` | Technical Lead | On design change CR |
| Test Plan | TP-BUILDNEST-001 | `docs/SDLC-docs/software-testing/test-plan.md` | QA Manager | On scope or test strategy change |
| RTM | RTM-BUILDNEST-001 | `docs/SDLC-docs/requirement-engineering/requirements-traceability-matrix.md` | QA Engineer | On every PR affecting requirements, design, or tests |

**Document naming convention**: `{document-type-kebab-case}.md`
**Version control**: All documents use the same Git repository as the source code; changes committed with type `docs`.

### 6.2 Problem Resolution Process

Per ISO/IEC/IEEE 12207:2017 §6.3.8:

```
Problem Discovered
     │
     ▼
Issue Triage (within 24 hours)
  ├── P1 (Critical): Blocks Ph-1 exit or data loss — fix within 1 sprint
  ├── P2 (High): Fails CI gate — fix within current sprint
  ├── P3 (Medium): Known defect, workaround available — fix within 2 sprints
  └── P4 (Low): Minor; cosmetic; edge case — backlog; prioritised as capacity allows
     │
     ▼
DEF-ID assigned in defect register (RTM §11)
     │
     ▼
Branch created: fix/DEF-ID-short-description
     │
     ▼
Root cause analysis documented (in PR description)
     │
     ▼
Fix implemented; regression test added (new test covers the defect scenario)
     │
     ▼
PR created; CI gate must pass; 1 reviewer approves
     │
     ▼
RTM defect row removed; status updated to ✅ Implemented
     │
     ▼
Closed
```

### 6.3 Auditing Process

Per ISO/IEC/IEEE 12207:2017 §6.3.5:

| Audit Type | Frequency | Scope | Responsible |
| :--- | :--- | :--- | :--- |
| Code review (inline) | Every PR | Changed files | Peer developer |
| Architecture review | At each milestone gate | SDD conformance | Technical Lead |
| Security review | Monthly + at M5 | SEC-* requirements | Security reviewer + OWASP Dependency Check |
| Test coverage review | Weekly | JaCoCo trend | QA Manager |
| Documentation audit | At each milestone gate | All SDLC documents current and consistent | QA Manager |
| RTM completeness audit | At each milestone gate | All rows have correct status | QA Manager |
| Dependency vulnerability scan | Weekly (security.yml schedule) | `pom.xml` dependencies | CI pipeline (automated) |

---

## 7. Organisational Processes

### 7.1 Roles and Responsibilities

Per IEEE Std 1058:2016 §4.2:

| Role | Responsibilities | Skills Required |
| :--- | :--- | :--- |
| **Project Manager** | Project planning, schedule, budget, stakeholder communication, milestone gate sign-off, risk escalation, CR approval | Project management, risk management, stakeholder management |
| **Technical Lead** | Architecture decisions, design review, SDD maintenance, code review for security changes, milestone technical gate | Spring Boot, Java 21, system design, OWASP |
| **Senior Developer** | Feature implementation, unit/integration test authoring, code review | Java 21, Spring Boot 3.5, JUnit 5, Mockito, Liquibase |
| **QA Engineer (Backend)** | Test plan execution, defect management, RTM maintenance, JaCoCo/PIT configuration, integration test authoring | Testing, JUnit 5, RestAssured, JaCoCo, OWASP ASVS |
| **QA Engineer (Frontend)** | Frontend component tests, Playwright E2E, accessibility audit | React, Vitest, Playwright, axe-core |
| **DevOps Engineer** | CI/CD pipeline, Docker, Kubernetes, environment provisioning, secret rotation, monitoring | GitHub Actions, Kubernetes, Docker, Prometheus |
| **Security Reviewer** | OWASP ASVS Level 2 sign-off, CSP implementation, penetration test coordination | OWASP Top 10, ASVS, Spring Security, JWT |
| **Configuration Manager** | Branching strategy enforcement, baseline management, change control records | Git, GitHub, SCM processes |

### 7.2 Authority Matrix

| Decision | Project Manager | Technical Lead | QA Manager | DevOps |
| :--- | :--- | :--- | :--- | :--- |
| Milestone gate pass/fail | **Approve** | Recommend | Recommend | — |
| Architecture change (CR) | Approve | **Decide** | Consult | Inform |
| Requirement change (CR) | **Approve** | Recommend | Inform | — |
| Production deployment | **Approve** | — | — | **Execute** |
| Security incident response | Informed | **Lead** | Informed | Execute |
| Test strategy change | Informed | Consult | **Decide** | — |
| Dependency version update | Informed | **Approve** | Consult | — |
| CI pipeline change | Informed | Consult | Consult | **Decide** |

### 7.3 Stakeholder Communication

| Stakeholder | Communication Channel | Frequency | Content |
| :--- | :--- | :--- | :--- |
| Project team | Sprint planning / daily standup | Daily | Task status, blockers, CI gate state |
| Project Manager | Sprint review | End of each 2-week sprint | Progress vs plan, milestone tracking, risk status |
| Technical Lead | Architecture review | At milestone gates | SDD conformance, design decisions |
| QA Manager | Test status meeting | Weekly | JaCoCo trend, defect count, RTM status |
| Security Reviewer | Security review | Monthly | SEC-* requirement status, OWASP scan results |
| Stakeholders | Milestone gate report | At each milestone | Gate criteria assessment, deliverables produced |

---

## 8. Project Schedule

### 8.1 Work Breakdown Structure (WBS)

```
BuildNest SDP v1.0
│
├── 1.0 Discovery (COMPLETE)
│    ├── 1.1 Baseline Assessment
│    ├── 1.2 SRS v4.0
│    ├── 1.3 SDD v3.0
│    ├── 1.4 Test Plan v4.0
│    ├── 1.5 RTM v1.0
│    └── 1.6 SDP v1.0 (this document)
│
├── 2.0 Phase 2 — Remediation (CURRENT)
│    ├── 2.1 Milestone 1: Stabilization
│    │    ├── 2.1.1 Fix DEF-001 (AuthServiceImplTest @Mock)
│    │    ├── 2.1.2 Fix DEF-002 / DEF-003 (E2E tag isolation)
│    │    ├── 2.1.3 Fix DEF-004 (403 vs 401 assertion)
│    │    ├── 2.1.4 Fix DEF-005 / DEF-006 (400/415 vs 401)
│    │    └── 2.1.5 M1 gate verification (./mvnw test → 0 failures)
│    │
│    ├── 2.2 Milestone 2: Quality Foundation
│    │    ├── 2.2.1 JaCoCo gap analysis (identify uncovered services)
│    │    ├── 2.2.2 Increase test coverage to ≥ 50% LINE
│    │    ├── 2.2.3 Raise JaCoCo gate in pom.xml to 0.50
│    │    ├── 2.2.4 Configure PIT plugin in pom.xml
│    │    ├── 2.2.5 Integration test profile verification
│    │    └── 2.2.6 M2 gate verification
│    │
│    └── 2.3 Milestone 3: Technical Debt Reduction
│         ├── 2.3.1 Fix DC-08 (explicit FetchType on Category, Order entities)
│         ├── 2.3.2 Fix SAF-03 (guard Optional.get() in PasswordResetServiceImpl)
│         ├── 2.3.3 Performance baseline (Gatling against local stack)
│         ├── 2.3.4 SEC-12 JWT rotation runbook documented
│         └── 2.3.5 M3 gate verification
│
├── 3.0 Phase 3 — Deployment and Readiness
│    ├── 3.1 Milestone 4: Feature Development
│    │    ├── 3.1.1 Payment full flow (FR-PAY-01 to FR-PAY-05)
│    │    ├── 3.1.2 Admin suite completion (FR-ADM-01 to FR-ADM-07)
│    │    ├── 3.1.3 Frontend SPA (FR-FE-01 to FR-FE-30)
│    │    ├── 3.1.4 Monitoring full suite (FR-MON-02 to FR-MON-08)
│    │    ├── 3.1.5 Frontend component tests (Vitest)
│    │    ├── 3.1.6 Frontend E2E (Playwright)
│    │    └── 3.1.7 M4 gate verification
│    │
│    └── 3.2 Milestone 5: Production Readiness
│         ├── 3.2.1 JaCoCo gate raised to 0.70
│         ├── 3.2.2 PIT mutation score ≥ 75%
│         ├── 3.2.3 CSP unsafe-inline removed (SEC-14)
│         ├── 3.2.4 Performance SLO validation (Gatling — staging)
│         ├── 3.2.5 Security OWASP ASVS Level 2 sign-off
│         ├── 3.2.6 Accessibility audit (axe-core, WCAG 2.1 AA)
│         ├── 3.2.7 Kubernetes staging validation
│         ├── 3.2.8 DR drill (RTO ≤ 15 min; RPO ≤ 5 min)
│         ├── 3.2.9 E2E suite on staging (0 failures)
│         └── 3.2.10 M5 gate verification
```

### 8.2 Milestone Schedule

> **Note**: Absolute dates are assigned at project initiation based on available team capacity. The relative durations below are based on estimated effort from TP v4.0 §13 and RTM §11 open defect analysis.

| Milestone | Relative Start | Duration | Estimated Effort |
| :--- | :--- | :--- | :--- |
| M1 — Stabilization | Sprint 1, Day 1 | 3–5 hours | ~5 hours (6 targeted defects per RTM §11) |
| M2 — Quality Foundation | Sprint 1, Day 3 | 1 sprint | ~25 hours (coverage gap analysis + new tests) |
| M3 — Technical Debt Reduction | Sprint 2 | 1 sprint | ~15 hours (DC-08, SAF-03, performance baseline) |
| M4 — Feature Development | Sprints 3–8 | 6 sprints | ~120 hours (frontend + payment + admin + monitoring) |
| M5 — Production Readiness | Sprints 9–10 | 2 sprints | ~50 hours (coverage gate, PIT, security, perf, DR) |

### 8.3 Critical Path

```
DEF-001 fix → DEF-002/003 fix → DEF-004/005/006 fix
     │                                              │
     └──────────────────────────────────────────────►M1 Gate
                                                    │
                                          Coverage gap analysis
                                                    │
                                          New unit tests (coverage)
                                                    │
                                          JaCoCo gate 50%
                                                    │
                                                   M2 Gate
                                                    │
                                         DC-08 + SAF-03 + perf baseline
                                                    │
                                                   M3 Gate
                                                    │
                             ┌──────────────────────┴──────────────────────┐
                             │                                             │
                    Frontend development                       Payment + Admin
                             │                                             │
                             └──────────────────────┬──────────────────────┘
                                                    │
                                                   M4 Gate
                                                    │
                           ┌────────────────────────┴────────────────────────┐
                           │                         │                       │
                   Coverage to 70%            Security sign-off       DR drill
                           │                         │                       │
                           └────────────────────────┬────────────────────────┘
                                                    │
                                                   M5 Gate (Ph-2 exit)
```

---

## 9. Resource Plan

### 9.1 Human Resources

| Role | Sprint 1 (M1-M2) | Sprint 2 (M3) | Sprints 3–8 (M4) | Sprints 9–10 (M5) |
| :--- | :--- | :--- | :--- | :--- |
| Technical Lead | 25% | 25% | 30% | 40% |
| Senior Developer | 80% | 80% | 80% | 60% |
| QA Engineer (Backend) | 50% | 60% | 40% | 60% |
| QA Engineer (Frontend) | 0% | 10% | 80% | 60% |
| DevOps Engineer | 10% | 20% | 20% | 60% |
| Security Reviewer | 0% | 10% | 10% | 30% |
| Project Manager | 20% | 20% | 20% | 20% |

### 9.2 Infrastructure Resources

| Resource | Environment | Purpose | Phase |
| :--- | :--- | :--- | :--- |
| Developer workstation (Java 21, Docker) | Local | Development and local testing | All |
| CI runner (GitHub Actions — `ubuntu-latest`) | CI | Build, test, scan on every push | All |
| MySQL 8.2 (Docker Compose) | Local + CI | Integration tests (H2 in CI; MySQL in staging) | All |
| Redis 7 (Docker Compose) | Local + staging | Cache and rate limiting | All |
| Elasticsearch 8.17 (Docker Compose) | Local + staging (optional) | Analytics, audit log | Ph-2 |
| Kubernetes cluster (staging) | Staging | E2E, performance, security, UAT validation | Ph-2 |
| Kubernetes cluster (production) | Production | Live system | Ph-2 |
| Prometheus + Grafana | Staging + production | Metrics and alerting | Ph-2 |
| GitHub Actions minutes | CI/CD | Workflow execution | All |
| Container registry | CI/CD | Docker image storage and promotion | Ph-2 |

### 9.3 Tooling Resources

| Tool | Version | Purpose | License |
| :--- | :--- | :--- | :--- |
| IntelliJ IDEA | 2024+ | Backend development | Commercial / Community |
| VS Code | Latest | Frontend and documentation | MIT |
| GitHub | — | Source control, CI/CD, PR reviews | Commercial (GitHub Actions) |
| JaCoCo | 0.8.11 | Coverage measurement | EPL 2.0 |
| PIT (Pitest) | 1.15.x | Mutation testing | Apache 2.0 |
| Gatling | 3.x | Load and performance testing | Apache 2.0 |
| OWASP Dependency Check | Latest | CVE scanning | Apache 2.0 |
| Docker | 24+ | Containerisation | Apache 2.0 |
| Kubernetes | 1.28+ | Container orchestration | Apache 2.0 |
| Terraform | 1.x | Infrastructure as code | BUSL 1.1 |
| Playwright | Latest | Frontend E2E | Apache 2.0 |
| axe-core | Latest | Accessibility auditing | MPL 2.0 |

---

## 10. Risk Management Plan

Per ISO/IEC/IEEE 12207:2017 §6.3.4 and ISO 31000:2018:

### 10.1 Risk Classification

| Likelihood | Low (< 20%) | Medium (20–60%) | High (> 60%) |
| :--- | :--- | :--- | :--- |
| **Critical Impact** | Medium Risk | High Risk | Critical Risk |
| **High Impact** | Low Risk | Medium Risk | High Risk |
| **Low Impact** | Accepted | Low Risk | Low Risk |

### 10.2 Risk Register

| Risk ID | Risk Description | Likelihood | Impact | Rating | Mitigation | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| RISK-01 | JaCoCo 70% gate not achievable within M5 schedule | Medium | High | **High** | Start gap analysis at M2; add targeted tests incrementally; raise gate in stages (50% → 70%); track weekly coverage trend | QA Manager |
| RISK-02 | H2 / MySQL Liquibase changeset incompatibility introduces test failures | Medium | High | **High** | Use `dbms="mysql"` context tags on MySQL-only DDL; validate all new changesets against H2 in CI before merging | Developer |
| RISK-03 | Staging Kubernetes cluster unavailable for E2E or performance validation | Medium | High | **High** | Maintain Docker Compose as fallback staging environment; E2E tests can run against local `spring-boot:run` | DevOps |
| RISK-04 | Razorpay API contract changes breaking payment integration | Low | Critical | **Medium** | Pin Razorpay SDK version in `pom.xml`; monitor Razorpay changelog; integration tested only in staging with test-mode keys | Technical Lead |
| RISK-05 | PIT mutation testing too slow for sprint velocity | High | Low | **Low** | Run PIT only on `coverage` profile; exclude Lombok and config classes; scope to `service.*` and `security.*`; parallelise mutation workers | QA Engineer |
| RISK-06 | Frontend development scope underestimated (30 FR-FE requirements) | High | High | **Critical** | Decompose FR-FE into vertical slices; deliver incrementally; prioritise core shopping journey (FR-FE-11 to FR-FE-17) first; deferrable: FR-FE-20, FR-FE-27, FR-FE-29 | Project Manager |
| RISK-07 | Security test divergence: TestSecurityConfig masks production security issues | Low | Critical | **Medium** | Periodic synchronised review of `TestSecurityConfig` vs `SecurityConfig` on every security-related PR; staging E2E tests against real security config | Technical Lead |
| RISK-08 | JWT secret committed to source by mistake | Low | Critical | **Medium** | Pre-commit hook (`git-secrets`); `.gitignore` covers `.env` and `*.key`; CI security workflow scans for secrets in PRs | Configuration Manager |
| RISK-09 | Production deployment rollout failure with active users | Low | Critical | **Medium** | Blue-green deployment via Argo Rollouts; automatic rollback if readiness probe fails within 5 min; gradual canary traffic shift | DevOps |
| RISK-10 | Dependency with critical CVE discovered post-deployment | Medium | High | **High** | Weekly OWASP Dependency Check (security.yml scheduled); CVSS 7 gate blocks CI; patch SLA: P1 = 24 h, P2 = 1 week | Technical Lead |
| RISK-11 | Schedule slip on M4 due to frontend + payment + admin parallelism | High | High | **Critical** | Phase M4 into frontend-first (Sprints 3–5) then payment+admin (Sprints 6–7) then integration (Sprint 8); hold Sprint 9 as buffer | Project Manager |
| RISK-12 | DR drill fails to meet RTO/RPO targets (REL-04, REL-05) | Medium | High | **High** | Conduct DR drill at M3 against local environment; identify gaps; address before M5 staging DR drill | DevOps + Technical Lead |

### 10.3 Risk Response Procedures

| Rating | Response Time | Escalation | Action |
| :--- | :--- | :--- | :--- |
| Critical | Immediate (same sprint day) | Project Manager + Technical Lead | Create RISK-CR; suspend affected sprint work; convene emergency review |
| High | Within 2 sprint days | Technical Lead | File issue; update risk register; revise affected tasks |
| Medium | Within current sprint | Owner | Update mitigation action; monitor weekly |
| Low | Next sprint review | Owner | Monitor; revisit at next milestone gate |

---

## 11. Quality Assurance Plan

Per ISO/IEC/IEEE 12207:2017 §6.2.5:

### 11.1 Quality Objectives

| Objective | Metric | Phase 1 Target | Phase 2 Target | Measurement |
| :--- | :--- | :--- | :--- | :--- |
| Test suite integrity | Failing tests | 0 failures, 0 errors | 0 failures, 0 errors | `./mvnw test` |
| Code coverage (line) | JaCoCo LINE | ≥ 40% (maintain) | **≥ 70%** | `./mvnw verify` |
| Mutation effectiveness | PIT score | Not yet active | **≥ 75%** | `./mvnw pitest:mutationCoverage` |
| Build reproducibility | Compilation | 0 errors | 0 errors | `./mvnw clean compile` |
| Dependency safety | CVE score | CVSS < 7 | CVSS < 7 | OWASP Dependency Check |
| Security compliance | OWASP ASVS | Level 1 | Level 2 | Security review |
| API backward compatibility | V1 response shape | Unchanged | Unchanged | `ProductControllerV1Test` |
| Logging quality | SLF4J compliance | 100% | 100% | `LoggingStandardsTest` |
| Javadoc coverage | Public members | 100% | 100% | Maven Javadoc Plugin |
| Performance (P95) | API response time | Baseline recorded | P95 < 500 ms | Gatling |

### 11.2 Quality Activities by Sprint

| Activity | Frequency | Responsible | Artefact Produced |
| :--- | :--- | :--- | :--- |
| Code review (peer) | Every PR | Peer Developer | PR approval record |
| CI gate review | Every PR | CI pipeline | CI pipeline report |
| JaCoCo trend review | Weekly | QA Manager | Coverage trend chart |
| Defect triage | Weekly | QA Manager | RTM §11 updated |
| Security scan review | Weekly (automated) + monthly (manual) | Security Reviewer | Dependency check report |
| RTM completeness check | At milestone gate | QA Manager | RTM audit report |
| Architecture conformance review | At milestone gate | Technical Lead | SDD conformance report |
| Documentation currency check | At milestone gate | QA Manager | Document version audit |

### 11.3 Non-Conformance Management

A **Non-Conformance Report (NCR)** is raised when:
- A CI gate fails and the failure is not immediately resolved within the same sprint day
- A quality objective regresses (e.g., JaCoCo drops below gate)
- A security scan identifies a CVSS ≥ 7 vulnerability
- A milestone gate cannot be declared due to unresolved criteria

NCR process: Raise NCR → assign owner → root cause analysis within 24 h → CAPA defined → CAPA verified → NCR closed.

---

## 12. Configuration Management Plan

Per ISO/IEC/IEEE 12207:2017 §6.3.6:

### 12.1 Configuration Items

| Configuration Item | Type | Version Control | Baseline Point |
| :--- | :--- | :--- | :--- |
| Source code (`backend/src/`, `frontend/src/`) | Software | Git | Each PR merge to `main` |
| Build files (`pom.xml`, `package.json`, `vite.config.js`) | Build | Git | Each dependency change |
| Maven Wrapper (`mvnw`, `.mvn/`) | Build | Git | Do not modify without Technical Lead approval |
| Liquibase changelogs (`db/changelog/`) | Database schema | Git | On schema change; never modified after merge |
| CI/CD workflows (`.github/workflows/*.yml`) | Infrastructure | Git | On workflow change CR |
| Kubernetes manifests (`kubernetes/*.yaml`) | Infrastructure | Git | On deployment change CR |
| Terraform files (`terraform/`) | Infrastructure | Git | On IaC change |
| SDLC documents (`docs/SDLC-docs/`) | Documentation | Git | On document version increment |
| Environment configuration (`.env.example`) | Configuration | Git | On new env var addition |
| Secrets (`.env`) | Configuration | **Not in Git** | Per §12.2 |

### 12.2 Secret Management

| Secret | Storage | Rotation | Access |
| :--- | :--- | :--- | :--- |
| `JWT_SECRET` | Kubernetes Secret / GitHub Actions Secret | Every 90 days (SEC-12) | DevOps only |
| `SPRING_DATASOURCE_PASSWORD` | Kubernetes Secret | Every 180 days (SEC-13) | DevOps only |
| `REDIS_PASSWORD` | Kubernetes Secret | Every 180 days | DevOps only |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` | Kubernetes Secret | On Razorpay key rotation | DevOps only |
| CI secrets (`REGISTRY_USERNAME`, `REGISTRY_PASSWORD`) | GitHub Actions Secrets | On rotation | DevOps only |

**Rule**: No secret, credential, key, or password may ever appear in source code, commit history, PR body, issue comment, or log output. Violation triggers immediate RISK-08 response.

### 12.3 Baseline Management

| Baseline | Trigger | Controlled By |
| :--- | :--- | :--- |
| Discovery Baseline | Completion of SRS, SDD, TP, RTM, SDP | Project Manager |
| M1 Baseline | M1 gate passed (0 test failures) | Technical Lead |
| M2 Baseline | M2 gate passed (JaCoCo ≥ 50%) | Technical Lead |
| M3 Baseline | M3 gate passed (tech debt resolved) | Technical Lead |
| M4 Baseline | M4 gate passed (features complete) | Project Manager |
| Release Baseline (Ph-2) | M5 gate passed (production ready) | Project Manager |

Each baseline is tagged in Git: `baseline/m1-YYYY-MM-DD`, `baseline/m2-YYYY-MM-DD`, etc.

### 12.4 Change Control

All changes to baselined artefacts (source code on `main`, SDLC documents) follow:

1. CR raised (GitHub Issue or formal CR document)
2. Impact assessment (scope, schedule, quality, risk)
3. Approval (per §7.2 Authority Matrix)
4. Implementation (via PR)
5. Verification (DoD check)
6. Closure (CR closed; changelog updated)

---

## 13. Communication and Coordination Plan

Per IEEE Std 1058:2016 §4.4:

### 13.1 Meeting Schedule

| Meeting | Participants | Frequency | Duration | Format |
| :--- | :--- | :--- | :--- | :--- |
| Daily Standup | Development team | Daily | 15 min | Async (written) or synchronous |
| Sprint Planning | Full team | Sprint start (every 2 weeks) | 60 min | Synchronous |
| Sprint Review | Full team + stakeholders | Sprint end | 45 min | Synchronous; demo of increment |
| Sprint Retrospective | Development team | Sprint end | 30 min | Synchronous |
| Milestone Gate Review | Full team + Project Manager | At each milestone | 90 min | Synchronous; formal gate sign-off |
| Architecture Review | Technical Lead + Developers | Monthly | 60 min | Synchronous |
| Security Review | Technical Lead + Security Reviewer | Monthly | 45 min | Synchronous |
| Risk Review | Project Manager + Technical Lead | Monthly | 30 min | Synchronous |

### 13.2 Communication Artefacts

| Artefact | Format | Location | Update Frequency |
| :--- | :--- | :--- | :--- |
| Sprint board | GitHub Projects | `github.com/{repo}/projects` | Daily |
| CI pipeline status | GitHub Actions | `github.com/{repo}/actions` | On every push |
| Coverage trend | JaCoCo HTML | `target/site/jacoco/index.html` | On every `./mvnw verify` |
| Defect register | RTM §11 | RTM-BUILDNEST-001 | On defect discovery/closure |
| Risk register | This document §10.2 | SDP §10.2 | Monthly or on risk event |
| Milestone gate report | Markdown | `docs/SDLC-docs/project-planning/milestone-reports/` | At each milestone |
| PR review comments | GitHub PR | GitHub PR thread | On PR creation |

### 13.3 Escalation Path

```
Technical Issue Identified
     │
     ▼
Developer attempts resolution (within 1 sprint day)
     │ unresolved
     ▼
Technical Lead consulted (within 1 sprint day)
     │ unresolved or design-impacting
     ▼
Architecture review called (within 3 sprint days)
     │ schedule / resource impact
     ▼
Project Manager notified (within same sprint)
     │ risk escalation required
     ▼
Stakeholder escalation (within 2 business days)
```

---

## 14. Measurement and Metrics Plan

Per ISO/IEC/IEEE 15939:2017 — Software Measurement:

### 14.1 Process Metrics

| Metric | Collection Method | Frequency | Target | Action if Breached |
| :--- | :--- | :--- | :--- | :--- |
| CI pipeline pass rate | GitHub Actions | Per run | 100% on `main`; ≥ 95% on PRs | Investigate and fix within 24 h |
| Sprint velocity (story points) | GitHub Projects | Per sprint | Stable ± 20% | Retrospective; adjust capacity |
| Defect open count | RTM §11 | Weekly | ≤ 0 blocking phase gate | Escalate; re-prioritise sprint |
| Mean time to fix (MTTF) defect | DEF timestamp tracking | Weekly | P1 ≤ 24 h; P2 ≤ 1 sprint | Root cause review |
| PR cycle time | GitHub PR creation to merge | Weekly | ≤ 2 sprint days | Review process; identify blockers |
| Code review approval rate | GitHub PR metrics | Weekly | 100% PRs reviewed | Enforce DoD check |

### 14.2 Product Quality Metrics

| Metric | Tool | Frequency | Ph-1 Target | Ph-2 Target |
| :--- | :--- | :--- | :--- | :--- |
| JaCoCo line coverage | JaCoCo 0.8.11 | Every `./mvnw verify` | ≥ 40% | ≥ 70% (actual enforced gate: 85% PACKAGE/INSTRUCTION, verified 2026-07-17, #461 — already exceeds both targets) |
| JaCoCo branch coverage | JaCoCo 0.8.11 | Every `./mvnw verify` | Reported | ≥ 60% |
| PIT mutation score | PIT 1.15.x | Weekly (after M2) | Active at 77% (verified 2026-07-17, #461) | ≥ 75% — already exceeded, ratcheting to 79% per M4 milestone |
| Unit test count | JUnit `./mvnw test` | Per run | ≥ 1,735 (verified 2026-07-17, #461) | Growing |
| Test pass rate | JUnit | Per run | 100% | 100% |
| Static analysis violations | Checkstyle | Per run | 0 | 0 |
| Dependency CVEs (CVSS ≥ 7) | OWASP Dependency Check | Weekly | 0 | 0 |
| API P95 response time | Gatling | Monthly | Baseline recorded | < 500 ms |
| WCAG AA violations | axe-core | On frontend PRs | — | 0 |

### 14.3 Metrics Dashboard

All metrics are made available via:

1. **GitHub Actions Summary**: CI pass/fail, JaCoCo coverage comment on PR, test result summary
2. **JaCoCo HTML Report**: `./mvnw verify` → `target/site/jacoco/index.html`
3. **Prometheus + Grafana** (staging / production): Application metrics, circuit breaker state, request rate
4. **GitHub Projects**: Sprint board, defect count, milestone progress

---

## 15. Training Plan

### 15.1 Onboarding Requirements

New team members joining the BuildNest project must complete the following before committing code:

| Topic | Materials | Duration | Gate |
| :--- | :--- | :--- | :--- |
| Project overview and architecture | SDD v3.0 §4.1–§4.4 | 2 hours | Architecture quiz (informal) |
| Development conventions | §4.3–§4.6 of this SDP | 1 hour | Code review standards read |
| Security requirements | SRS v4.0 §3.8.3 (SEC-01 to SEC-14); OWASP Top 10 2021 | 2 hours | Security checklist sign-off |
| Test standards | TP v4.0 §3–§4; TIR-01 to TIR-05 | 1 hour | Test standards quiz (informal) |
| Git and PR process | §4.4–§4.6 of this SDP | 1 hour | Shadow first PR with reviewer |
| Local environment setup | `backend/.env.example`, `docker compose up -d` | 2 hours | Successful local `./mvnw test` run |

### 15.2 Skill Development

| Skill Area | Recommended for | Resources |
| :--- | :--- | :--- |
| Spring Boot 3.5 / Java 21 | All developers | Spring Boot Reference 3.5.10; Baeldung Spring Security series |
| Resilience4j | Backend developers | Resilience4j docs; `ResilienceConfig.java` + `ReliabilityTest.java` |
| React 19 / Vite 8 | Frontend developer | React 19 official docs; Vite docs |
| JUnit 5 + Mockito | All developers | JUnit 5 User Guide; Mockito docs; existing test classes as reference |
| Kubernetes | DevOps | Kubernetes official docs; `kubernetes/` manifests |
| OWASP ASVS Level 2 | Security Reviewer + Technical Lead | OWASP ASVS 4.0 PDF |
| Gatling | QA Engineer (Backend) | Gatling docs; `LoadTestSimulation.java` |

---

## 16. Supplier and Dependency Management

### 16.1 Third-Party Dependency Policy

| Policy | Rule |
| :--- | :--- |
| Version pinning | All dependencies pinned in `pom.xml`; no open ranges (`+` or `LATEST`) |
| CVE monitoring | Weekly OWASP Dependency Check (security.yml); CVSS ≥ 7 blocks CI |
| License compliance | All dependencies reviewed against LIC-01 to LIC-10 (SRS §3.10) |
| Update approval | Patch versions: developer decision; minor/major: Technical Lead CR approval |
| Supply chain | Maven Central only; no unapproved private repositories |

### 16.2 Key External Dependencies

| Dependency | Version | Supplier | Failure Mode | SRS |
| :--- | :--- | :--- | :--- | :--- |
| MySQL 8.2 | 8.2 | Oracle | Critical — DB unavailable | DEP-01 |
| Redis 7 | 7 | Redis Ltd | High — cache and rate limit offline | DEP-02 |
| Razorpay Java SDK | 1.4.5 | Razorpay | High — payments offline | DEP-03 |
| Elasticsearch 8.17 | 8.17 | Elastic | Low — optional feature | DEP-04 |
| Spring Boot | 3.5.10 | VMware (Broadcom) | Critical | CON-02 |
| JJWT | 0.12.3 | jwtk | Critical — auth offline | SEC-02 |
| Bucket4j | 8.1.0 | Vladimir Bukhtoyarov | High — rate limiting offline | SEC-07 |
| Resilience4j | 2.1.0 | Michael Nygard | High — resilience patterns inactive | REL-02 |
| JaCoCo | 0.8.11 | EclEmma | Medium — coverage gate inactive | MNT-02 |

### 16.3 End-of-Life and Upgrade Tracking

| Dependency | Current EOL/Support End | Action Required |
| :--- | :--- | :--- |
| Java 21 LTS | September 2029 | No action required |
| Spring Boot 3.5.x | November 2027 | Review Spring Boot 3.6+ release notes at M5 |
| MySQL 8.2 | Covered by MySQL 8.0 LTS EOL 2026-04 | Evaluate MySQL 8.4 LTS upgrade at M5 |
| Elasticsearch 8.17 | Supported (8.17.x maintained) | Resolved — verified 2026-07-17 (#461); `docker-compose.yml`'s active service already runs 8.17.6 |

> **Elasticsearch EOL Notice — Resolved**: Elasticsearch was previously recorded at version 8.10 (EOL October 2024). Verified 2026-07-17 (#461) that `docker-compose.yml`'s active Elasticsearch service already runs **8.17.6** — the upgrade this risk called for has already happened; no outstanding action remains before the Ph-2 gate.

---

## 17. Standard Alignment Matrix

Per IEEE Std 1058:2016 §4 clause alignment:

| IEEE 1058:2016 Clause | SDP Section |
| :--- | :--- |
| §4.1 — Overview / Purpose / Scope | §1.1, §1.2 |
| §4.2 — Project organisation (staffing, roles) | §7.1, §7.2 |
| §4.3 — Managerial process (objectives, assumptions) | §2, §3.1 |
| §4.4 — Technical process (tools, methods, infrastructure) | §5 |
| §4.5 — Work packages / WBS | §8.1 |
| §4.6 — Schedule | §8.2, §8.3 |
| §4.7 — Resource requirements | §9 |
| §4.8 — Budget (not applicable — internal project) | N/A |
| §4.9 — Training requirements | §15 |
| §4.10 — Documentation plan | §6.1 |
| §4.11 — Configuration management | §12 |
| §4.12 — Quality assurance | §11 |
| §4.13 — Metrics plan | §14 |
| §4.14 — Risk management | §10 |
| §4.15 — Problem resolution | §6.2 |
| §4.16 — Supplier management | §16 |

| ISO/IEC/IEEE 12207:2017 Process | SDP Section |
| :--- | :--- |
| §6.2.5 — Quality Management | §11 |
| §6.3.2 — Documentation Management | §6.1 |
| §6.3.4 — Risk Management | §10 |
| §6.3.5 — Configuration Management | §12 |
| §6.3.6 — Measurement | §14 |
| §6.3.7 — Decision Management | §7.2 |
| §6.3.8 — Problem Resolution | §6.2 |
| §6.4.2 — Stakeholder Requirements Definition | §5.1 |
| §6.4.5 — Architecture Definition | §5.2 |
| §6.4.6 — Implementation | §5.3 |
| §6.4.7 — Integration | §5.4 |
| §6.4.8 — Deployment | §5.7 |
| §6.4.9 — Verification | §5.5 |
| §6.4.10 — Validation | §5.6 |
| §6.4.12 — Maintenance | §5.8 |

---

## 18. Appendices

### Appendix A: Phase 1 Immediate Action Checklist

The following actions are required to achieve the M1 milestone gate. Each can be completed within 1 sprint day from the date of this SDP.

| Priority | Task | File to Modify | Expected Outcome | DEF/TIR |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Add `@Mock RoleRepository roleRepository` to `AuthServiceImplTest`; add `when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole))` in `@BeforeEach` | `AuthServiceImplTest.java` | 3 errors resolved | DEF-001, TIR-02 |
| 2 | Add `@Tag("e2e")` annotation to `ProductApiTest` class | `ProductApiTest.java` | 4 failures resolved in unit-tests profile | DEF-002, TIR-01 |
| 3 | Add `@Tag("e2e")` annotation to `OrderApiTest` class | `OrderApiTest.java` | E2E test isolated | DEF-003, TIR-01 |
| 4 | Change `equalTo(401)` to `equalTo(403)` in `testRoleHierarchyEnforcement()` | `AuthenticationAuthorizationSecurityTest.java:246` | 1 failure resolved | DEF-004, TIR-03 |
| 5 | Update `testXSSPrevention()` assertion to accept 400 or 401 | `InputValidationSecurityTest.java:168` | 1 failure resolved | DEF-005, TIR-04 |
| 6 | Update `testFileUploadValidation()` assertion to accept 415 or 401 | `InputValidationSecurityTest.java:303` | 1 failure resolved | DEF-006, TIR-04 |
| 7 | Verify: `./mvnw test` → 0 failures, 0 errors | CI + local | M1 gate passed | All TIR |

### Appendix B: Environment Variables Quick Reference

| Variable | Required | Secret | Description |
| :--- | :--- | :--- | :--- |
| `JWT_SECRET` | Yes | Yes | HMAC-SHA512 key; minimum 512 bits; no default |
| `JWT_EXPIRATION` | No | No | Access token TTL in ms; default 900,000 (15 min) |
| `JWT_REFRESH_EXPIRATION` | No | No | Refresh token TTL in ms; default 2,592,000,000 (30 days) |
| `SPRING_DATASOURCE_URL` | Yes | No | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | No | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Yes | MySQL password |
| `REDIS_HOST` | No | No | Redis host; default `localhost` |
| `REDIS_PORT` | No | No | Redis port; default 6379 |
| `REDIS_PASSWORD` | No | Yes | Redis auth password; empty = no auth |
| `ELASTICSEARCH_ENABLED` | No | No | Enable ES features; default `false` |
| `ELASTICSEARCH_HOST` | No | No | ES host; default `localhost` |
| `RAZORPAY_KEY_ID` | No (required Ph-2) | No | Razorpay API key ID |
| `RAZORPAY_KEY_SECRET` | No (required Ph-2) | Yes | Razorpay API secret |
| `SERVER_SSL_ENABLED` | No (required in prod) | No | Enable HTTPS; default `false` |
| `CHAOS_ENABLED` | No | No | Enable chaos fault injection; default `false` |

### Appendix C: Milestone Gate Checklist Template

For use at each milestone gate review:

```markdown
# Milestone [N] — [Name] Gate Review

**Date**: YYYY-MM-DD
**Project**: BuildNest
**Reviewer(s)**:

## Criteria Assessment

| Criterion | Evidence | Pass/Fail |
| :--- | :--- | :--- |
| [criterion 1] | [link/artefact] | ✅ / ❌ |
| ...           | ...             | ...       |

## CI Pipeline State

- CI workflow: [ ] Pass / [ ] Fail
- Last run: [GitHub Actions link]
- JaCoCo coverage: [%]

## Open Issues

| Issue | Severity | Owner | ETA |
| :--- | :--- | :--- | :--- |

## Gate Decision

[ ] PASS — All criteria met; milestone declared complete
[ ] CONDITIONAL PASS — Minor items outstanding; agreed to resolve by [date]
[ ] FAIL — [reason]; [blocking criterion] not met; re-review by [date]

**Signed**: Project Manager: _____________ Technical Lead: _____________
```

### Appendix D: Document Suite Summary

| Document | ID | Version | Status | Location |
| :--- | :--- | :--- | :--- | :--- |
| Baseline Assessment Report | — | 2026-06-19 | Approved | `docs/reports/baseline-assessment-2026-06-19.md` |
| Software Requirements Specification | SRS-BUILDNEST-001 | 5.8 | Under Review | `docs/SDLC-docs/requirement-engineering/software-requirements-specification.md` |
| Software Design Description | SDD-BUILDNEST-001 | 4.13 | Under Review | `docs/SDLC-docs/design/software-design-description.md` |
| Test Plan | TP-BUILDNEST-001 | 4.5 | Under Review | `docs/SDLC-docs/software-testing/test-plan.md` |
| Requirements Traceability Matrix | RTM-BUILDNEST-001 | 1.43 | Under Review | `docs/SDLC-docs/requirement-engineering/requirements-traceability-matrix.md` |
| **Software Development Plan** | **SDP-BUILDNEST-001** | **1.3** | **Under Review** | **`docs/SDLC-docs/project-planning/software-development-plan.md`** |

---

**— End of Document —**

*This Software Development Plan was prepared in conformance with ISO/IEC/IEEE 12207:2017, IEEE Std 1058:2016, and ISO/IEC/IEEE 15288:2023 for the BuildNest E-Commerce Platform. All schedule estimates, risk assessments, resource plans, and quality targets are evidence-based and traceable to the Baseline Assessment Report (`docs/reports/baseline-assessment-2026-06-19.md`), SRS-BUILDNEST-001 v4.0, SDD-BUILDNEST-001 v3.0, TP-BUILDNEST-001 v4.0, and RTM-BUILDNEST-001 v1.0, all dated 2026-06-19.*
