# Difference Between Git Merge and GitHub Merge

`git merge` (a local Git command combining branches) vs. GitHub merge (a platform workflow that
merges an approved, reviewed, CI-checked Pull Request) — the mechanism vs. the governance
around it.

## Table of Contents

1. [Git Merge](#1-git-merge)
2. [GitHub Merge](#2-github-merge)
- [Important Relationship](#important-relationship)
- [Example Workflow Difference](#example-workflow-difference)
- [Comparison Table](#comparison-table)
- [Merge Types in GitHub](#merge-types-in-github)
- [When to Use Which?](#when-to-use-which)
- [Final Mental Model](#final-mental-model)
- [See Also](#see-also)

---

## Q: What is the difference between **Git merge** and **GitHub merge**?

**Beginner answer:**

Both combine code changes, but they happen at different layers.

* **`git merge` = Local Git operation to combine branches**
* **GitHub merge = Platform workflow that merges an approved Pull Request**

```text
Git merge
=
"Combine these branches"


GitHub merge
=
"After review and checks, accept this PR"
```

---

## 1. Git Merge

`git merge` is a **Git command**.

It works locally on your machine.

Example:

You have:

```text
main

A---B---C


feature/login

A---B---C---D---E
```

You switch to `main`:

```bash
git switch main
```

Merge feature branch:

```bash
git merge feature/login
```

Result:

```text
main

A---B---C-------M
         \     /
          D---E
```

Now feature code is inside `main`.

---

Git only performs:

```text
Take commits

      ↓

Combine branches

      ↓

Update history
```

Git does **not** know about:

* Pull Requests
* Review approval
* GitHub Issues
* CI checks
* Branch protection

---

## 2. GitHub Merge

GitHub merge happens through a **Pull Request**.

Example:

Developer creates:

```text
feature/login

        │

        ▼

Pull Request #50

        │

        ▼

main
```

Before merge GitHub can enforce:

```text
Pull Request

      │

      ├── Code Review ✔
      │
      ├── GitHub Actions CI ✔
      │
      ├── Security Scan ✔
      │
      ├── Required Approval ✔
      │
      └── Branch Rules ✔


      │

      ▼


Merge Button Enabled
```

Then:

```text
Click "Merge"

or

gh pr merge
```

GitHub performs the Git merge internally.

---

## Important Relationship

GitHub merge uses Git internally.

```text
GitHub Merge

      ↓

Runs a Git operation

      ↓

Updates repository history
```

GitHub is adding a management layer.

---

## Example Workflow Difference

### Using only Git merge

Developer:

```bash
git switch main

git merge feature/payment

git push origin main
```

Flow:

```text
Feature Branch

       ↓

git merge

       ↓

main
```

Fast but no controls.

---

### Using GitHub PR merge

Developer:

```bash
git push feature/payment
```

Create PR:

```bash
gh pr create
```

Then:

```text
Pull Request

      ↓

Reviewer checks

      ↓

CI tests

      ↓

Approval

      ↓

Merge
```

More controlled.

---

## Comparison Table

| Feature           | Git Merge     | GitHub Merge    |
| ----------------- | ------------- | --------------- |
| Belongs to        | Git           | GitHub          |
| Location          | Usually local | Remote platform |
| Requires PR       | ❌ No          | ✅ Usually       |
| Code review       | ❌ No          | ✅ Yes           |
| CI integration    | ❌ No          | ✅ Yes           |
| Branch protection | ❌ No          | ✅ Yes           |
| Updates history   | ✅ Yes         | ✅ Yes           |
| Underlying engine | Git           | Git             |

---

## Merge Types in GitHub

GitHub gives three options:

```text
Pull Request

      │

      ├── Merge Commit
      │
      ├── Squash Merge
      │
      └── Rebase Merge
```

---

### 1. Create Merge Commit

Equivalent to:

```bash
git merge feature
```

History:

```text
A---B------M
     \    /
      C--D
```

Keeps complete history.

---

### 2. Squash Merge

Equivalent idea:

```text
Many commits → One commit
```

Before:

```text
feature:

A
B
C
D
```

After:

```text
main:

S
```

Common in professional teams.

---

### 3. Rebase Merge

Creates linear history:

Before:

```text
main

A---B


feature

     C---D
```

After:

```text
A---B---C---D
```

---

## When to Use Which?

### Personal/local work

Usually:

```bash
git merge
```

Example:

* experiments
* local branches
* small projects

---

### Team production work

Usually:

```text
GitHub Pull Request Merge
```

Because you get:

```text
Issue Tracking

      ↓

PR

      ↓

Review

      ↓

CI

      ↓

Quality Gate

      ↓

Merge
```

---

## Final Mental Model

```text
Git merge
=
Mechanism


GitHub merge
=
Controlled process
```

or:

```text
Git
=
"Can these branches technically combine?"


GitHub
=
"Should these branches be allowed to combine?"
```

Git provides the engine.
GitHub provides the governance around the engine.

---

## See Also

- [topic-1.md](topic-1.md) — `gh pr merge` options (merge/squash/rebase) in the full PR lifecycle
- [topic-15.md](topic-15.md) — difference between Pull and Pull Request
- [topic-18.md](topic-18.md) — difference between Git Push and Pull Request
