# GitHub Issue Creation With Label, Milestone, and Project Using GitHub CLI

A 9-step walkthrough of creating a fully-enriched issue — label, milestone, and project field
assignment — entirely via `gh`, then carrying it through implementation to merge.

## Table of Contents

1. [Check Existing Labels](#1-check-existing-labels)
2. [Create Required Labels (Optional)](#2-create-required-labels-optional)
3. [Check Existing Milestones](#3-check-existing-milestones)
4. [Create a Milestone](#4-create-a-milestone)
5. [Create Issue With Label + Milestone](#5-create-issue-with-label--milestone)
6. [Create Detailed Issue From Template File](#6-create-detailed-issue-from-template-file)
7. [Add Issue to GitHub Project](#7-add-issue-to-github-project)
8. [Change Project Status Field](#8-change-project-status-field)
9. [Full Professional Workflow](#9-full-professional-workflow)
- [Final Relationship Model](#final-relationship-model)
- [See Also](#see-also)

---

Complete relationship:

```text
GitHub Repository

        │

        ▼

Issue #25
"Implement Authentication"

        │

        ├── Labels
        │      └── feature, backend, priority-high
        │
        ├── Milestone
        │      └── v1.0 Release
        │
        └── Project
               └── Backend Development Board
```

Meaning:

* **Issue** → What work needs to be done?
* **Label** → What type/category is this work?
* **Milestone** → Which delivery target/release?
* **Project** → Where do we track workflow?

---

## 1. Check Existing Labels

```bash
gh label list
```

Example:

```text
bug
enhancement
documentation
priority-high
backend
security
```

---

## 2. Create Required Labels (Optional)

If labels do not exist:

```bash
gh label create backend \
  --description "Backend development work"
```

Priority label:

```bash
gh label create priority-high \
  --description "High priority task"
```

Bug label:

```bash
gh label create bug \
  --description "Defect or unexpected behavior"
```

---

## 3. Check Existing Milestones

GitHub CLI does not have a direct milestone command, so use API:

```bash
gh api repos/:owner/:repo/milestones
```

Example output:

```json
[
  {
    "title": "v1.0 Release"
  }
]
```

---

## 4. Create a Milestone

Example:

```bash
gh api \
repos/:owner/:repo/milestones \
-f title="v1.0 Release" \
-f description="Initial production release"
```

Now:

```text
Milestone:

v1.0 Release

      │

      ├── Issue #10
      ├── Issue #20
      └── Issue #30
```

---

## 5. Create Issue With Label + Milestone

Command:

```bash
gh issue create \
--title "feat: implement JWT authentication" \
--body "Implement secure login using JWT" \
--label backend \
--label security \
--label priority-high \
--milestone "v1.0 Release" \
--assignee @me
```

Result:

```text
Issue #25

Title:
feat: implement JWT authentication


Labels:
backend
security
priority-high


Milestone:
v1.0 Release


Assignee:
You
```

---

## 6. Create Detailed Issue From Template File

Create:

```bash
authentication.md
```

Content:

```markdown
## Problem

Application does not have authentication.

## Requirements

- Login API
- JWT generation
- Token validation
- Security configuration

## Acceptance Criteria

- Valid users receive token
- Invalid users get 401 response
- Tests pass
```

Create:

```bash
gh issue create \
--title "feat: authentication module" \
--body-file authentication.md \
--label backend,security \
--milestone "v1.0 Release" \
--assignee @me
```

---

## 7. Add Issue to GitHub Project

GitHub Projects use the `gh project` commands.

First list projects:

```bash
gh project list
```

Example:

```text
1   BuildNest Backend
```

---

View project:

```bash
gh project view 1
```

---

Add issue:

```bash
gh project item-add 1 \
--url https://github.com/user/repo/issues/25
```

Now:

```text
Project:

BuildNest Backend

TODO

#25 JWT Authentication


IN PROGRESS


DONE
```

---

## 8. Change Project Status Field

Find project fields:

```bash
gh project field-list 1
```

Example:

```text
Status
Priority
Sprint
```

Set:

```text
Status = Todo
Priority = High
Sprint = Sprint 1
```

Now project tracks:

```text
Issue #25

Status:
Todo

Priority:
High

Sprint:
Sprint 1

Milestone:
v1.0
```

---

## 9. Full Professional Workflow

Create planning objects:

```text
Milestone:
v1.0 Release

Project:
BuildNest Backend

Labels:
backend
feature
priority-high
```

Create issue:

```bash
gh issue create \
--title "feat: create order service" \
--body-file order.md \
--label backend,feature,priority-high \
--milestone "v1.0 Release" \
--assignee @me
```

Add to project:

```bash
gh project item-add 1 \
--url <issue-url>
```

Develop:

```bash
git switch -c feature/issue-25-order-service
```

Commit:

```bash
git commit -m "feat: implement order service

Refs #25"
```

PR:

```bash
gh pr create \
--title "feat: order service" \
--body "Closes #25"
```

Merge:

```bash
gh pr merge --squash --delete-branch
```

---

## Final Relationship Model

```text
Product Goal
     │
     ▼
Milestone
"When are we delivering?"

     │

     ▼
Epic
"What capability?"

     │

     ▼
Issue
"What exact work?"

     │

     ├── Label
     │     "What type?"
     │
     ├── Project
     │     "Where is it tracked?"
     │
     └── Sprint
           "When are we doing it?"


Implementation:

Issue
 ↓
Branch
 ↓
Commit
 ↓
PR
 ↓
CI
 ↓
Merge
 ↓
Done
```

A production-style GitHub workflow usually treats **Issue + Label + Milestone + Project fields** as the planning layer, and **Branch + PR + CI + Merge** as the engineering execution layer.

---

## See Also

- [topic-8.md](topic-8.md) — simpler issue-creation walkthrough (without milestone/project)
- [topic-7.md](topic-7.md) — Milestone vs. Epic vs. Sprint
- [topic-10.md](topic-10.md) — two-layer mental model this file's closing model previews
