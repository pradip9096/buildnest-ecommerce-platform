# CI with Pull Request Using GitHub Actions — End-to-End Walkthrough

A worked example walking a PR through GitHub Actions CI — workflow file, quality gates, a
failure/fix cycle, branch protection, and merge — using the same product-review feature as
[topic-11.md](topic-11.md).

## Table of Contents

1. [Developer Implements Code](#1-developer-implements-code)
2. [Create Pull Request](#2-create-pull-request)
3. [GitHub Actions Workflow File](#3-github-actions-workflow-file)
4. [CI Pipeline Execution](#4-ci-pipeline-execution)
5. [Add More Quality Gates](#5-add-more-quality-gates)
6. [PR Shows CI Result](#6-pr-shows-ci-result)
7. [Failed CI Example](#7-failed-ci-example)
8. [Branch Protection Rule](#8-branch-protection-rule)
9. [Merge After Successful CI](#9-merge-after-successful-ci)
- [Complete Lifecycle](#complete-lifecycle)
- [Mental Model](#mental-model)
- [See Also](#see-also)

---

**Core idea:**

> A Pull Request proposes a code change. GitHub Actions automatically runs CI checks against that change. The PR can be merged only if the quality gates pass.

Flow:

```text
Developer

    │

    ▼

Create Feature Branch

    │

    ▼

Push Code

    │

    ▼

Open Pull Request

    │

    ▼

GitHub Actions Trigger

    │

    ▼

CI Pipeline

    ├── Build
    ├── Unit Tests
    ├── Integration Tests
    ├── Code Quality
    └── Security Checks

    │

    ▼

Quality Gate

    │

 ┌──┴───┐
 │      │
PASS   FAIL
 │      │
 ▼      ▼

Merge  Block PR
```

---

## Scenario

Example:

Requirement:

```text
Add Product Review API
```

Issue:

```text
Issue #125

feat: implement product review system
```

Developer creates branch:

```bash
git switch -c feature/125-product-review
```

---

## 1. Developer Implements Code

Example:

```text
src/main/java/

ReviewController.java
ReviewService.java
ReviewRepository.java


src/test/java/

ReviewServiceTest.java
```

---

Commit:

```bash
git add .

git commit -m "feat: add product review feature

Refs #125"
```

Push:

```bash
git push -u origin feature/125-product-review
```

---

## 2. Create Pull Request

Using GitHub CLI:

```bash
gh pr create \
--base main \
--title "feat: add product reviews" \
--body "Closes #125"
```

Now:

```text
Pull Request #210


Source:

feature/125-product-review


Target:

main
```

---

At this moment GitHub emits an event:

```text
pull_request opened
```

GitHub Actions listens for that event.

---

## 3. GitHub Actions Workflow File

Repository:

```text
.github/

└── workflows/

    └── ci.yml
```

Example Spring Boot CI:

```yaml
name: Backend CI


on:
  pull_request:
    branches:
      - main


jobs:

  build-test:

    runs-on: ubuntu-latest


    steps:

      - name: Checkout repository
        uses: actions/checkout@v4


      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'


      - name: Build application
        run: ./mvnw clean compile


      - name: Run tests
        run: ./mvnw test


      - name: Package application
        run: ./mvnw package
```

---

Meaning:

```text
Someone opens PR into main

          │

          ▼

Run ci.yml
```

---

## 4. CI Pipeline Execution

GitHub Actions creates a runner:

```text
GitHub Hosted Runner

Ubuntu VM

     │

     ▼

Clone Repository

     │

     ▼

Install Java 21

     │

     ▼

Build Project

     │

     ▼

Execute Tests
```

---

Example output:

```text
Pull Request #210


Checks:


✔ Checkout

✔ Setup Java 21

✔ Maven Compile

✔ Unit Tests

✔ Package


CI Successful
```

---

## 5. Add More Quality Gates

Production CI usually grows:

```yaml
name: Production CI


on:
  pull_request:
    branches:
      - main


jobs:

  verify:


    runs-on: ubuntu-latest


    steps:


      - uses: actions/checkout@v4


      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: temurin


      - name: Run tests with coverage
        run: ./mvnw test jacoco:report


      - name: Dependency Security Check
        run: ./mvnw dependency-check:check


      - name: Verify package
        run: ./mvnw verify
```

---

Now CI verifies:

```text
Code Change

     │

     ├── Does it compile?
     │
     ├── Do tests pass?
     │
     ├── Is coverage acceptable?
     │
     ├── Are dependencies safe?
     │
     └── Can it build?
```

---

## 6. PR Shows CI Result

Inside GitHub PR:

```text
Pull Request #210


Checks


backend-ci

✔ Passed


JUnit Tests

✔ Passed


Security Scan

✔ Passed
```

---

Developer sees:

```text
Ready to merge
```

---

## 7. Failed CI Example

Developer introduces bug:

```java
public int calculateTotal(){
    return 0;
}
```

Test fails:

```text
CI Result:


Build        PASS

Tests        FAIL


ReviewServiceTest

Expected: 500
Actual: 0
```

GitHub:

```text
Pull Request blocked
```

Developer fixes:

```bash
git add .

git commit -m "fix: correct price calculation"

git push
```

Same PR:

```text
PR #210

Automatically reruns CI
```

---

## 8. Branch Protection Rule

Production repositories protect `main`.

Settings:

```text
main branch protection


Before merge require:


✔ Pull Request

✔ Approval

✔ GitHub Actions success

✔ Security checks

✔ Latest branch
```

---

Without passing CI:

```text
Merge Button Disabled
```

---

## 9. Merge After Successful CI

Command:

```bash
gh pr merge \
--squash \
--delete-branch
```

Result:

```text
feature branch

       │

       ▼

CI verified

       │

       ▼

main branch
```

---

## Complete Lifecycle

```text
Issue #125

    ↓

Branch

feature/125-product-review


    ↓

Commit


    ↓

Pull Request #210


    ↓

GitHub Actions Trigger


    ↓

CI Pipeline


    ↓

Automated Tests


    ↓

Quality Gate


    ↓

Code Review


    ↓

Merge


    ↓

Issue Closed
```

---

## Mental Model

```text
Pull Request
=
"Can I introduce this change?"


GitHub Actions CI
=
"Automatically prove it is safe"


Tests
=
"Evidence"


Quality Gate
=
"Pass/fail decision"


Merge
=
"Accepted into official codebase"
```

Together, **PR + GitHub Actions turns code merging from a trust-based process into an evidence-based engineering workflow.**

---

## See Also

- [topic-4.md](topic-4.md) — Testing → Quality Gate → CI → PR as a closed-loop system (concept)
- [topic-11.md](topic-11.md) — the planning-layer half of this same worked example
- `.github/workflows/` (this repo) — the real CI workflows this topic's `ci.yml` example
  generalizes from (see `.claude/rules/common/development-workflow.md`'s CI Failure Handling
  section for actual blocking-vs-advisory gate behavior)
