# Audit / Review Report

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                                                                                                                                                                                                                                                                                                                                             |
| :----------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Document Title**       | Internal Quality Management System Audit Report                                                                                                                                                                                                                                                                                                                   |
| **Document ID**          | ARR-BUILDNEST-001                                                                                                                                                                                                                                                                                                                                                 |
| **Version**              | 1.0                                                                                                                                                                                                                                                                                                                                                               |
| **Audit Date**           | February 11, 2026                                                                                                                                                                                                                                                                                                                                                 |
| **Status**               | Issued                                                                                                                                                                                                                                                                                                                                                            |
| **Classification**       | Internal Use — Confidential                                                                                                                                                                                                                                                                                                                                       |
| **Conformance Standard** | ISO 9001:2015 — Quality management systems — Requirements                                                                                                                                                                                                                                                                                                         |
| **Audit Type**           | Internal (1st Party) — Full Systems Audit                                                                                                                                                                                                                                                                                                                         |
| **Related Documents**    | [QMP](Quality_Management_Plan_IEEE_12207.md), [SQR](Software_Quality_Report_ISO_25010.md), [MMR](Metrics_Measurement_Report_IEEE_15939.md), [CSD](Coding_Standards_Document_ISO_25010.md), [TP](Test_Plan_IEEE_29119.md), [TER](Test_Execution_Report_IEEE_29119.md), [DBR](Defect_Bug_Report_IEEE_29119.md), [VVR](Verification_Validation_Report_IEEE_12207.md) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author       | Changes                                   | Approval   |
| :------ | :--------- | :----------- | :---------------------------------------- | :--------- |
| 1.0     | 2026-02-11 | Lead Auditor | Initial full systems audit — Clauses 4–10 | ✅ Pending |

### Document Approval

| Role                | Name         | Signature  | Date         |
| :------------------ | :----------- | :--------- | :----------- |
| **Lead Auditor**    | QA Lead      | ****\_**** | ****\_\_**** |
| **Quality Manager** | QA Manager   | ****\_**** | ****\_\_**** |
| **Management Rep.** | Project Lead | ****\_**** | ****\_\_**** |

---

## Table of Contents

