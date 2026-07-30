# GitHub Issue Creation Walkthrough Using GitHub CLI (`gh`)

A 15-step walkthrough of creating, enriching, and linking a GitHub issue through to PR merge and
closure, entirely from the terminal via `gh`.

## Table of Contents

1. [Verify GitHub CLI Setup](#1-verify-github-cli-setup)
2. [Create a Simple Issue](#2-create-a-simple-issue)
3. [Create Issue Directly From Terminal](#3-create-issue-directly-from-terminal)
4. [Create a High-Quality Issue](#4-create-a-high-quality-issue)
5. [Create Issue From Markdown File (Recommended)](#5-create-issue-from-markdown-file-recommended)
6. [Add Labels](#6-add-labels)
7. [Assign Developer](#7-assign-developer)
8. [Attach Issue to Milestone](#8-attach-issue-to-milestone)
9. [View Issues](#9-view-issues)
10. [Edit Existing Issue](#10-edit-existing-issue)
11. [Create Branch From Issue](#11-create-branch-from-issue)
12. [Link Commit With Issue](#12-link-commit-with-issue)
13. [Create PR That Closes Issue](#13-create-pr-that-closes-issue)
14. [Close Issue Manually](#14-close-issue-manually)
15. [Complete Professional Workflow](#15-complete-professional-workflow)
- [Mental Model](#mental-model)
- [See Also](#see-also)

---

Complete lifecycle:

```text
Requirement / Problem
        │
        ▼
Create GitHub Issue
        │
        ▼
Add Metadata
(Label, Assignee, Milestone)
        │
        ▼
Add to GitHub Project
        │
        ▼
Create Branch
        │
        ▼
Implementation
        │
        ▼
Pull Request
        │
        ▼
Close Issue
```

---

## 1. Verify GitHub CLI Setup

Check authentication:

```bash
gh auth status
```

Example:

```text
github.com
✓ Logged in
```

Check repository:

```bash
gh repo view
```

Example:

```text
BuildNest/backend
```

---

## 2. Create a Simple Issue

Command:

```bash
gh issue create
```

Interactive mode:

```text
? Title:
> Add user authentication API

? Body:
> Implement JWT based login functionality.

? What's next?
> Submit
```

Result:

```text
Created issue #25
```

---

## 3. Create Issue Directly From Terminal

```bash
gh issue create \
  --title "feat: add user authentication" \
  --body "Implement JWT authentication system"
```

Creates:

```text
Issue #25

Title:
feat: add user authentication

Description:
Implement JWT authentication system
```

---

## 4. Create a High-Quality Issue

A professional issue contains:

```text
Context
Problem
Expected Solution
Requirements
Acceptance Criteria
Verification Criteria
```

Example:

```bash
gh issue create \
--title "feat: implement JWT authentication" \
--body "
## Problem

Users need secure authentication.

## Requirements

- Implement login endpoint
- Generate JWT token
- Validate credentials
- Handle invalid login

## Acceptance Criteria

- User receives token after login
- Invalid login returns 401
- Unit tests added
"
```

---

## 5. Create Issue From Markdown File (Recommended)

Create:

```bash
touch auth-issue.md
```

Content:

```markdown
## Problem

Authentication is missing.

## Requirements

- Add JWT support
- Create login API
- Secure endpoints

## Acceptance Criteria

- Login works
- Tests pass
- Documentation updated
```

Create issue:

```bash
gh issue create \
--title "feat: add authentication module" \
--body-file auth-issue.md
```

Cleaner for large issues.

---

## 6. Add Labels

Example:

```bash
gh issue create \
--title "Fix payment timeout" \
--label bug
```

Multiple labels:

```bash
gh issue create \
--title "Add cart API" \
--label feature,backend
```

Example:

```text
Issue #30

Labels:
feature
backend
priority-high
```

---

## 7. Assign Developer

Assign yourself:

```bash
gh issue create \
--title "Create product API" \
--assignee @me
```

Assign someone:

```bash
gh issue create \
--title "Fix bug" \
--assignee username
```

---

## 8. Attach Issue to Milestone

First view milestones:

```bash
gh api repos/:owner/:repo/milestones
```

Create issue:

```bash
gh issue create \
--title "Implement checkout" \
--milestone "v1.0 Release"
```

Meaning:

```text
Milestone:

v1.0 Release

      │

      ├── Authentication Issue
      ├── Product Issue
      └── Checkout Issue
```

---

## 9. View Issues

List all:

```bash
gh issue list
```

Example:

```text
#25 Authentication API   open
#26 Payment Bug          open
```

---

View one:

```bash
gh issue view 25
```

Open browser:

```bash
gh issue view 25 --web
```

---

## 10. Edit Existing Issue

Change title:

```bash
gh issue edit 25 \
--title "feat: implement JWT authentication"
```

Add label:

```bash
gh issue edit 25 \
--add-label priority-high
```

Assign:

```bash
gh issue edit 25 \
--add-assignee @me
```

---

## 11. Create Branch From Issue

Issue:

```text
#25 Add authentication
```

Create branch:

```bash
git switch -c feature/25-authentication
```

Now relationship:

```text
Issue #25

      │

      ▼

feature/25-authentication

      │

      ▼

Implementation
```

---

## 12. Link Commit With Issue

Commit:

```bash
git commit -m "feat: implement JWT authentication #25"
```

Better:

```bash
git commit -m "feat: implement authentication

Refs #25"
```

Means:

> This commit relates to Issue #25

---

## 13. Create PR That Closes Issue

After implementation:

```bash
git push -u origin feature/25-authentication
```

Create PR:

```bash
gh pr create \
--title "feat: implement authentication" \
--body "Closes #25"
```

Important keyword:

```text
Closes #25
Fixes #25
Resolves #25
```

After PR merge:

```text
PR merged

      │

      ▼

Issue #25 automatically closed
```

---

## 14. Close Issue Manually

If needed:

```bash
gh issue close 25
```

With comment:

```bash
gh issue close 25 \
--comment "Completed with authentication PR"
```

---

## 15. Complete Professional Workflow

```bash
# create issue
gh issue create \
--title "feat: add order service" \
--label enhancement \
--assignee @me


# create branch
git switch -c feature/issue-45-order-service


# implement


# commit
git add .

git commit -m "feat: add order service

Refs #45"


# push
git push -u origin feature/issue-45-order-service


# create PR
gh pr create \
--base main \
--title "feat: add order service" \
--body "Closes #45"


# CI passes + review


# merge
gh pr merge --squash --delete-branch
```

---

## Mental Model

```text
Issue
= Define the work


Branch
= Isolate implementation


Commit
= Record progress


PR
= Request integration


CI
= Verify quality


Merge
= Deliver change


Close Issue
= Work completed
```

In professional workflows, **every meaningful code change usually starts with an Issue and ends when a linked PR is merged.**

---

## See Also

- [topic-9.md](topic-9.md) — issue creation with label, milestone, and project together via `gh`
- [topic-1.md](topic-1.md) — full PR workflow lifecycle this issue flow feeds into
- [topic-11.md](topic-11.md) — worked example applying this walkthrough to a real BuildNest feature
