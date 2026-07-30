# Can a Pull Request Exist Without CI?

Whether PR and CI are independent mechanisms or must always be combined — with the reverse
question (CI without a PR) and why professional teams combine both anyway.

---

### Q: Can a Pull Request (PR) exist without CI?

**A: Yes. A PR can exist without CI.**

A Pull Request is mainly a **collaboration and change management mechanism**.

Example:

```text
Developer
    │
    ▼
Creates Branch

feature/login
    │
    ▼
Creates PR

"Please review and merge my changes"

    │

Manual Review Only

    │
    ▼

Merge
```

No automated pipeline is required.

Small projects often use:

* PR discussion
* Manual code review
* Manual testing
* Manual approval

without CI.

---

### Q: Can CI exist without Pull Requests?

**A: Yes. CI can exist without PRs.**

CI only needs a trigger/event.

Example:

```text
Developer

    │
    ▼

git push main

    │
    ▼

CI Pipeline Starts

    │
    ▼

Build
Test
Scan
Package
```

Example GitHub Actions:

```yaml
on:
  push:
    branches:
      - main
```

Meaning:

> "Run CI whenever someone pushes to main."

No PR involved.

---

### Q: Then why combine PR + CI?

Because together they create a **controlled feedback loop**.

Without CI:

```text
Pull Request
     │
     ▼
Human Review
     │
     ▼
Merge

Risk:
"Looks good but breaks system"
```

The reviewer checks design/style, but may miss runtime failures.

---

With PR + CI:

```text
Pull Request
      │
      ▼

Automated CI

      │
      ├── Compile
      ├── Unit Tests
      ├── Integration Tests
      ├── Security Scan
      └── Quality Checks

      │
      ▼

Reviewer Decision

      │
      ▼

Merge
```

Now the reviewer has evidence.

---

### Comparison

| Scenario          | Possible?           | Example                                |
| ----------------- | ------------------- | -------------------------------------- |
| PR without CI     | ✅ Yes               | Open-source project with manual review |
| CI without PR     | ✅ Yes               | Solo developer pushing directly        |
| PR + CI           | ✅ Yes (recommended) | Professional software teams            |
| Neither PR nor CI | ✅ Possible          | Small local experiments                |

---

### Real-world analogy

**PR without CI**

```text
Author submits a book chapter

Editor reads it manually

Approve
```

---

**CI without PR**

```text
Machine automatically checks every saved chapter

No editor involved
```

---

**PR + CI**

```text
Author submits chapter

        │

Spell checker
Grammar checker
Plagiarism checker

        │

Editor reviews

        │

Publish
```

---

Production-grade mental model:

```text
PR answers:
"Should humans accept this change?"

CI answers:
"Does the machine evidence prove this change works?"

Quality Gate answers:
"Do we allow this change into the protected system?"
```

They are independent systems, but together they form a reliable engineering control mechanism.

---

## See Also

- [topic-4.md](topic-4.md) — Testing → Quality Gate → CI → PR as a closed-loop system
- [topic-16.md](topic-16.md) — can a Pull Request exist without GitHub?
- [topic-1.md](topic-1.md) — full PR workflow lifecycle using GitHub CLI
