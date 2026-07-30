# Two-Layer Mental Model for a Production-Style GitHub Software Delivery Workflow

Planning Layer (Issue, Label, Milestone, Project Fields) vs. Engineering Execution Layer
(Branch, PR, CI, Merge), and how they chain into one auditable delivery pipeline.

## Table of Contents

1. [Issue = Define the Work](#1-issue--define-the-work)
2. [Label = Classification](#2-label--classification)
3. [Milestone = Delivery Target](#3-milestone--delivery-target)
4. [Project Fields = Management System](#4-project-fields--management-system)
5. [Branch = Isolated Workspace](#5-branch--isolated-workspace)
6. [Pull Request (PR) = Change Approval](#6-pull-request-pr--change-approval)
7. [CI = Automated Verification](#7-ci--automated-verification)
8. [Quality Gate = Decision Control](#8-quality-gate--decision-control)
9. [Merge = Integration](#9-merge--integration)
- [Complete Traceability Chain](#complete-traceability-chain)
- [Final Mental Model](#final-mental-model)
- [See Also](#see-also)

---

**two-layer mental model for a production-style GitHub software delivery workflow**:

> **Planning Layer:** Issue + Label + Milestone + Project Fields
> **Engineering Execution Layer:** Branch + Pull Request + CI + Merge

In other words:

> First decide **what should be built, why, priority, ownership, and schedule**.
> Then perform **implementation, verification, and integration**.

Complete flow:

```text
Business Need / Problem
          │
          ▼

=============================
     PLANNING LAYER
=============================

Issue
  │
  ├── Label
  │
  ├── Milestone
  │
  └── Project Fields


          │

          ▼

=============================
 ENGINEERING EXECUTION LAYER
=============================

Branch
  │
  ▼
Code Changes
  │
  ▼
Pull Request
  │
  ▼
CI Pipeline
  │
  ▼
Quality Gate
  │
  ▼
Merge


          │

          ▼

Released Capability
```

---

## 1. Issue = Define the Work

The GitHub Issue is the **unit of work**.

It answers:

> "What problem are we solving?"

Example:

```
Issue #42

Title:
Implement user authentication

Description:

Problem:
Users cannot securely login.

Requirements:
- Create login endpoint
- Generate JWT token
- Validate password

Acceptance Criteria:
- Valid login returns token
- Invalid login returns 401
- Tests exist
```

The issue becomes the source of truth.

---

## 2. Label = Classification

Labels answer:

> "What kind of work is this?"

Example:

```
Issue #42 Authentication

Labels:

feature
backend
security
priority-high
```

Labels help filtering:

```
Show me:

All security issues

or

All high priority bugs
```

Common production labels:

```
Type:

feature
bug
documentation
refactor


Area:

backend
frontend
database
security


Priority:

P0-critical
P1-high
P2-medium
P3-low
```

---

## 3. Milestone = Delivery Target

Milestone answers:

> "Which release or goal does this belong to?"

Example:

```
Milestone:

BuildNest v1.0 Release


Contains:

#10 Authentication
#20 Product Catalog
#30 Shopping Cart
#40 Payment
```

Progress:

```
v1.0 Release

Completed:
████████░░ 80%
```

A milestone groups work toward a delivery checkpoint.

---

## 4. Project Fields = Management System

GitHub Project provides workflow tracking.

It answers:

> "Where is this work right now?"

Example:

```
GitHub Project: Backend Development

------------------------------------------------

Issue              Status        Priority

Authentication     In Progress   High

Payment API        Todo          High

Logging            Done          Medium
```

Common project fields:

```
Status:
Todo
In Progress
Review
Done


Sprint:
Sprint 5


Owner:
Developer A


Estimate:
5 points
```

---

At this point:

```text
Planning completed:

✔ What?          Issue
✔ Category?      Label
✔ Release?       Milestone
✔ Tracking?      Project
```

Now engineering starts.

---

## 5. Branch = Isolated Workspace

Branch answers:

> "Where do we safely implement?"

Example:

Issue:

```
#42 Add authentication
```

Create branch:

```bash
git switch -c feature/42-authentication
```

Now:

```
main

 A---B---C

          \
           D---E

     feature/42-authentication
```

Developers can work without breaking `main`.

---

## 6. Pull Request (PR) = Change Approval

PR answers:

> "Should this code enter the main system?"

Flow:

```
feature branch

      │

      ▼

Pull Request

      │

Review
Discussion
Verification

      │

      ▼

main
```

Example:

```
PR #55

Title:
Implement authentication

Linked Issue:
Closes #42
```

---

## 7. CI = Automated Verification

CI answers:

> "Does this change actually work?"

PR triggers:

```
GitHub Actions

        │

        ▼

Build Application

        │

        ├── Unit Tests
        ├── Integration Tests
        ├── Security Scan
        ├── Code Analysis
        └── Coverage Check
```

Example result:

```
PR #55 Checks:

Build             PASS
JUnit Tests       PASS
Security Scan     PASS
Coverage          PASS
```

---

## 8. Quality Gate = Decision Control

Quality gate decides:

```
IF

Tests pass
AND
Security pass
AND
Review approved

THEN

Allow merge


ELSE

Block merge
```

It protects the main branch.

---

## 9. Merge = Integration

Merge means:

> "Approved change becomes part of the official codebase."

Example:

Before:

```
main

A---B---C


feature

A---B---C---D
```

After:

```
main

A---B---C---D
```

Then:

```
Issue #42 → Closed

Project Status → Done

Milestone Progress → Updated
```

---

## Complete Traceability Chain

Professional teams want every change traceable:

```text
Requirement

     ↓

Issue #42

     ↓

Label:
backend/security

     ↓

Milestone:
v1.0 Release

     ↓

Project:
Sprint 5

     ↓

Branch:
feature/42-auth

     ↓

Commit:
feat: add authentication

     ↓

PR #55

     ↓

CI Evidence

     ↓

Merge

     ↓

Release
```

---

## Final Mental Model

```
PLANNING LAYER

Issue
= Define work

Label
= Categorize work

Milestone
= Delivery target

Project
= Track execution


        ↓


ENGINEERING LAYER

Branch
= Safe workspace

PR
= Change control

CI
= Automated evidence

Quality Gate
= Acceptance decision

Merge
= Integration
```

Together they create an **auditable software delivery pipeline** connecting requirements → implementation → verification → release.

---

## See Also

- [topic-9.md](topic-9.md) — the planning-layer walkthrough (issue + label + milestone + project) via `gh`
- [topic-1.md](topic-1.md) — the engineering-layer walkthrough (branch + PR + CI + merge) via `gh`
- [topic-11.md](topic-11.md) — worked example applying this exact two-layer model end-to-end
