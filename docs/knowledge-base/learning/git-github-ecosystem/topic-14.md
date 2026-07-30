# Common Interview Questions: Git, GitHub, and GitHub Actions (With Answers)

30 interview-style questions across three sections — Git fundamentals, GitHub (Issues/PRs/
Projects), and GitHub Actions/CI — pitched at Java Backend, Full Stack, and junior/DevOps
interview level.

## Table of Contents

- [Section 1 — Git Fundamentals](#section-1--git-fundamentals) (Q1–Q10: commits, branches,
  merge vs rebase, conflicts, stash, fetch vs pull)
- [Section 2 — GitHub Questions](#section-2--github-questions) (Q11–Q20: PRs, Issues, Labels,
  Milestones, Projects)
- [Section 3 — GitHub Actions / CI](#section-3--github-actions--ci) (Q21–Q30: Actions, workflows,
  runners, triggers, Quality Gates, production workflow)
- [Final Interview Summary](#final-interview-summary)
- [See Also](#see-also)

---

## Section 1 — Git Fundamentals

---

## 1. What is Git?

**Answer:**

Git is a **distributed version control system** used to track changes in source code.

It helps developers:

* Maintain code history
* Work in parallel
* Restore previous versions
* Collaborate safely

Mental model:

```text
Developer Machine

Working Directory
        ↓
     Staging
        ↓
     Commit
        ↓
 Git Repository History
```

---

## 2. What is the difference between Git and GitHub?

**Answer:**

| Git                  | GitHub                        |
| -------------------- | ----------------------------- |
| Version control tool | Repository hosting platform   |
| Runs locally         | Runs online                   |
| Tracks commits       | Enables collaboration         |
| Manages branches     | Provides PRs, Issues, Actions |

Example:

```text
Git:
"Track my code changes"


GitHub:
"Share, review, automate, and collaborate"
```

---

## 3. Explain the Git lifecycle.

**Answer:**

```text
Working Directory

        │
        │ git add
        ▼

Staging Area

        │
        │ git commit
        ▼

Local Repository

        │
        │ git push
        ▼

Remote Repository
(GitHub)
```

---

## 4. What is a commit?

**Answer:**

A commit is a **snapshot of repository changes at a specific point in time**.

Example:

```bash
git commit -m "feat: add login API"
```

Each commit contains:

* Unique SHA ID
* Author
* Timestamp
* Changes
* Message

---

## 5. What is the difference between `git add` and `git commit`?

**Answer:**

`git add`:

* Moves changes to staging area
* Prepares changes

`git commit`:

* Saves staged changes permanently in Git history

Flow:

```text
Modify File

    ↓

git add

    ↓

Ready for commit

    ↓

git commit

    ↓

Saved version
```

---

## 6. What is a branch?

**Answer:**

A branch is an **independent line of development**.

Example:

```text
main

A---B---C

        \
         D---E

     feature/login
```

Purpose:

* Isolate changes
* Develop features safely
* Enable parallel work

---

## 7. Difference between merge and rebase?

**Answer:**

### Merge

Combines histories:

```text
A---B---C
     \
      D---E

          ↓

A---B---C---M
     \     /
      D---E
```

Creates a merge commit.

---

### Rebase

Moves commits:

```text
Before:

A---B
     \
      C---D


After:

A---B---C---D
```

Creates linear history.

---

## 8. What is a merge conflict?

**Answer:**

A merge conflict happens when Git cannot automatically combine changes.

Example:

Developer A:

```java
return "Hello";
```

Developer B:

```java
return "Hi";
```

Git asks the developer to manually choose.

Resolution:

```bash
fix file

git add .

git commit
```

---

## 9. What is git stash?

**Answer:**

Stash temporarily stores unfinished changes.

Example:

```bash
git stash
```

Switch branch:

```bash
git switch main
```

Restore:

```bash
git stash pop
```

Use case:

> Need to change tasks without committing incomplete work.

---

## 10. Difference between git pull and git fetch?

**Answer:**

| Fetch                  | Pull               |
| ---------------------- | ------------------ |
| Downloads changes      | Downloads + merges |
| Safer                  | Direct update      |
| Does not modify branch | Changes branch     |

Equivalent:

```text
git pull

=

git fetch
+
git merge
```

---

## Section 2 — GitHub Questions

---

## 11. What is a Pull Request?

**Answer:**

A Pull Request is a **request to merge code changes from one branch into another**.

Flow:

```text
Feature Branch

      ↓

Pull Request

      ↓

Review + Testing

      ↓

Merge

      ↓

main
```

It provides:

* Code review
* Discussion
* Automated checks
* Change control

---

## 12. What is the difference between branch and PR?

**Answer:**

Branch:

> Where development happens.

PR:

> Process to review and approve branch changes.

Example:

```text
Branch
=
Developer workspace


PR
=
Approval workflow
```

---

## 13. What happens when a PR is created?

**Answer:**

Usually:

```text
PR Created

    ↓

Review Requested

    ↓

CI Pipeline Runs

    ↓

Tests Execute

    ↓

Quality Gate

    ↓

Approve

    ↓

Merge
```

---

## 14. Can a PR exist without CI?

**Answer:**

Yes.

PR is independent.

Example:

```text
PR

 ↓

Manual Review

 ↓

Merge
```

CI is optional.

---

## 15. Can CI exist without PR?

**Answer:**

Yes.

Example:

```yaml
on:
  push:
    branches:
      - main
```

Every push can trigger CI without a PR.

---

## 16. What are GitHub Issues?

**Answer:**

Issues are trackable work items.

Examples:

* Feature request
* Bug
* Enhancement
* Task

Example:

```text
Issue #50

Implement payment API

Status:
In Progress
```

---

## 17. How do Issues connect with PRs?

**Answer:**

Using keywords:

```text
Closes #50

Fixes #50

Resolves #50
```

Example:

```text
PR merged

      ↓

Issue automatically closed
```

---

## 18. What are GitHub Labels?

**Answer:**

Labels classify work.

Example:

```text
Issue:

Payment failure


Labels:

bug
backend
priority-high
```

---

## 19. What is a Milestone?

**Answer:**

A milestone groups issues toward a delivery goal.

Example:

```text
Milestone:

Version 1.0 Release


Contains:

Authentication
Payment
Orders
```

---

## 20. What is GitHub Project?

**Answer:**

A project tracks and manages work.

Example:

```text
TODO

Login API


IN PROGRESS

Payment API


DONE

Testing
```

---

## Section 3 — GitHub Actions / CI

---

## 21. What is GitHub Actions?

**Answer:**

GitHub Actions is GitHub's automation platform for CI/CD.

It automates:

* Build
* Testing
* Deployment
* Security checks

---

## 22. What is CI?

**Answer:**

Continuous Integration means automatically verifying code changes frequently.

Flow:

```text
Code Push

    ↓

Build

    ↓

Test

    ↓

Quality Check
```

Goal:

> Detect problems early.

---

## 23. Explain GitHub Actions workflow.

**Answer:**

A workflow is defined in:

```text
.github/workflows/
```

Example:

```yaml
name: CI

on:
  pull_request:
    branches:
      - main

jobs:
  test:

    runs-on: ubuntu-latest

    steps:

      - uses: actions/checkout@v4

      - run: mvn test
```

---

## 24. What are GitHub Actions components?

**Answer:**

```text
Workflow

   ↓

Event

   ↓

Job

   ↓

Step

   ↓

Action
```

Example:

```text
Event:
pull_request


Job:
build


Steps:
checkout
install
test
```

---

## 25. Difference between workflow, job, and step?

**Answer:**

Workflow:

> Complete automation process.

Job:

> Group of tasks executed on runner.

Step:

> Individual command/action.

Example:

```text
CI Workflow


Job:
Backend Build


Steps:

1. Checkout

2. Setup Java

3. Run Tests
```

---

## 26. What is a runner?

**Answer:**

A runner is a machine that executes GitHub Actions jobs.

Types:

* GitHub-hosted runner
* Self-hosted runner

Example:

```yaml
runs-on: ubuntu-latest
```

---

## 27. What triggers GitHub Actions?

**Answer:**

Events.

Examples:

```yaml
on:
  push

  pull_request

  schedule

  workflow_dispatch
```

---

## 28. What is a Quality Gate?

**Answer:**

Rules that decide whether code can proceed.

Example:

```text
Required:

✔ Build success

✔ Tests pass

✔ Security scan

✔ Coverage > 80%

✔ Review approval
```

---

## 29. How do you prevent broken code entering main?

**Answer:**

Using:

* Branch protection rules
* Pull requests
* Required reviews
* Required CI checks

Flow:

```text
Developer

 ↓

PR

 ↓

CI Pass

 ↓

Approval

 ↓

Merge
```

---

## 30. Explain a production GitHub workflow.

**Answer:**

```text
Requirement

 ↓

Issue

 ↓

Label + Milestone + Project

 ↓

Feature Branch

 ↓

Code

 ↓

Commit

 ↓

Pull Request

 ↓

GitHub Actions CI

 ↓

Quality Gate

 ↓

Merge

 ↓

Release
```

---

## Final Interview Summary

```text
Git
=
Version control


GitHub
=
Collaboration platform


Issue
=
Define work


Branch
=
Isolated development


PR
=
Change approval


GitHub Actions
=
Automation engine


CI
=
Automatic verification


Quality Gate
=
Release confidence


Merge
=
Accepted delivery
```

These questions cover the core Git/GitHub/GitHub Actions knowledge expected for most **Java Backend, Full Stack, DevOps beginner, and junior software engineering interviews**.

---

## See Also

- [topic-3.md](topic-3.md) — same fundamentals in FAQ form rather than interview form
- [topic-13.md](topic-13.md) — FAQ: GitHub professional workflow (Issue → Project → PR → CI → Merge)
- [topic-12.md](topic-12.md) — CI with Pull Requests using GitHub Actions, worked end-to-end