1. [Audit Overview](#1-audit-overview)
2. [Audit Scope & Criteria](#2-audit-scope--criteria)
3. [Audit Summary](#3-audit-summary)
4. [Clause 4: Context of the Organization](#4-clause-4-context-of-the-organization)
5. [Clause 5: Leadership](#5-clause-5-leadership)
6. [Clause 6: Planning](#6-clause-6-planning)
7. [Clause 7: Support](#7-clause-7-support)
8. [Clause 8: Operation](#8-clause-8-operation)
9. [Clause 9: Performance Evaluation](#9-clause-9-performance-evaluation)
10. [Clause 10: Improvement](#10-clause-10-improvement)
11. [Findings Register](#11-findings-register)
12. [Corrective Action Requests](#12-corrective-action-requests)
13. [Audit Conclusion](#13-audit-conclusion)

---

## 1. Audit Overview

### 1.1 Audit Objectives

|  #  | Objective                                                                                |
| :-: | :--------------------------------------------------------------------------------------- |
|  1  | Determine conformity of the BuildNest QMS with ISO 9001:2015 requirements (Clauses 4–10) |
|  2  | Evaluate the effectiveness of the QMS in achieving quality objectives                    |
|  3  | Identify opportunities for improvement (OFIs) in processes and documented information    |
|  4  | Verify implementation of corrective actions from previous defect reports                 |
|  5  | Assess risk-based thinking integration across software lifecycle processes               |

### 1.2 Audit Team

| Role             | Name        | Qualification                            |
| :--------------- | :---------- | :--------------------------------------- |
| **Lead Auditor** | QA Lead     | ISO 9001:2015 Internal Auditor certified |
| **Auditor**      | QA Engineer | ISO 9001:2015 awareness trained          |
| **Observer**     | Dev Lead    | Technical subject matter expert          |

### 1.3 Audit Schedule

| Time          | Activity                                 | Auditee               |
| :------------ | :--------------------------------------- | :-------------------- |
| 09:00 – 09:30 | Opening meeting                          | All stakeholders      |
| 09:30 – 10:30 | Clause 4: Context & Clause 5: Leadership | Project Lead, QA Mgr  |
| 10:30 – 11:30 | Clause 6: Planning & Clause 7: Support   | QA Manager, Dev Lead  |
| 11:30 – 13:00 | Clause 8: Operation                      | Dev Team, QA Engineer |
| 13:00 – 13:30 | Lunch break                              | —                     |
| 13:30 – 14:30 | Clause 9: Performance Evaluation         | QA Team, DevOps       |
| 14:30 – 15:30 | Clause 10: Improvement                   | QA Manager, Dev Lead  |
| 15:30 – 16:00 | Closing meeting & findings summary       | All stakeholders      |

### 1.4 Conformance Statement

> This audit report was conducted in conformance with **ISO 9001:2015** — Quality management systems — Requirements, and follows audit methodology prescribed by **ISO 19011:2018** — Guidelines for auditing management systems. All findings are supported by objective evidence obtained through document review, codebase inspection, and configuration analysis.

---

## 2. Audit Scope & Criteria

### 2.1 Scope

| Dimension             | Coverage                                                                                                                                    |
| :-------------------- | :------------------------------------------------------------------------------------------------------------------------------------------ |
| **Organization**      | BuildNest project team (Development, QA, DevOps)                                                                                            |
| **Product**           | BuildNest E-Commerce Platform (Spring Boot 3.5.10, Java 21)                                                                                 |
| **Processes Covered** | Software development, testing, security, deployment, monitoring, documentation                                                              |
| **Standard Clauses**  | ISO 9001:2015 Clauses 4–10 (all mandatory requirements)                                                                                     |
| **Exclusions**        | Clause 8.3.2 (Design inputs from external customers — N/A for internal product), Clause 8.5.5 (Post-delivery activities — not yet released) |
| **Codebase Size**     | 28 controllers · 56 services · 19 repos · 48 models · 38 configs · 167 test files                                                           |
| **Document Suite**    | 19 ISO/IEC/IEEE standard-conformant documents across 6 standards                                                                            |

### 2.2 Audit Criteria

| Criteria Source | Standard / Document                                                          |
| :-------------- | :--------------------------------------------------------------------------- |
| **Primary**     | ISO 9001:2015 — Quality management systems — Requirements                    |
| **Supporting**  | ISO/IEC 25010:2011 — Product quality model                                   |
| **Supporting**  | ISO/IEC/IEEE 12207:2017 — Software lifecycle processes                       |
| **Supporting**  | ISO/IEC/IEEE 15939:2017 — Measurement process                                |
| **Internal**    | [QMP](Quality_Management_Plan_IEEE_12207.md) — Quality policies & objectives |
| **Internal**    | [CSD](Coding_Standards_Document_ISO_25010.md) — Coding standards             |

### 2.3 Evidence Types Used

| Evidence Type              | Examples                                                               |
| :------------------------- | :--------------------------------------------------------------------- |
| **Documented Information** | ISO docs (19 files), `pom.xml`, `application.properties`, `Dockerfile` |
| **Records**                | JaCoCo reports, PITest reports, OWASP reports, Git commit history      |
| **Source Code Review**     | Controllers, services, configs, security classes, exception handlers   |
| **Configuration**          | Maven profiles, CI/CD pipeline, Kubernetes manifests, Docker Compose   |

---

## 3. Audit Summary

### 3.1 Results Overview

| ISO 9001 Clause  | Sub-Clauses Audited | Conformity | Minor NC | Major NC |  OFI  | Result          |
| :--------------- | :-----------------: | :--------: | :------: | :------: | :---: | :-------------- |
| 4 — Context      | 4.1, 4.2, 4.3, 4.4  |     10     |    0     |    0     |   1   | ✅ Conformant   |
| 5 — Leadership   |    5.1, 5.2, 5.3    |     8      |    0     |    0     |   1   | ✅ Conformant   |
| 6 — Planning     |    6.1, 6.2, 6.3    |     9      |    0     |    0     |   1   | ✅ Conformant   |
| 7 — Support      |       7.1–7.5       |     16     |    1     |    0     |   1   | ⚠️ Minor NC     |
| 8 — Operation    |       8.1–8.7       |     22     |    1     |    1     |   2   | ❌ Major NC     |
| 9 — Performance  |    9.1, 9.2, 9.3    |     10     |    0     |    0     |   1   | ✅ Conformant   |
| 10 — Improvement |  10.1, 10.2, 10.3   |     8      |    0     |    0     |   1   | ✅ Conformant   |
| **TOTAL**        |       **30**        |   **83**   |  **2**   |  **1**   | **8** | **Conditional** |

### 3.2 Overall Verdict

> **CONDITIONAL CONFORMANCE** — The BuildNest QMS demonstrates substantial conformity with ISO 9001:2015. One **Major Non-Conformance** (NC-MAJ-001: DEF-003 Stored XSS — Clause 8.7) and two **Minor Non-Conformances** must be resolved before full certification recommendation. Eight **Opportunities for Improvement** have been identified for continual improvement per Clause 10.

---

## 4. Clause 4: Context of the Organization

### 4.1 Understanding the Organization and Its Context (§4.1)

| Audit Question                               | Evidence Examined                                                                                                      | Finding       |
| :------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are internal and external issues determined? | [SRS](SRS_IEEE_29148_2018.md) §1.2 Scope — market context, competitive landscape, regulatory requirements              | ✅ Conformant |
| Are issues monitored and reviewed?           | [QMP](Quality_Management_Plan_IEEE_12207.md) §11 Risk Register — 8 quality risks with external/internal classification | ✅ Conformant |
| Is the strategic direction established?      | [SRS](SRS_IEEE_29148_2018.md) §1.4 — project objectives, business goals documented                                     | ✅ Conformant |

### 4.2 Understanding Needs and Expectations of Interested Parties (§4.2)

| Audit Question                     | Evidence Examined                                                                                                                            | Finding       |
| :--------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are interested parties identified? | [SRS](SRS_IEEE_29148_2018.md) §2 — End users, Admin users, Payment gateway, Delivery partners                                                | ✅ Conformant |
| Are their requirements determined? | [BRD](Business_Rules_Document_IEEE_29148.md) — 6 business rule categories, [UCS](Use_Case_Specification_IEEE_29148.md) — 12 use case modules | ✅ Conformant |

### 4.3 Determining the Scope of the QMS (§4.3)

| Audit Question            | Evidence Examined                                                                                                             | Finding       |
| :------------------------ | :---------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Is QMS scope documented?  | [QMP](Quality_Management_Plan_IEEE_12207.md) §1.2 — Product scope, lifecycle scope, organizational scope                      | ✅ Conformant |
| Are exclusions justified? | [QMP](Quality_Management_Plan_IEEE_12207.md) — No unjustified exclusions; applicable clauses mapped to 22 lifecycle processes | ✅ Conformant |

### 4.4 QMS and Its Processes (§4.4)

| Audit Question                                | Evidence Examined                                                                                                                       | Finding       |
| :-------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are processes identified and documented?      | [QMP](Quality_Management_Plan_IEEE_12207.md) §4 — All 22 ISO 12207 lifecycle processes mapped                                           | ✅ Conformant |
| Are process interactions defined?             | [SAD](Software_Architecture_Document_IEEE_42010.md) — Component diagrams, [HLD](High_Level_Design_IEEE_42010.md) — layered architecture | ✅ Conformant |
| Are process criteria and methods established? | [QMP](Quality_Management_Plan_IEEE_12207.md) §5 — Entry/exit criteria for each technical process                                        | ✅ Conformant |

**OFI-01:** Consider creating a formal process interaction diagram (turtle diagram) showing inputs, outputs, resources, and KPIs for each QMS process.

---

## 5. Clause 5: Leadership

### 5.1 Leadership and Commitment (§5.1)

| Audit Question                                     | Evidence Examined                                                                                                 | Finding       |
| :------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------- | :------------ |
| Does top management demonstrate commitment to QMS? | [QMP](Quality_Management_Plan_IEEE_12207.md) §3.1 — Quality Policy with 6 principles, signed by Project Lead      | ✅ Conformant |
| Is customer focus promoted?                        | [SRS](SRS_IEEE_29148_2018.md) §2 User Needs, [UCS](Use_Case_Specification_IEEE_29148.md) — user-centric use cases | ✅ Conformant |
| Is risk-based thinking integrated?                 | [QMP](Quality_Management_Plan_IEEE_12207.md) §11 — 8-item risk register, `ResilienceConfig.java`                  | ✅ Conformant |

### 5.2 Quality Policy (§5.2)

| Audit Question                            | Evidence Examined                                                                                                                               | Finding       |
| :---------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Is quality policy documented?             | [QMP](Quality_Management_Plan_IEEE_12207.md) §3.1 — 6 policy principles (Prevention, Standards, Automation, Security, Measurement, Improvement) | ✅ Conformant |
| Is it appropriate to purpose and context? | Policies reference Java 21 / Spring Boot stack, CI/CD pipeline, ISO standards — appropriate                                                     | ✅ Conformant |
| Is it communicated and available?         | Quality policy included in QMP, referenced by CSD and SQR documentation                                                                         | ✅ Conformant |

### 5.3 Organizational Roles, Responsibilities and Authorities (§5.3)

| Audit Question                          | Evidence Examined                                                                                                                                                                              | Finding       |
| :-------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are roles and responsibilities defined? | [QMP](Quality_Management_Plan_IEEE_12207.md) §2 — 8 roles with ISO 12207 clause mapping (Project Lead, QA Manager, Dev Lead, Security Lead, QA Engineer, Developer, DevOps, Measurement Owner) | ✅ Conformant |
| Is QMS responsibility assigned?         | [QMP](Quality_Management_Plan_IEEE_12207.md) §2.1 — QA Manager designated as quality authority with independence mandate                                                                       | ✅ Conformant |

**OFI-02:** Consider documenting role assignments with named individuals rather than role titles only.

---

## 6. Clause 6: Planning

### 6.1 Actions to Address Risks and Opportunities (§6.1)

| Audit Question                          | Evidence Examined                                                                                                       | Finding       |
| :-------------------------------------- | :---------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are risks and opportunities identified? | [QMP](Quality_Management_Plan_IEEE_12207.md) §11 — 8 quality risks (QR-01 to QR-08) with likelihood, impact, mitigation | ✅ Conformant |
| Are risk mitigations planned?           | Each risk has assigned control: `ResilienceConfig`, `ChaosEngineeringFilter`, OWASP scanning, PITest thresholds         | ✅ Conformant |
| Is effectiveness of actions evaluated?  | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §7 — IND-03 Security Risk Index, IND-05 Process Health Index            | ✅ Conformant |

### 6.2 Quality Objectives and Planning to Achieve Them (§6.2)

| Audit Question                                            | Evidence Examined                                                                                                                                                         | Finding       |
| :-------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :------------ |
| Are quality objectives established at relevant functions? | [QMP](Quality_Management_Plan_IEEE_12207.md) §3.2 — 12 objectives (QO-01 to QO-12) mapped to ISO 25010 characteristics                                                    | ✅ Conformant |
| Are objectives measurable?                                | Each QO has a quantitative target: QO-01 (≥80%), QO-04 (≥95%), QO-10 (≤24h), QO-11 (P95 ≤200ms), QO-12 (≥99.9%)                                                           | ✅ Conformant |
| Is planning to achieve objectives documented?             | [QMP](Quality_Management_Plan_IEEE_12207.md) §12 — 6 planned improvements with timeline, [MMR](Metrics_Measurement_Report_IEEE_15939.md) §13 — 6 measurement improvements | ✅ Conformant |

### 6.3 Planning of Changes (§6.3)

| Audit Question                                    | Evidence Examined                                                                                                       | Finding       |
| :------------------------------------------------ | :---------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are changes to QMS conducted in a planned manner? | `ApiSunsetConfig.java` — API versioning strategy, Liquibase (`liquibase-core`) — DB migration versioning, Git branching | ✅ Conformant |
| Is integrity of QMS maintained during changes?    | [QMP](Quality_Management_Plan_IEEE_12207.md) §4 — Quality gates at each lifecycle phase prevent regression              | ✅ Conformant |

**OFI-03:** Consider formalizing a Management of Change (MOC) procedure document for changes to the QMS itself (beyond code changes).

---

## 7. Clause 7: Support

### 7.1 Resources (§7.1)

| Sub-Clause           | Audit Question                               | Evidence Examined                                                                                                                             | Finding       |
| :------------------- | :------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| 7.1.1 General        | Are resources determined and provided?       | `pom.xml` — 30+ dependencies, Docker/K8s infrastructure, monitoring stack                                                                     | ✅ Conformant |
| 7.1.2 People         | Are necessary people determined?             | [QMP](Quality_Management_Plan_IEEE_12207.md) §2 — 8 roles defined, responsibilities assigned                                                  | ✅ Conformant |
| 7.1.3 Infrastructure | Is infrastructure determined and maintained? | `Dockerfile`, `docker-compose.yml`, `kubernetes-deployment-optimized.yaml`, `ContainerOptimizationConfig.java`, `GracefulShutdownConfig.java` | ✅ Conformant |
| 7.1.4 Environment    | Is process environment determined?           | Spring Profiles for dev/test/prod, H2 for test, MySQL for prod, `TestProfilePropertyValidator.java`                                           | ✅ Conformant |
| 7.1.5 Monitoring     | Are monitoring resources determined?         | Micrometer, Prometheus, Spring Actuator, JaCoCo, PITest, OWASP — all configured in `pom.xml` and dedicated config classes                     | ✅ Conformant |
| 7.1.6 Knowledge      | Is organizational knowledge managed?         | 19 ISO documents, Javadoc (`failOnError=true`, `doclint=all`), OpenAPI specs, Coding Standards                                                | ✅ Conformant |

### 7.2 Competence (§7.2)

| Audit Question                      | Evidence Examined                                                                | Finding       |
| :---------------------------------- | :------------------------------------------------------------------------------- | :------------ |
| Is competence determined?           | [QMP](Quality_Management_Plan_IEEE_12207.md) §2 — Required competencies per role | ✅ Conformant |
| Is evidence of competence retained? | Code review history (Git), test authorship, documented responsibilities          | ✅ Conformant |

### 7.3 Awareness (§7.3)

| Audit Question                        | Evidence Examined                                                                               | Finding       |
| :------------------------------------ | :---------------------------------------------------------------------------------------------- | :------------ |
| Are persons aware of quality policy?  | [QMP](Quality_Management_Plan_IEEE_12207.md) §3.1 — Documented and distributed                  | ✅ Conformant |
| Are they aware of quality objectives? | [QMP](Quality_Management_Plan_IEEE_12207.md) §3.2 — Objectives documented with measurement plan | ✅ Conformant |

### 7.4 Communication (§7.4)

| Audit Question                                 | Evidence Examined                                                                                                                                        | Finding       |
| :--------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are internal communications on QMS determined? | [QMP](Quality_Management_Plan_IEEE_12207.md) §9 — 6 audit types, [MMR](Metrics_Measurement_Report_IEEE_15939.md) §4.1 — communication procedures defined | ✅ Conformant |

### 7.5 Documented Information (§7.5)

| Audit Question                                                              | Evidence Examined                                                                            | Finding       |
| :-------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------- | :------------ |
| 7.5.1 — Does QMS include required documented information?                   | 19 ISO/IEC/IEEE documents spanning 6 standards; Document Information tables in all documents | ✅ Conformant |
| 7.5.2 — Are documents properly created with identification and description? | Document ID, Version, Date, Status in all documents; Revision History and Approval tables    | ✅ Conformant |
| 7.5.3 — Is documented information controlled?                               | Git version control for all documents; Document Control sections with revision history       | ✅ Conformant |
| 7.5.3 — Is adequate protection of documented information ensured?           | Git repository access control, branch protection (`bugfix/iso-ieee-documentation`)           | ⚠️ Minor NC   |

**NC-MIN-001:** Document access control policy is not formally documented. While Git branch protection provides technical control, there is no written access control matrix specifying who can approve/modify quality documents. This is a minor non-conformance against §7.5.3.2(e) — protection of documented information.

**OFI-04:** Consider implementing branch protection rules with required reviewer approvals for the `docs/` directory to enforce documented information control.

---

## 8. Clause 8: Operation

### 8.1 Operational Planning and Control (§8.1)

| Audit Question                          | Evidence Examined                                                                                                   | Finding       |
| :-------------------------------------- | :------------------------------------------------------------------------------------------------------------------ | :------------ |
| Are processes planned and controlled?   | [QMP](Quality_Management_Plan_IEEE_12207.md) §4 — 22 lifecycle processes mapped, §5 — quality gates for each        | ✅ Conformant |
| Are criteria for processes established? | [QMP](Quality_Management_Plan_IEEE_12207.md) §5 — Entry/exit criteria for all 11 technical processes (6.4.1–6.4.11) | ✅ Conformant |

### 8.2 Requirements for Products and Services (§8.2)

| Sub-Clause          | Audit Question                                | Evidence Examined                                                                                                                      | Finding       |
| :------------------ | :-------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| 8.2.1 Communication | Are customer communication processes defined? | OpenAPI/Swagger (`ComprehensiveAPIDocConfig.java`), structured error responses (`GlobalExceptionHandler`), health endpoints            | ✅ Conformant |
| 8.2.2 Determining   | Are product requirements determined?          | [SRS](SRS_IEEE_29148_2018.md)— functional & non-functional requirements, [BRD](Business_Rules_Document_IEEE_29148.md) — business rules | ✅ Conformant |
| 8.2.3 Review        | Are requirements reviewed?                    | [RTM](Requirements_Traceability_Matrix_IEEE_29148.md) — SRS → TCS full traceability                                                    | ✅ Conformant |
| 8.2.4 Changes       | Are requirement changes controlled?           | SRS version control (Git), RTM reflects current requirements                                                                           | ✅ Conformant |

### 8.3 Design and Development (§8.3)

| Sub-Clause     | Audit Question                                     | Evidence Examined                                                                                                                                                                                     | Finding       |
| :------------- | :------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| 8.3.1 General  | Is D&D process established?                        | [SAD](Software_Architecture_Document_IEEE_42010.md), [HLD](High_Level_Design_IEEE_42010.md), [LLD](Low_Level_Design_IEEE_42010.md), [SDD](SDD_IEEE_1016_2017.md) — comprehensive design documentation | ✅ Conformant |
| 8.3.2 Planning | Is D&D planned considering stages and reviews?     | Design documents cover all stages: Architecture → High Level → Low Level → Detailed, with design viewpoints per IEEE 1016/42010                                                                       | ✅ Conformant |
| 8.3.3 Inputs   | Are D&D inputs determined?                         | [SRS](SRS_IEEE_29148_2018.md) — functional/NFR inputs, [ICD](Interface_Control_Document_IEEE_42010.md) — interface inputs                                                                             | ✅ Conformant |
| 8.3.4 Controls | Are D&D reviews/verification/validation conducted? | [VVR](Verification_Validation_Report_IEEE_12207.md) — V&V activities, [TER](Test_Execution_Report_IEEE_29119.md) — test results                                                                       | ✅ Conformant |
| 8.3.5 Outputs  | Do D&D outputs meet input requirements?            | [RTM](Requirements_Traceability_Matrix_IEEE_29148.md) — SRS → Design → Test traceability                                                                                                              | ✅ Conformant |
| 8.3.6 Changes  | Are D&D changes identified and controlled?         | Git commit history, design document version control, Liquibase migrations                                                                                                                             | ✅ Conformant |

### 8.4 Control of Externally Provided Processes, Products and Services (§8.4)

| Audit Question                                           | Evidence Examined                                                                                             | Finding       |
| :------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------ | :------------ |
| Are externally provided products controlled?             | OWASP Dependency-Check 9.0.9 (`failBuildOnCVSS=7`) — automated vulnerability scanning of all 30+ dependencies | ✅ Conformant |
| Are selection criteria defined for external providers?   | Razorpay integration (`RazorpayProperties.java`), Elasticsearch, Redis — production-grade vendor selection    | ✅ Conformant |
| Is verification applied to externally provided products? | OWASP scan verifies dependency security; integration tests verify external service behaviour                  | ✅ Conformant |

### 8.5 Production and Service Provision (§8.5)

| Sub-Clause                          | Audit Question                        | Evidence Examined                                                                                          | Finding       |
| :---------------------------------- | :------------------------------------ | :--------------------------------------------------------------------------------------------------------- | :------------ |
| 8.5.1 Control                       | Are production conditions controlled? | `Dockerfile`, K8s manifests, Spring Profiles, `GracefulShutdownConfig`, monitoring stack, circuit breakers | ✅ Conformant |
| 8.5.2 Identification & Traceability | Are outputs identified and traceable? | Maven version (`3.5.10`), Docker image tags, Git SHA in builds, `AppConfig.java` application metadata      | ✅ Conformant |
| 8.5.3 Third-party property          | N/A for internal platform             | —                                                                                                          | ➖ N/A        |
| 8.5.4 Preservation                  | Are outputs preserved?                | Docker image registry, Git repository, CI artifact storage                                                 | ✅ Conformant |
| 8.5.6 Change control                | Are production changes controlled?    | Liquibase migrations, K8s rolling updates, `ApiSunsetConfig.java` for API versioning                       | ✅ Conformant |

### 8.6 Release of Products and Services (§8.6)

| Audit Question                                       | Evidence Examined                                                                                          | Finding       |
| :--------------------------------------------------- | :--------------------------------------------------------------------------------------------------------- | :------------ |
| Are planned arrangements for verification completed? | [TER](Test_Execution_Report_IEEE_29119.md) — 124 test cases, 5 Maven profiles (unit, all, e2e, stress, ci) | ✅ Conformant |
| Is evidence of conformity retained?                  | JaCoCo HTML reports, PITest XML/HTML reports, OWASP HTML/JSON reports, Surefire XML reports                | ✅ Conformant |
| Is release authorised by relevant authority?         | **GAP**: Pass rate 86.3% (target 95%) — release should be blocked                                          | ⚠️ Minor NC   |

**NC-MIN-002:** Test pass rate of 86.3% (107/124) is below the quality objective QO-04 target of ≥95%. Per §8.6, products should not be released until planned arrangements are satisfactorily completed. The failing test cases are linked to DEF-003 (S1 XSS) and DEF-002 (S2 Inventory). Release gates exist (`ci` Maven profile) but the CI minimum coverage is set at 40% rather than the 80% production target. This soft gate permits release without full conformity.

### 8.7 Control of Nonconforming Outputs (§8.7)

| Audit Question                                               | Evidence Examined                                                                                                                  | Finding       |
| :----------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are nonconforming outputs identified and controlled?         | [DBR](Defect_Bug_Report_IEEE_29119.md) — structured defect lifecycle, [QMP](Quality_Management_Plan_IEEE_12207.md) §8 — NCR system | ✅ Conformant |
| Is documented information retained on nonconforming outputs? | [DBR](Defect_Bug_Report_IEEE_29119.md) — 5 defects with severity, priority, lifecycle dates, environment details                   | ✅ Conformant |
| Are nonconforming outputs prevented from unintended use?     | **CRITICAL GAP**: DEF-003 (Stored XSS) — S1 Critical severity defect remains unresolved                                            | ❌ Major NC   |

**NC-MAJ-001:** **Stored XSS vulnerability (DEF-003) in ProductReview comments remains unresolved.** Per §8.7.1, the organization shall deal with nonconforming outputs to prevent their unintended use or delivery. A Critical (S1) security vulnerability that enables cross-site scripting attacks constitutes a nonconforming output that has not been adequately controlled. The defect allows malicious script injection through user-generated content, directly compromising end-user data confidentiality and integrity.

- **Root Cause:** Missing HTML sanitization in review/comment input pipeline.
- **ISO 25010 Impact:** Security → Integrity (NCR-SEC-001 per [SQR](Software_Quality_Report_ISO_25010.md))
- **Corrective Action Required:** See CAR-001.

**OFI-05:** Consider implementing automated DAST (Dynamic Application Security Testing) via OWASP ZAP in the CI pipeline to detect XSS and injection vulnerabilities before release.

**OFI-06:** Consider raising the CI coverage gate from 40% to 80% in phases to align with the QO-01 production target.

---

## 9. Clause 9: Performance Evaluation

### 9.1 Monitoring, Measurement, Analysis and Evaluation (§9.1)

| Audit Question                               | Evidence Examined                                                                                                             | Finding        |
| :------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- | :------------- |
| Is it determined what needs to be monitored? | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §2 — 7 entities, 26 attributes, 21 base measures                              | ✅ Conformant  |
| Are methods for monitoring determined?       | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §5 — collection tool for each BM (JaCoCo, PITest, OWASP, Gatling, Prometheus) | ✅ Conformant  |
| Is timing of monitoring determined?          | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §4.2 — per-build, per-release, continuous, monthly frequencies defined        | ✅ Conformant  |
| Are results analysed and evaluated?          | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §12 — Analysis & Interpretation with root cause analysis                      | ✅ Conformant  |
| Is customer satisfaction monitored?          | **Limited**: No formal customer satisfaction survey mechanism; user feedback via API error tracking only                      | ⚠️ Observation |

### 9.2 Internal Audit (§9.2)

| Audit Question                                      | Evidence Examined                                                                                                                                          | Finding       |
| :-------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are internal audits conducted at planned intervals? | [QMP](Quality_Management_Plan_IEEE_12207.md) §9.1 — 6 audit types defined (Code Review, Process Compliance, Security, Standards, Pre-release, Remediation) | ✅ Conformant |
| Is an audit programme established?                  | [QMP](Quality_Management_Plan_IEEE_12207.md) §9.1 — audit frequencies (sprint/monthly/quarterly/per-release)                                               | ✅ Conformant |
| Are audit criteria and scope defined per audit?     | This document (ARR-BUILDNEST-001) — systematic Clause 4–10 audit                                                                                           | ✅ Conformant |
| Are results reported to relevant management?        | Audit findings documented with CAR tracking, management approval tables                                                                                    | ✅ Conformant |

### 9.3 Management Review (§9.3)

| Audit Question                                         | Evidence Examined                                                                                                                                                             | Finding       |
| :----------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are management reviews conducted at planned intervals? | [QMP](Quality_Management_Plan_IEEE_12207.md) §9.2 — 6 review types (Sprint Quality, Milestone, Release Readiness, Document Compliance, Defect Triage, Continuous Improvement) | ✅ Conformant |
| Do reviews consider audit results?                     | Review inputs include: audit findings, NCR status, measurement results, risk register changes                                                                                 | ✅ Conformant |
| Are review outputs documented?                         | Decision records, CAR assignments, improvement actions — documented per review                                                                                                | ✅ Conformant |

**OFI-07:** Consider formalizing customer satisfaction measurement mechanisms (e.g., NPS surveys, API usage analytics, support ticket trends) per §9.1.2.

---

## 10. Clause 10: Improvement

### 10.1 General (§10.1)

| Audit Question                                  | Evidence Examined                                                                                                                                                                                                             | Finding       |
| :---------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------ |
| Are improvement opportunities determined?       | [QMP](Quality_Management_Plan_IEEE_12207.md) §12 — 6 planned improvements; [SQR](Software_Quality_Report_ISO_25010.md) §13 — 7-item roadmap; [MMR](Metrics_Measurement_Report_IEEE_15939.md) §13 — 6 measurement improvements | ✅ Conformant |
| Are actions taken to improve QMS effectiveness? | Improvements include: SonarQube integration, ZAP scanning, Pact testing, threshold ramps                                                                                                                                      | ✅ Conformant |

### 10.2 Nonconformity and Corrective Action (§10.2)

| Audit Question                      | Evidence Examined                                                                                                                           | Finding        |
| :---------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------ | :------------- |
| Are nonconformities reacted to?     | [DBR](Defect_Bug_Report_IEEE_29119.md) — structured defect lifecycle, [QMP](Quality_Management_Plan_IEEE_12207.md) §8 — NCR and CAPA system | ✅ Conformant  |
| Is root cause analysis performed?   | [QMP](Quality_Management_Plan_IEEE_12207.md) §8 — 3 NCRs with root cause analysis, 6 CAPAs assigned                                         | ✅ Conformant  |
| Are corrective actions effective?   | **Partial**: CAPAs assigned (CAPA-001 to CAPA-006) but not yet closed. Effectiveness verification pending post-implementation               | ⚠️ Observation |
| Is documented information retained? | [DBR](Defect_Bug_Report_IEEE_29119.md) — retention 5 years, [QMP](Quality_Management_Plan_IEEE_12207.md) §10 — retention policy             | ✅ Conformant  |

### 10.3 Continual Improvement (§10.3)

| Audit Question                                       | Evidence Examined                                                                                                  | Finding       |
| :--------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------- | :------------ |
| Is the QMS continually improved?                     | [QMP](Quality_Management_Plan_IEEE_12207.md) §12 — PDCA cycle defined, 6 improvements planned                      | ✅ Conformant |
| Are measurement trend analyses used for improvement? | [MMR](Metrics_Measurement_Report_IEEE_15939.md) §12 — Trend analysis table (Sprint N-2 to N), baseline established | ✅ Conformant |
| Is risk-based thinking applied for improvement?      | [QMP](Quality_Management_Plan_IEEE_12207.md) §11 — Risk register feeds improvement priorities                      | ✅ Conformant |

**OFI-08:** Consider aligning improvement cycles with formal PDCA review cadence (e.g., quarterly PDCA review meetings with documented minutes).

---

## 11. Findings Register

### 11.1 Non-Conformances

| NC ID      | Clause | Type  | Description                                                                    | Severity | Root Cause                                   |
| :--------- | :----- | :---- | :----------------------------------------------------------------------------- | :------- | :------------------------------------------- |
| NC-MAJ-001 | 8.7    | Major | Stored XSS (DEF-003) — unresolved S1 critical vulnerability in ProductReview   | Critical | Missing HTML sanitization in input pipeline  |
| NC-MIN-001 | 7.5    | Minor | No formal access control policy for quality documentation                      | Low      | Document control procedures not formalized   |
| NC-MIN-002 | 8.6    | Minor | CI quality gate (40%) misaligned with production target (80%); pass rate 86.3% | Medium   | Gate threshold set conservatively during dev |

### 11.2 Opportunities for Improvement

| OFI ID | Clause | Description                                                                             | Priority |
| :----- | :----- | :-------------------------------------------------------------------------------------- | :------- |
| OFI-01 | 4.4    | Create formal process interaction diagrams (turtle diagrams) for QMS processes          | P3       |
| OFI-02 | 5.3    | Assign named individuals to QMS roles (not just role titles)                            | P3       |
| OFI-03 | 6.3    | Formalize Management of Change (MOC) procedure for QMS modifications                    | P2       |
| OFI-04 | 7.5    | Implement Git branch protection rules with required reviewers for docs/ directory       | P2       |
| OFI-05 | 8.7    | Integrate OWASP ZAP (DAST) into CI pipeline for automated XSS/injection detection       | P1       |
| OFI-06 | 8.6    | Progressively raise CI coverage gate: 40% → 60% → 80%                                   | P2       |
| OFI-07 | 9.1    | Establish formal customer satisfaction measurement (NPS, API analytics, support trends) | P2       |
| OFI-08 | 10.3   | Align PDCA improvement cycles with formal quarterly review cadence                      | P3       |

---

## 12. Corrective Action Requests

### CAR-001 — Stored XSS Vulnerability Remediation

| Field                   | Detail                                                                                                                                                                                               |
| :---------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CAR ID**              | CAR-001                                                                                                                                                                                              |
| **NC Reference**        | NC-MAJ-001 (Clause 8.7)                                                                                                                                                                              |
| **Defect Reference**    | DEF-003 ([DBR](Defect_Bug_Report_IEEE_29119.md))                                                                                                                                                     |
| **Description**         | Stored XSS vulnerability in ProductReview comments allows malicious script injection                                                                                                                 |
| **Root Cause**          | No HTML sanitization applied to user-generated content before persistence                                                                                                                            |
| **Corrective Action**   | 1. Add OWASP HTML Sanitizer dependency<br/>2. Implement input sanitization in ReviewService<br/>3. Add XSS test patterns in security test suite<br/>4. Verify fix resolves all related failing tests |
| **Responsible**         | Dev Team Lead                                                                                                                                                                                        |
| **Target Date**         | 2026-02-14                                                                                                                                                                                           |
| **Verification Method** | Re-run security test suite, OWASP ZAP scan, manual penetration test                                                                                                                                  |
| **Status**              | Open                                                                                                                                                                                                 |

### CAR-002 — Document Access Control Policy

| Field                   | Detail                                                                                                                                                          |
| :---------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CAR ID**              | CAR-002                                                                                                                                                         |
| **NC Reference**        | NC-MIN-001 (Clause 7.5)                                                                                                                                         |
| **Description**         | No formal access control policy for quality documentation                                                                                                       |
| **Root Cause**          | Git branch protection relied upon implicitly; no documented procedure                                                                                           |
| **Corrective Action**   | 1. Create Document Control Procedure<br/>2. Define access control matrix (who can approve, modify, view)<br/>3. Configure Git branch protection rules for docs/ |
| **Responsible**         | QA Manager                                                                                                                                                      |
| **Target Date**         | 2026-02-28                                                                                                                                                      |
| **Verification Method** | Review written procedure, verify Git protection configuration                                                                                                   |
| **Status**              | Open                                                                                                                                                            |

### CAR-003 — CI Quality Gate Alignment

| Field                   | Detail                                                                                                                                                                          |
| :---------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **CAR ID**              | CAR-003                                                                                                                                                                         |
| **NC Reference**        | NC-MIN-002 (Clause 8.6)                                                                                                                                                         |
| **Description**         | CI coverage gate (40%) permits release below QO-01 target (80%)                                                                                                                 |
| **Root Cause**          | Initial CI threshold set conservatively; not updated as codebase matured                                                                                                        |
| **Corrective Action**   | 1. Phase 1: Raise JaCoCo CI minimum to 60% (2026-03-01)<br/>2. Phase 2: Raise to 70% (2026-04-01)<br/>3. Phase 3: Raise to 80% (2026-06-01)<br/>4. Align pass rate gate to ≥95% |
| **Responsible**         | DevOps + Dev Team Lead                                                                                                                                                          |
| **Target Date**         | 2026-06-01 (final phase)                                                                                                                                                        |
| **Verification Method** | Verify `pom.xml` JaCoCo check rules, CI build failure on below-threshold coverage                                                                                               |
| **Status**              | Open                                                                                                                                                                            |

---

## 13. Audit Conclusion

### 13.1 Summary of Findings

| Category               |      Count       |
| :--------------------- | :--------------: |
| Clauses Audited        | 7 (Clauses 4–10) |
| Sub-Clauses Audited    |        30        |
| Conformity findings    |        83        |
| Major Non-Conformances |        1         |
| Minor Non-Conformances |        2         |
| OFIs Identified        |        8         |
| CARs Issued            |        3         |

### 13.2 Audit Opinion

> The BuildNest E-Commerce Platform QMS demonstrates **substantial conformity** with ISO 9001:2015 across all 7 auditable clauses. The quality management infrastructure — including documented quality policy, 12 measurable objectives, 22 mapped lifecycle processes, comprehensive measurement system, and automated quality gates — provides a strong foundation.
>
> However, **one Major Non-Conformance** (NC-MAJ-001: unresolved S1 Stored XSS vulnerability) and **two Minor Non-Conformances** (document access control, CI gate misalignment) must be resolved before full certification recommendation.
>
> The **8 Opportunities for Improvement** demonstrate healthy areas for continual enhancement per Clause 10.

### 13.3 Certification Recommendation

| Decision               | Status                                                                  |
| :--------------------- | :---------------------------------------------------------------------- |
| **Recommendation**     | **CONDITIONAL** — Pending closure of NC-MAJ-001, NC-MIN-001, NC-MIN-002 |
| **Re-audit Required**  | Yes — targeted re-audit after CAR-001 completion (target: 2026-02-21)   |
| **Full Certification** | Eligible upon successful re-audit and closure of all 3 CARs             |

### 13.4 Follow-Up Actions

| Action                                          | Owner         | Due Date   |
| :---------------------------------------------- | :------------ | :--------- |
| Close CAR-001 (XSS remediation)                 | Dev Team Lead | 2026-02-14 |
| Close CAR-002 (Document access control policy)  | QA Manager    | 2026-02-28 |
| Initiate CAR-003 Phase 1 (coverage gate to 60%) | DevOps        | 2026-03-01 |
| Targeted re-audit (Clauses 7.5, 8.6, 8.7)       | Lead Auditor  | 2026-02-21 |
| Present OFI implementation plan to management   | QA Manager    | 2026-03-01 |

---

**— End of Audit Report —**

_This audit was conducted in conformance with **ISO 9001:2015** — Quality management systems — Requirements, following the methodology of **ISO 19011:2018** — Guidelines for auditing management systems._
