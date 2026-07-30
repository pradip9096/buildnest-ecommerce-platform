# Automated Testing → Quality Gate → CI → Pull Request as a Closed-Loop Quality Control System

How Automated Testing, Quality Gates, CI, and Pull Requests fit together as one feedback loop
that prevents unverified changes from reaching `main`.

## Table of Contents

1. [Pull Request (PR) = Change Proposal](#1-pull-request-pr--change-proposal)
2. [CI (Continuous Integration) = Verification Machine](#2-ci-continuous-integration--verification-machine)
3. [Automated Testing = Evidence Generator](#3-automated-testing--evidence-generator)
4. [Quality Gate = Decision Maker](#4-quality-gate--decision-maker)
- [Example Failure Scenario](#example-failure-scenario)
- [Example Success Scenario](#example-success-scenario)
- [Mapping to Real Tools](#mapping-to-real-tools)
- [In GitHub](#in-github)
- [Closed-Loop Control System View](#closed-loop-control-system-view)
- [See Also](#see-also)

---

The relationship between **Automated Testing → Quality Gate → CI → Pull Request (PR)** is a **closed-loop quality control system**.

Core idea:

> **A Pull Request proposes a change. CI verifies the change automatically. Automated tests generate evidence. Quality gates decide whether the change is acceptable.**

Flow:

```text
Developer
writes code
    │
    ▼
Creates Pull Request
    │
    ▼
CI Pipeline Starts
    │
    ▼
Automated Verification
    │
    ├── Build
    ├── Unit Tests
    ├── Integration Tests
    ├── Security Scan
    ├── Code Quality Scan
    └── Coverage Check
            │
            ▼
      Quality Gates
            │
      ┌─────┴─────┐
      │           │
      ▼           ▼
   PASS         FAIL

Merge        Block PR
Allowed      Fix Required
```

---

## 1. Pull Request (PR) = Change Proposal

A PR says:

> "I want to merge these code changes into the main codebase."

Example:

```text
feature/payment-api

        │
        │ Pull Request
        ▼

main branch
```

But the question is:

**Is this change safe?**

That is where CI begins.

---

## 2. CI (Continuous Integration) = Verification Machine

CI automatically checks every change.

Example tools:

* GitHub Actions
* Jenkins
* GitLab CI
* Azure Pipelines

When PR opens:

```text
PR Created Event
        │
        ▼
GitHub Actions Triggered
```

Example:

```yaml
on:
  pull_request:
    branches:
      - main
```

Meaning:

> "Whenever someone creates a PR into main, run verification."

---

## 3. Automated Testing = Evidence Generator

Testing answers:

> "Does the software still behave correctly?"

CI runs:

### Unit Tests

Small component verification:

```text
Service Method
      │
      ▼
JUnit Test
```

Example:

```java
@Test
void shouldCreateUserSuccessfully()
```

---

### Integration Tests

Verify components together:

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
```

---

### API Tests

Verify external behavior:

```text
HTTP Request

POST /users

      │

Expected Response
201 Created
```

---

### End-to-End Tests

Verify user journey:

```text
Browser
  |
Login
  |
Add Product
  |
Checkout
```

---

Testing produces signals:

```text
1000 tests executed

998 passed
2 failed
```

---

## 4. Quality Gate = Decision Maker

Quality gate asks:

> "Based on evidence, should this code enter main?"

Example rules:

```text
Quality Gate Rules

✔ Build successful
✔ Unit tests passed
✔ Integration tests passed
✔ Coverage >= 80%
✔ No critical vulnerabilities
✔ Code review approved
✔ No major code smells
```

---

Quality gate is basically:

```text
IF

tests = pass
AND
security = pass
AND
quality = pass
AND
review = approved

THEN

allow merge

ELSE

block merge
```

---

## Example Failure Scenario

Developer creates PR:

```text
feature/order-service
        |
        ▼
Pull Request #45
```

CI starts:

```text
Build       ✔
Unit Test   ✔
Security    ✔
Coverage    ✘ 55%
```

Quality gate:

```text
Required coverage: 80%
Actual coverage: 55%

Decision: FAIL
```

Result:

```text
Merge button disabled
```

Developer must improve tests.

---

## Example Success Scenario

```text
Pull Request #46

        │
        ▼

CI Pipeline

Build              ✔
Unit Tests         ✔
Integration Tests  ✔
Security Scan      ✔
Coverage           ✔
Review Approval    ✔

        │

Quality Gate PASS

        │

Merge Allowed
```

---

## Mapping to Real Tools

| Concept             | Tool Example            |
| ------------------- | ----------------------- |
| Pull Request        | GitHub PR               |
| CI Pipeline         | GitHub Actions          |
| Unit Testing        | JUnit, Mockito          |
| Integration Testing | Testcontainers          |
| Coverage Gate       | JaCoCo                  |
| Code Quality        | SonarQube               |
| Dependency Security | OWASP Dependency Check  |
| Review Gate         | Branch Protection Rules |

---

## In GitHub

Usually implemented like:

```text
GitHub Repository

main branch
     │
     ▼

Branch Protection Rule

Requires:

✔ PR before merge
✔ Required reviewers
✔ GitHub Actions pass
✔ Code scanning pass
✔ Coverage pass
```

---

## Closed-Loop Control System View

This matches engineering feedback loops:

```text
Change
  │
  ▼
PR
  │
  ▼
CI observes change
  │
  ▼
Tests measure quality
  │
  ▼
Quality Gate compares against standard
  │
  ▼
Decision
  │
  ├── Accept → Merge
  │
  └── Reject → Feedback → Improve
```

---

The mental model:

* **PR = Request for change**
* **CI = Automated inspector**
* **Automated tests = Measurements**
* **Quality gate = Acceptance criteria**
* **Merge = Approval after evidence**

Together they prevent uncontrolled changes from entering the main system.

---

## See Also

- [topic-5.md](topic-5.md) — can a Pull Request exist without CI?
- [topic-12.md](topic-12.md) — CI with Pull Requests using GitHub Actions, end-to-end walkthrough
- [topic-1.md](topic-1.md) — full PR workflow lifecycle this quality-gate loop sits inside
- `.claude/rules/common/testing.md` (this repo) — the real V&V workflow and blocking-vs-advisory
  CI gate distinctions (JaCoCo, PIT, SpotBugs, SonarCloud) this topic generalizes from
