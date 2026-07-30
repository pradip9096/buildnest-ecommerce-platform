# Worked Example: BuildNest E-Commerce "Add Product Review Feature" End-to-End

A single feature carried through the full two-layer model from [topic-10.md](topic-10.md) —
Issue → Label → Milestone → Project (planning), then Branch → PR → CI → Merge (engineering) —
using a real BuildNest example end-to-end.

## Table of Contents

- Planning Layer (Issue, Labels, Milestone, Project — narrative walkthrough below, not separate headings)
5. [Create Development Branch](#5-create-development-branch)
6. [Implement Code](#6-implement-code)
7. [Commit Changes](#7-commit-changes)
8. [Push Branch](#8-push-branch)
9. [Create Pull Request](#9-create-pull-request)
10. [CI Pipeline Runs Automatically](#10-ci-pipeline-runs-automatically)
11. [Merge Pull Request](#11-merge-pull-request)
12. [Automatic Updates](#12-automatic-updates)
- [See Also](#see-also)

---

A real production-style workflow from planning → merge:

```text
Business Requirement:

Customers should be able to write product reviews.

                 │
                 ▼

================================================
                 PLANNING LAYER
================================================


1. GitHub Issue
"What exactly needs to be done?"

Issue #125

Title:
feat: implement product review system


Description:

Problem:
Currently customers cannot share feedback about products.


Functional Requirements:

- Customer can add review
- Customer can give 1–5 star rating
- Customer can edit own review
- Customer can delete own review
- Product page displays reviews


Acceptance Criteria:

✔ POST /products/{id}/reviews works
✔ Only authenticated users can review
✔ Rating validation exists
✔ Unit tests pass
✔ API documentation updated
```

---

Now classify the issue.

```text
2. Labels
"What type of work is this?"


Issue #125

Labels:

feature
backend
api
database
priority-high
```

Meaning:

```text
feature       → new capability

backend       → backend team responsibility

api           → REST endpoint changes

database      → schema changes required

priority-high → important work
```

---

Attach delivery target.

```text
3. Milestone
"When should it be delivered?"


Milestone:

BuildNest v1.0 Release


Contains:

#100 User Authentication
#110 Product Catalog
#120 Shopping Cart
#125 Product Reviews
#130 Payment Integration
```

Progress:

```text
BuildNest v1.0

Completed:

██████░░░░ 60%
```

---

Track execution.

```text
4. GitHub Project


Project:
BuildNest Backend Development


Board View:


TODO

#125 Product Review


        ↓


IN PROGRESS

#125 Product Review


        ↓


REVIEW


        ↓


DONE
```

Project fields:

```text
Issue:
#125 Product Review


Status:
In Progress


Sprint:
Sprint 8


Owner:
Pradip


Estimate:
5 Story Points
```

---

Now engineering begins.

```text
================================================
          ENGINEERING EXECUTION LAYER
================================================
```

---

## 5. Create Development Branch

Issue:

```text
#125 Product Review
```

Create branch:

```bash
git switch -c feature/125-product-review
```

Structure:

```text
main

A---B---C

        \
         D---E

feature/125-product-review
```

---

## 6. Implement Code

Example changes:

```text
src/

review/

├── ReviewController.java
├── ReviewService.java
├── ReviewRepository.java
├── ReviewEntity.java
└── ReviewDTO.java


tests/

└── ReviewServiceTest.java
```

---

## 7. Commit Changes

```bash
git add .

git commit -m "feat: implement product review system

Refs #125"
```

Traceability:

```text
Commit
   |
   |
connects to
   |
   ▼

Issue #125
```

---

## 8. Push Branch

```bash
git push -u origin feature/125-product-review
```

---

## 9. Create Pull Request

```bash
gh pr create \
--title "feat: add product review system" \
--body "Closes #125"
```

Creates:

```text
Pull Request #210


Source:

feature/125-product-review


Target:

main


Linked:

Issue #125
```

---

## 10. CI Pipeline Runs Automatically

GitHub Actions starts:

```text
Pull Request #210

        │

        ▼

CI Pipeline


Build Application

        │

        ├── Maven Build
        │
        ├── Unit Tests
        │
        ├── Integration Tests
        │
        ├── JaCoCo Coverage
        │
        ├── Security Scan
        │
        └── Code Quality Check
```

---

CI result:

```text
Quality Gate


✔ Build successful

✔ 240 tests passed

✔ Coverage 85%

✔ No critical vulnerabilities

✔ Code review approved


RESULT:

PASS
```

---

## 11. Merge Pull Request

```bash
gh pr merge \
--squash \
--delete-branch
```

Now:

```text
main


A---B---C---F


F =
Product Review Feature
```

---

## 12. Automatic Updates

After merge:

```text
Issue #125

Status:
Closed


Project:

DONE


Milestone:

BuildNest v1.0

Progress:

███████░░░ 70%
```

---

Complete traceability:

```text
Business Need:

"Users need product reviews"


        ↓


Issue #125

Defines requirement


        ↓


Labels

Classify work


        ↓


Milestone v1.0

Release planning


        ↓


Project Sprint 8

Execution tracking


        ↓


Branch

feature/125-product-review


        ↓


Code + Commit


        ↓


PR #210


        ↓


CI + Quality Gate


        ↓


Merge


        ↓


Feature Delivered
```

This is the same pattern used in many professional repositories: **plan the change → isolate the work → verify automatically → integrate safely.**

---

## See Also

- [topic-10.md](topic-10.md) — the two-layer mental model this worked example applies
- [topic-9.md](topic-9.md), [topic-1.md](topic-1.md) — the underlying `gh` command walkthroughs for planning and engineering layers
