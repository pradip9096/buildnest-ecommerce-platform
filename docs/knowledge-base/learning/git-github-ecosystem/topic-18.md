# Difference Between Git Push and Pull Request

`git push` (transports commits to a remote) vs. Pull Request (requests review/approval before
those commits merge) — transport vs. approval process.

## Table of Contents

1. [What is Git Push?](#1-what-is-git-push)
2. [What is Pull Request?](#2-what-is-pull-request)
- [Complete Developer Flow](#complete-developer-flow)
- [Push Without PR](#push-without-pr)
- [Push + PR Workflow (Professional)](#push--pr-workflow-professional)
- [Comparison](#comparison)
- [Push vs PR vs Merge Relationship](#push-vs-pr-vs-merge-relationship)
- [Interview Answer](#interview-answer)
- [See Also](#see-also)

---

## Q: What is the difference between **Git Push** and **Pull Request (PR)?**

**Beginner answer:**

* **`git push` = Upload my commits to a remote repository**
* **Pull Request = Ask permission to merge my uploaded commits into another branch**

```text
git push
=
"Here is my code"


Pull Request
=
"Please review and accept my code"
```

---

## 1. What is Git Push?

`git push` is a **Git command**.

It transfers commits from your local repository to a remote repository like GitHub.

Flow:

```text
Developer Laptop

feature/login

A---B---C

      │

      │ git push

      ▼

GitHub

feature/login

A---B---C
```

Command:

```bash
git push origin feature/login
```

Meaning:

> Upload my `feature/login` branch commits to GitHub.

---

After push:

GitHub has your code:

```text
GitHub Repository


main

A---B


feature/login

A---B---C
```

But `main` is not changed yet.

Your feature exists separately.

---

## 2. What is Pull Request?

A Pull Request is a **GitHub workflow**.

It asks:

> "Can my feature branch be merged into the main branch?"

Example:

```text
feature/login

A---B---C

        │

        │ Pull Request

        ▼

main

A---B
```

PR provides:

* Code review
* Discussion
* CI testing
* Approval
* Controlled merge

---

## Complete Developer Flow

You create a branch:

```bash
git switch -c feature/payment
```

Write code.

Commit:

```bash
git add .

git commit -m "feat: add payment API"
```

Now:

```text
Local Machine


feature/payment

A---B---C
```

---

Upload it:

```bash
git push origin feature/payment
```

Now:

```text
GitHub


feature/payment exists


BUT


main unchanged
```

---

Create PR:

```bash
gh pr create \
--base main \
--head feature/payment
```

Now:

```text
Pull Request

feature/payment

        ↓

Review
CI Tests
Approval

        ↓

main
```

---

Merge PR:

```text
main

A---B---C
```

Feature is officially added.

---

## Push Without PR

Possible:

```bash
git push origin main
```

Flow:

```text
Developer

    │

    ▼

main updated directly
```

Example:

```text
Before:

main:
A---B


After push:

main:
A---B---C
```

Fast, but:

* No review
* No approval
* May bypass testing

Common for:

* Personal projects
* Experiments

---

## Push + PR Workflow (Professional)

```text
Developer

    │

    ▼

Feature Branch

    │

git push

    │

    ▼

GitHub Branch

    │

Create PR

    │

    ▼

Review

    │

CI Pipeline

    │

Quality Gate

    │

Merge

    ▼

main
```

---

## Comparison

| Question               | Push            | Pull Request   |
| ---------------------- | --------------- | -------------- |
| Tool                   | Git             | GitHub         |
| Purpose                | Upload commits  | Request merge  |
| Moves code?            | Yes             | Controls merge |
| Needs review?          | No              | Usually yes    |
| Runs CI?               | Not necessarily | Usually yes    |
| Changes main directly? | Can             | After approval |
| Command example        | `git push`      | `gh pr create` |

---

## Push vs PR vs Merge Relationship

```text
1. Commit

"I saved my work"

        ↓

2. Push

"I uploaded my work"

        ↓

3. Pull Request

"Please verify my work"

        ↓

4. Merge

"My work is accepted"
```

---

## Interview Answer

**Q: Difference between git push and Pull Request?**

**A:**

> `git push` is a Git operation that uploads local commits to a remote repository. A Pull Request is a collaboration process provided by platforms like GitHub that allows those pushed changes to be reviewed, tested, and approved before merging into a target branch.

---

Short mental model:

```text
Push
=
Transport


PR
=
Approval Process


Merge
=
Integration
```

`push` delivers the code.
`PR` decides whether the delivered code should become official.

---

## See Also

- [topic-15.md](topic-15.md) — difference between Pull and Pull Request
- [topic-17.md](topic-17.md) — difference between Git merge and GitHub merge
- [topic-1.md](topic-1.md) — full PR workflow lifecycle using GitHub CLI
