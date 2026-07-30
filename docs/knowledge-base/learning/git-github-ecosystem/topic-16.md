# Can a Pull Request Exist Without GitHub?

Whether "Pull Request" is a Git feature or a platform-layer convention built on top of Git — and
what the equivalent concept is called on GitLab, Bitbucket, Azure DevOps, and Gerrit.

---

## Q: Can a Pull Request (PR) exist without GitHub?

**A: Yes — the concept can exist without GitHub, but the exact feature name may differ.**

A Pull Request is **not a Git feature**. It is a **collaboration workflow built on top of Git** by hosting platforms.

Git itself only knows:

```text
Git

✔ Branch
✔ Commit
✔ Merge
✔ Rebase
✔ Push
✔ Pull

❌ Pull Request
```

---

## Git Alone (No PR)

With only Git:

Developer creates branch:

```text
main

A---B---C

        \
         D---E

      feature/login
```

Then manually merges:

```bash
git checkout main

git merge feature/login
```

Result:

```text
main

A---B---C---M
        \   /
         D-E
```

Git does not ask for:

* Code review
* Approval
* Comments
* CI checks

It just merges.

---

## GitHub Adds Pull Requests

GitHub adds a control layer:

```text
Feature Branch

       │

       ▼

Pull Request

       │

       ├── Code Review
       ├── Discussion
       ├── CI Checks
       ├── Approval
       └── Merge


       ▼

main
```

A PR asks:

> "Should this branch be merged?"

---

## Other Platforms Have Similar Concepts

Pull Request is GitHub terminology, but other tools have equivalents:

| Platform     | Equivalent    |
| ------------ | ------------- |
| GitHub       | Pull Request  |
| Bitbucket    | Pull Request  |
| Azure DevOps | Pull Request  |
| GitLab       | Merge Request |
| Gerrit       | Change Review |

Same idea:

```text
Propose Change

      ↓

Review

      ↓

Verify

      ↓

Merge
```

---

## Can GitHub PR exist without Git?

Usually **no**.

Because PR depends on Git concepts:

```text
Commit
   ↓
Branch
   ↓
Compare Changes
   ↓
Merge Request
```

A PR needs something to compare:

```text
source branch

vs

target branch
```

Example:

```text
feature/payment

        ↓ PR

main
```

---

## Relationship

```text
Git
(Foundation)

     │

     ▼

Branch + Commit + Merge

     │

     ▼

GitHub / GitLab / Bitbucket

     │

     ▼

Pull Request Workflow
```

---

## Interview Answer

**Q: Is Pull Request a Git command?**

**A:**

> No. Pull Request is not part of Git. Git provides the underlying version control features like branches, commits, and merges. Platforms such as GitHub build Pull Requests on top of Git to enable code review, discussion, automated checks, and controlled merging.

---

Mental shortcut:

```text
Git
=
Can merge code


Pull Request
=
Can discuss, verify, approve, then merge code
```

Git is the engine.
A Pull Request is the traffic-control system around the engine.

---

## See Also

- [topic-5.md](topic-5.md) — can a Pull Request exist without CI?
- [topic-15.md](topic-15.md) — difference between Pull and Pull Request
- [topic-17.md](topic-17.md) — difference between Git merge and GitHub merge
