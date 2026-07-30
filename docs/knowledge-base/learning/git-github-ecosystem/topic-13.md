# FAQ: GitHub Professional Workflow (Issue → Project → PR → CI → Merge)

25 questions covering the full professional GitHub workflow — planning objects (Issue, Label,
Milestone, Project, Epic, Sprint) through engineering execution (Branch, PR, CI, Quality Gate,
Merge).

## Table of Contents

1. [What is the complete GitHub software development workflow?](#1-q-what-is-the-complete-github-software-development-workflow)
2. [What is the difference between Git and GitHub?](#2-q-what-is-the-difference-between-git-and-github)
3. [What is a GitHub Issue?](#3-q-what-is-a-github-issue)
4. [What is a GitHub Label?](#4-q-what-is-a-github-label)
5. [What is a Milestone?](#5-q-what-is-a-milestone)
6. [What is a GitHub Project?](#6-q-what-is-a-github-project)
7. [What are GitHub Project Views?](#7-q-what-are-github-project-views)
8. [What is an Epic?](#8-q-what-is-an-epic)
9. [What is a Sprint?](#9-q-what-is-a-sprint)
10. [Epic vs Milestone vs Sprint?](#10-q-epic-vs-milestone-vs-sprint)
11. [Why create a branch?](#11-q-why-create-a-branch)
12. [Can I jump between branches?](#12-q-can-i-jump-between-branches)
13. [What is a Pull Request?](#13-q-what-is-a-pull-request)
14. [Can PR exist without CI?](#14-q-can-pr-exist-without-ci)
15. [Can CI exist without PR?](#15-q-can-ci-exist-without-pr)
16. [Why combine PR and CI?](#16-q-why-combine-pr-and-ci)
17. [What is GitHub Actions?](#17-q-what-is-github-actions)
18. [How does PR trigger CI?](#18-q-how-does-pr-trigger-ci)
19. [What happens inside CI?](#19-q-what-happens-inside-ci)
20. [What is automated testing?](#20-q-what-is-automated-testing)
21. [What is a Quality Gate?](#21-q-what-is-a-quality-gate)
22. [What happens after failed CI?](#22-q-what-happens-after-failed-ci)
23. [What happens after merge?](#23-q-what-happens-after-merge)
24. [How are Issues and PRs connected?](#24-q-how-are-issues-and-prs-connected)
25. [What is the final mental model?](#25-q-what-is-the-final-mental-model)
- [See Also](#see-also)

---

## 1. Q: What is the complete GitHub software development workflow?

**A:** A production-style workflow usually follows:

```text
Requirement / Problem

        ↓

GitHub Issue

        ↓

Label + Milestone + Project

        ↓

Branch

        ↓

Implementation

        ↓

Commit

        ↓

Pull Request

        ↓

CI Pipeline

        ↓

Quality Gate

        ↓

Merge

        ↓

Release
```

It connects **planning → execution → verification → delivery**.

---

## 2. Q: What is the difference between Git and GitHub?

**A:**

```text
Git
=
Version control system

Manages:
- commits
- branches
- history


GitHub
=
Collaboration platform

Manages:
- repositories
- issues
- PRs
- projects
- CI/CD
```

Mental model:

```text
Git  → Code history
GitHub → Engineering collaboration
```

---

## 3. Q: What is a GitHub Issue?

**A:**

An Issue is a **trackable work item**.

It answers:

> What needs to be done and why?

Examples:

```text
Issue #45

Implement payment API

Contains:

Problem
Requirements
Acceptance Criteria
Discussion
Progress
```

Issue types:

* Feature
* Bug
* Enhancement
* Documentation
* Technical debt

---

## 4. Q: What is a GitHub Label?

**A:**

A label classifies an issue.

It answers:

> What type of work is this?

Example:

```text
Issue #45 Payment API

Labels:

feature
backend
priority-high
security
```

Common categories:

```text
Type:
bug
feature
documentation


Area:
frontend
backend
database


Priority:
P0
P1
P2
```

---

## 5. Q: What is a Milestone?

**A:**

A milestone represents a delivery target.

It answers:

> What are we trying to complete?

Example:

```text
Milestone:

BuildNest v1.0


Contains:

#10 Authentication
#20 Products
#30 Orders
#40 Payment
```

Purpose:

* Release planning
* Progress tracking
* Delivery management

---

## 6. Q: What is a GitHub Project?

**A:**

A Project is a planning and tracking board.

It answers:

> Where is the work currently?

Example:

```text
Project Board


TODO

#10 Authentication


IN PROGRESS

#20 Payment


DONE

#30 Logging
```

---

## 7. Q: What are GitHub Project Views?

**A:**

Views are different ways to look at project data.

Same issues, different visualization.

Examples:

```text
Board View
→ Workflow tracking


Table View
→ Detailed management


Roadmap View
→ Timeline planning
```

---

## 8. Q: What is an Epic?

**A:**

An Epic is a large capability broken into smaller issues.

Example:

```text
Epic:

Authentication System


Contains:

#1 Register API

#2 Login API

#3 JWT Security

#4 Password Reset
```

Relationship:

```text
Epic
 ↓
Issues
 ↓
Tasks
```

---

## 9. Q: What is a Sprint?

**A:**

A Sprint is a fixed execution period.

Example:

```text
Sprint 5

Duration:
2 weeks


Selected Work:

#10 Login
#11 JWT
#12 Tests
```

It answers:

> What are we working on now?

---

## 10. Q: Epic vs Milestone vs Sprint?

**A:**

```text
Epic
=
Organize by capability


Milestone
=
Organize by release goal


Sprint
=
Organize by time
```

Example:

```text
Epic:
Payment System


Sprint:
Sprint 3 implementation


Milestone:
Version 1.0 Release
```

---

## 11. Q: Why create a branch?

**A:**

A branch creates an isolated workspace.

Example:

```text
main

A---B---C

        \
         D---E

feature/payment
```

You can change code without affecting stable code.

---

## 12. Q: Can I jump between branches?

**A: Yes.**

Example:

```bash
git switch feature/login
```

Switch:

```bash
git switch bugfix/payment
```

Return:

```bash
git switch feature/login
```

Save unfinished work first:

```bash
git stash
```

or commit.

---

## 13. Q: What is a Pull Request?

**A:**

A PR is a controlled request to merge changes.

It asks:

> Should this change enter the main codebase?

Flow:

```text
Feature Branch

      ↓

Pull Request

      ↓

Review + CI

      ↓

Merge
```

---

## 14. Q: Can PR exist without CI?

**A: Yes.**

Example:

```text
PR

 ↓

Human Review

 ↓

Merge
```

CI is optional.

---

## 15. Q: Can CI exist without PR?

**A: Yes.**

Example:

```text
git push main

      ↓

CI Trigger

      ↓

Build + Test
```

CI only needs an event.

---

## 16. Q: Why combine PR and CI?

**A:**

Because together they create quality control.

```text
PR
=
Human approval


CI
=
Machine verification
```

Together:

```text
Change

 ↓

Review

 ↓

Automated Tests

 ↓

Safe Merge
```

---

## 17. Q: What is GitHub Actions?

**A:**

GitHub Actions is GitHub's automation platform.

Commonly used for CI/CD.

Example:

```text
Pull Request Created

        ↓

GitHub Actions

        ↓

Build

        ↓

Tests

        ↓

Quality Checks
```

---

## 18. Q: How does PR trigger CI?

**A:**

Workflow file:

```yaml
on:
  pull_request:
    branches:
      - main
```

Meaning:

```text
Whenever PR targets main

run CI pipeline
```

---

## 19. Q: What happens inside CI?

**A:**

Typical pipeline:

```text
Checkout Code

      ↓

Setup Environment

      ↓

Compile

      ↓

Unit Tests

      ↓

Integration Tests

      ↓

Security Scan

      ↓

Quality Report
```

---

## 20. Q: What is automated testing?

**A:**

Tests automatically verify software behavior.

Examples:

```text
Unit Test
→ Individual class/function


Integration Test
→ Components together


E2E Test
→ Complete user journey
```

---

## 21. Q: What is a Quality Gate?

**A:**

A quality gate is a pass/fail decision.

Example:

```text
Rules:

✔ Build passes

✔ Tests pass

✔ Coverage >= 80%

✔ Security scan passes

✔ Review approved
```

If failed:

```text
Merge blocked
```

---

## 22. Q: What happens after failed CI?

**A:**

Developer fixes code:

```text
Fix problem

 ↓

Commit

 ↓

Push

 ↓

Same PR updated

 ↓

CI reruns
```

No new PR required.

---

## 23. Q: What happens after merge?

**A:**

Usually:

```text
PR merged

      ↓

Issue closed

      ↓

Project updated

      ↓

Milestone progress updated

      ↓

Branch deleted
```

---

## 24. Q: How are Issues and PRs connected?

**A:**

Using keywords:

```text
Closes #25

Fixes #25

Resolves #25
```

Example:

```bash
gh pr create \
--body "Closes #25"
```

When PR merges:

```text
Issue #25 closes automatically
```

---

## 25. Q: What is the final mental model?

**A:**

```text
PLANNING SYSTEM


Epic
 ↓
Issue
 ↓
Label
 ↓
Milestone
 ↓
Project
 ↓
Sprint


====================


ENGINEERING SYSTEM


Branch
 ↓
Commit
 ↓
Pull Request
 ↓
CI
 ↓
Quality Gate
 ↓
Merge
 ↓
Release
```

Short version:

```text
Issue
= Define work


Project
= Track work


Branch
= Build safely


PR
= Request approval


CI
= Verify automatically


Quality Gate
= Decide acceptance


Merge
= Deliver change
```

This is the foundation of a modern GitHub-based software engineering workflow.

---

## See Also

- [topic-3.md](topic-3.md) — beginner-level Git/GitHub FAQ (fundamentals, this file assumes them)
- [topic-14.md](topic-14.md) — interview-style Git/GitHub/GitHub Actions questions
- [topic-10.md](topic-10.md) — the two-layer mental model this FAQ's Q25 summarizes
