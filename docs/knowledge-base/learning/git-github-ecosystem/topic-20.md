# Feature Branching for Parallel, Independent Issue Development

Whether multiple issues can be developed in parallel without interference — worked through with
concurrent branches, independent PRs/CI, conflict handling, and keeping a long-running branch in
sync with `main`.

## Table of Contents

- [Example Scenario](#example-scenario)
- [Developer A Works on Authentication](#developer-a-works-on-authentication)
- [Developer B Works on Product Search](#developer-b-works-on-product-search)
- [Each Issue Gets Its Own PR](#each-issue-gets-its-own-pr)
- [Merge Order Does Not Matter (Mostly)](#merge-order-does-not-matter-mostly)
- [What if Two Branches Change the Same File?](#what-if-two-branches-change-the-same-file)
- [Keeping Long-Running Branch Updated](#keeping-long-running-branch-updated)
- [Real Professional Flow](#real-professional-flow)
- [Solo Developer Example](#solo-developer-example)
- [Important Rule](#important-rule)
- [See Also](#see-also)

---

Yes. **Feature branching exists exactly for this reason: multiple independent issues can be developed in parallel without interfering with each other.**

The mental model:

```text
main
 │
 │
 ├── feature/101-user-login
 │
 ├── feature/102-product-search
 │
 ├── feature/103-payment-api
 │
 └── bugfix/104-price-calculation
```

Each branch is an isolated workspace connected to a specific issue.

---

## Example Scenario

Suppose your GitHub Project has:

```text
Issues:

#101 Implement user authentication

#102 Add product search

#103 Integrate payment gateway
```

Instead of doing everything in `main`:

```text
❌ Bad:

main

Authentication
+
Search
+
Payment

(all mixed together)
```

create separate branches:

```bash
git switch -c feature/101-authentication
```

```bash
git switch -c feature/102-product-search
```

```bash
git switch -c feature/103-payment
```

---

Now development happens independently:

```text
                 main

                  │

        ┌─────────┼──────────┐
        │         │          │

 feature/auth  feature/search feature/payment


Issue #101    Issue #102     Issue #103
```

---

## Developer A Works on Authentication

Branch:

```text
feature/101-authentication
```

Changes:

```text
UserController.java
AuthService.java
JwtProvider.java
```

Commit:

```bash
git commit -m "feat: implement authentication

Refs #101"
```

---

## Developer B Works on Product Search

Branch:

```text
feature/102-product-search
```

Changes:

```text
ProductController.java
SearchService.java
```

Commit:

```bash
git commit -m "feat: implement product search

Refs #102"
```

Both can work at the same time.

---

## Each Issue Gets Its Own PR

Authentication:

```text
feature/101-authentication

          ↓

Pull Request #201

          ↓

main
```

Search:

```text
feature/102-product-search

          ↓

Pull Request #202

          ↓

main
```

---

CI verifies independently:

```text
PR #201

Build ✔
Tests ✔
Security ✔


PR #202

Build ✔
Tests ✔
Security ✔
```

---

## Merge Order Does Not Matter (Mostly)

Example:

Merge authentication first:

```text
main

A---B---C---AUTH
```

Then merge search:

```text
main

A---B---C---AUTH---SEARCH
```

Both features arrive safely.

---

## What if Two Branches Change the Same File?

Example:

Branch A:

```text
feature/auth

changes:

User.java
```

Branch B:

```text
feature/profile

also changes:

User.java
```

Possible conflict:

```text
main

       ┌── auth change
       │

User.java

       │
       └── profile change
```

Git cannot decide automatically.

Result:

```text
Merge Conflict
```

Developer resolves manually.

---

## Keeping Long-Running Branch Updated

While you work:

Other PRs may enter main:

```text
main

A---B---C---D


feature/payment

A---B---X---Y
```

Update your branch:

Option 1:

```bash
git switch feature/payment

git merge main
```

or:

```bash
git rebase main
```

Now your branch includes latest changes.

---

## Real Professional Flow

```text
GitHub Issue #50

"Add Order API"

        ↓

Create Branch

feature/50-order-api

        ↓

Implement

        ↓

Commit

        ↓

Push

        ↓

Create PR

        ↓

GitHub Actions CI

        ↓

Review

        ↓

Merge

        ↓

Issue Closed
```

---

## Solo Developer Example

Even alone, feature branches help.

Instead of:

```text
main

login
payment
cart
bug fixes

(all mixed)
```

use:

```text
main

 ├── feature/login
 ├── feature/payment
 └── bugfix/cart-error
```

Benefits:

* pause work safely
* switch priorities
* rollback easier
* cleaner history

---

## Important Rule

Prefer:

```text
One Issue
    ↓
One Branch
    ↓
One Pull Request
```

Example:

```text
Issue #10 Authentication
        ↓
feature/10-authentication
        ↓
PR #55
        ↓
Merge
```

This creates traceability:

```text
Requirement
     ↓
Issue
     ↓
Branch
     ↓
Commit
     ↓
PR
     ↓
CI Evidence
     ↓
Merge
```

That is the foundation of parallel professional software development.

---

## See Also

- [topic-2.md](topic-2.md) — branch-switching mechanics and safety mid-workflow
- [topic-1.md](topic-1.md) — full PR workflow lifecycle for a single branch
- `.claude/rules/common/git-workflow.md` (this repo) — the real one-issue-one-branch convention
  this topic's "Important Rule" generalizes from
