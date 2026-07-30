# Milestone vs. Epic vs. Sprint

Different ways to organize work and the different management questions each one answers, plus
how GitHub implements each concept in practice.

## Table of Contents

1. [Epic](#1-epic)
2. [GitHub Issue vs Epic](#2-github-issue-vs-epic)
3. [Milestone](#3-milestone)
4. [Sprint](#4-sprint)
5. [Epic vs Milestone vs Sprint](#5-epic-vs-milestone-vs-sprint)
6. [Timeline Relationship](#6-timeline-relationship)
7. [GitHub Implementation](#7-github-implementation)
8. [Full Delivery Lifecycle](#8-full-delivery-lifecycle)
- [Final Mental Model](#final-mental-model)
- [See Also](#see-also)

---

**Claim:** Milestone, Epic, and Sprint are different ways to organize work. They answer different management questions:

```text
Epic      → "What large capability are we building?"
Milestone → "What target/release are we working toward?"
Sprint    → "What are we doing in this short time window?"
```

They complement GitHub Issues and Projects.

```text
Product Goal
     │
     ▼
Epic
     │
     ▼
Issues / User Stories
     │
     ▼
Sprint Execution
     │
     ▼
Milestone / Release
```

---

## 1. Epic

### Q: What is an Epic?

An **Epic is a large body of work that is broken into smaller issues/tasks.**

It represents a major capability.

Example:

```text
Epic:
User Authentication System

        │

        ├── Issue #10
        │   Create user registration API
        │
        ├── Issue #11
        │   Implement login
        │
        ├── Issue #12
        │   Add JWT authentication
        │
        ├── Issue #13
        │   Password reset
        │
        └── Issue #14
            Write authentication tests
```

---

### Epic answers:

> "What big feature or outcome are we trying to achieve?"

---

### Epic Characteristics

Usually:

* Large scope
* Multiple days/weeks/months
* Contains many issues
* Represents business capability

Examples:

```text
E-Commerce Project

Epic:
Authentication

Epic:
Product Management

Epic:
Shopping Cart

Epic:
Payment System

Epic:
Order Management
```

---

## 2. GitHub Issue vs Epic

An issue is executable work.

An epic groups related work.

```text
Epic
│
├── Issue
├── Issue
└── Issue
```

Example:

```text
Epic:
Payment System


Issues:

#20 Integrate Razorpay API

#21 Store payment transactions

#22 Add refund handling

#23 Add payment tests
```

---

## 3. Milestone

### Q: What is a Milestone?

A **Milestone is a target checkpoint, usually representing a release or major delivery goal.**

It answers:

> "When should this group of work be completed?"

---

Example:

```text
Milestone:
Version 1.0 Release


Due Date:
30 July


Contains:

✔ Authentication
✔ Product Catalog
✔ Cart
✔ Checkout
```

---

GitHub Milestone:

```text
v1.0 Backend Release

Progress:
████████░░ 80%

Open Issues:
5

Closed Issues:
20
```

---

### Milestone Groups Issues

Example:

```text
Milestone: BuildNest v1.0

        │

        ├── Issue #10 Login
        ├── Issue #20 Product API
        ├── Issue #30 Cart API
        └── Issue #40 Payment
```

---

### Milestone Answers:

* What are we releasing?
* What is remaining?
* How close are we?
* What is the deadline?

---

## 4. Sprint

### Q: What is a Sprint?

A **Sprint is a fixed short execution cycle where selected work is completed.**

Common Scrum duration:

* 1 week
* 2 weeks
* 4 weeks

---

Example:

```text
Sprint 5

Duration:
July 1 → July 14


Goal:
Complete Authentication


Sprint Backlog:

#10 Login API

#11 JWT Security

#12 Authentication Tests
```

---

Sprint answers:

> "What are we working on right now?"

---

### Sprint Flow

```text
Product Backlog

100 Issues

      │

Select priority work

      ▼

Sprint Backlog

10 Issues

      │

Develop

      ▼

Completed Increment
```

---

## 5. Epic vs Milestone vs Sprint

| Concept   | Main Question        | Focus           |
| --------- | -------------------- | --------------- |
| Epic      | What capability?     | Feature scope   |
| Milestone | What target/release? | Delivery goal   |
| Sprint    | What now?            | Execution cycle |

---

Example with an e-commerce project:

```text
Epic:

Payment System

        │

        ▼

Issues:

#50 Payment API
#51 Razorpay Integration
#52 Refund Logic


        │


Sprint:

Sprint 7

Work selected:

#50
#51


        │


Milestone:

BuildNest v1.0 Release

Includes:

Authentication
Products
Cart
Payment
```

---

## 6. Timeline Relationship

```text
Product Roadmap

│
├── Epic: Authentication
│
├── Epic: Payment
│
└── Epic: Orders


            Milestone v1.0
       ┌───────────────────┐
       │                   │

Sprint 1   Sprint 2   Sprint 3

Login      Cart       Payment
JWT        Product    Testing
```

---

## 7. GitHub Implementation

GitHub does not have a native "Epic" object like Jira.

Usually:

### Epic

Implemented using:

* Large GitHub Issue
* Parent issue
* Label: `epic`
* Project field

Example:

```text
Issue #1

[EPIC] Authentication System


Sub Issues:

#2 Register API
#3 Login API
#4 JWT
```

---

### Sprint

Implemented using:

GitHub Project field:

```text
Iteration:

Sprint 10
July 1 - July 14
```

---

### Milestone

Native GitHub feature:

```text
Repository

Issues

      │

      ▼

Milestones

      │

      ▼

v1.0 Release
```

---

## 8. Full Delivery Lifecycle

```text
Business Goal

     │

     ▼

Epic
(Big capability)

     │

     ▼

Issues
(Actionable work)

     │

     ▼

Project Board

     │

     ▼

Sprint
(Current execution)

     │

     ▼

Branch

     │

     ▼

Pull Request

     │

     ▼

CI + Quality Gate

     │

     ▼

Merge

     │

     ▼

Milestone Complete

     │

     ▼

Release
```

---

## Final Mental Model

```text
Epic
= Organize by capability


Sprint
= Organize by time


Milestone
= Organize by delivery target


Issue
= Actual work item


Project
= Tracking system


PR
= Code change control
```

Together they connect **planning → execution → verification → release**.

---

## See Also

- [topic-6.md](topic-6.md) — Issues, Projects, and Views as a work management system
- [topic-9.md](topic-9.md) — GitHub issue creation with label, milestone, and project via `gh`
- `.claude/rules/common/development-workflow.md` (this repo) — the real Sequence table this
  topic generalizes from (necessity tags, severity-based step merging, milestone/priority-at-
  creation rules)
