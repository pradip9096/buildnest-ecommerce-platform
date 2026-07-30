# Full PR Workflow Lifecycle Using GitHub CLI (`gh`)

A typical **Pull Request (PR) workflow using GitHub CLI (`gh`)** — branch, commit, push, PR
create/review/merge, cleanup — walked through as 15 numbered steps with a complete reusable
command sequence at the end.

## Table of Contents

1. [Verify Repository State](#1-verify-repository-state)
2. [Create a Feature Branch](#2-create-a-feature-branch)
3. [Implement Changes](#3-implement-changes)
4. [Stage Changes](#4-stage-changes)
5. [Commit Changes](#5-commit-changes)
6. [Push Branch to GitHub](#6-push-branch-to-github)
7. [Create Pull Request Using GitHub CLI](#7-create-pull-request-using-github-cli)
8. [View Pull Request](#8-view-pull-request)
9. [Review Changes Before Merge](#9-review-changes-before-merge)
10. [Request Review (Team Workflow)](#10-request-review-team-workflow)
11. [Update PR if Changes Requested](#11-update-pr-if-changes-requested)
12. [Merge Pull Request Using GitHub CLI](#12-merge-pull-request-using-github-cli)
13. [Auto Delete Branch After Merge](#13-auto-delete-branch-after-merge)
14. [Update Local Main](#14-update-local-main)
15. [Verify Final State](#15-verify-final-state)
- [Complete Command Sequence (Most Common)](#complete-command-sequence-most-common)
- [See Also](#see-also)

---

**Idea → Issue → Branch → Implementation → Commit → Push → PR Creation → Review → Checks → Merge → Cleanup**

```text
GitHub Issue
     │
     ▼
Create Branch
     │
     ▼
Implement Changes
     │
     ▼
Commit Locally
     │
     ▼
Push Branch
     │
     ▼
Create Pull Request (gh pr create)
     │
     ▼
Review + CI Checks
     │
     ▼
Merge Pull Request (gh pr merge)
     │
     ▼
Delete Branch / Cleanup
```

---

## 1. Verify Repository State

Before starting:

```bash
git status
```

Example:

```text
On branch main
nothing to commit, working tree clean
```

Make sure you are synchronized:

```bash
git checkout main
git pull origin main
```

Purpose:

* Start from latest code
* Avoid unnecessary merge conflicts

---

## 2. Create a Feature Branch

Never directly work on `main`.

Pattern:

```text
feature/<description>
bugfix/<description>
hotfix/<description>
```

Example:

```bash
git checkout -b feature/add-user-authentication
```

Now:

```text
main
 │
 └── feature/add-user-authentication
```

---

## 3. Implement Changes

Example:

```text
src/
 └── auth/
      ├── LoginController.java
      ├── AuthService.java
      └── JwtProvider.java
```

Check modified files:

```bash
git status
```

Example:

```text
modified:
  AuthService.java
  LoginController.java
```

---

## 4. Stage Changes

Add selected files:

```bash
git add src/auth/AuthService.java
```

or all changes:

```bash
git add .
```

Verify:

```bash
git status
```

---

## 5. Commit Changes

Commit with meaningful message:

```bash
git commit -m "feat: add user authentication service"
```

Common convention:

```text
feat:     new feature
fix:      bug fix
docs:     documentation
test:     testing changes
refactor: code improvement
```

---

## 6. Push Branch to GitHub

First push:

```bash
git push -u origin feature/add-user-authentication
```

Now GitHub has:

```text
Remote Repository

main
 │
 └── feature/add-user-authentication
```

---

## 7. Create Pull Request Using GitHub CLI

Check authentication:

```bash
gh auth status
```

Create PR:

```bash
gh pr create
```

Interactive mode:

```text
? Where should we merge?
> main

? Title
> feat: add user authentication

? Body
> Implements JWT authentication service
```

You can also create directly:

```bash
gh pr create \
  --base main \
  --head feature/add-user-authentication \
  --title "feat: add user authentication" \
  --body "Adds authentication service with JWT support"
```

Meaning:

```text
--base = target branch
--head = source branch
```

Visualization:

```text
feature branch
       |
       | Pull Request
       ▼

      main
```

---

## 8. View Pull Request

List PRs:

```bash
gh pr list
```

Example:

```text
#12 feat: add user authentication
```

View details:

```bash
gh pr view 12
```

Open in browser:

```bash
gh pr view --web
```

---

## 9. Review Changes Before Merge

View diff:

```bash
gh pr diff 12
```

Output:

```diff
+ public String generateToken()
- oldLogin()
```

Check CI status:

```bash
gh pr checks 12
```

Example:

```text
build      pass
tests      pass
security   pass
```

---

## 10. Request Review (Team Workflow)

Add reviewer:

```bash
gh pr edit 12 --add-reviewer username
```

Reviewer can approve:

```bash
gh pr review 12 --approve
```

or request changes:

```bash
gh pr review 12 --request-changes \
--body "Need additional tests"
```

---

## 11. Update PR if Changes Requested

Modify code.

Then:

```bash
git add .
```

```bash
git commit -m "fix: address review comments"
```

Push:

```bash
git push
```

No new PR needed.

The existing PR updates automatically.

Flow:

```text
Commit 1
   +
Commit 2
   +
Commit 3

      ↓

Same Pull Request
```

---

## 12. Merge Pull Request Using GitHub CLI

After approval:

```bash
gh pr merge 12
```

Options appear:

```text
? Merge method

> Create a merge commit
  Squash and merge
  Rebase and merge
```

### Option A: Merge Commit

```bash
gh pr merge 12 --merge
```

History:

```text
A---B---C main
     \
      D---E feature
            \
             M
```

Keeps full branch history.

### Option B: Squash Merge (Common)

```bash
gh pr merge 12 --squash
```

History:

```text
A---B---C---S main
```

Multiple commits become one.

Common for:

* GitHub projects
* Open source
* Clean history

### Option C: Rebase Merge

```bash
gh pr merge 12 --rebase
```

History:

```text
A---B---C---D---E
```

Linear history.

---

## 13. Auto Delete Branch After Merge

Recommended:

```bash
gh pr merge 12 --squash --delete-branch
```

Does:

```text
Merge PR
   +
Delete remote feature branch
```

---

## 14. Update Local Main

Return:

```bash
git checkout main
```

Pull merged code:

```bash
git pull origin main
```

Delete local branch:

```bash
git branch -d feature/add-user-authentication
```

---

## 15. Verify Final State

```bash
git branch
```

Output:

```text
* main
```

Check history:

```bash
git log --oneline
```

Example:

```text
abc123 feat: add user authentication
789abc initial project
```

---

## Complete Command Sequence (Most Common)

```bash
# update main
git checkout main
git pull origin main

# create work branch
git checkout -b feature/my-feature

# work...

# commit
git add .
git commit -m "feat: implement feature"

# push
git push -u origin feature/my-feature

# create PR
gh pr create \
 --base main \
 --title "feat: implement feature" \
 --body "Feature implementation"

# check
gh pr checks
gh pr diff

# merge
gh pr merge --squash --delete-branch

# update local
git checkout main
git pull
```

Reusable mental model:

> **Git handles code movement. GitHub PR handles collaboration, verification, and approval. GitHub CLI is simply the terminal interface for the GitHub PR lifecycle.**

---

## See Also

- [topic-2.md](topic-2.md) — branch-switching mechanics and safety mid-workflow
- [topic-4.md](topic-4.md) — how Testing → Quality Gate → CI → PR fit together as a closed-loop system
- [topic-5.md](topic-5.md) — whether a PR can exist without CI
- [topic-12.md](topic-12.md) — CI with Pull Requests using GitHub Actions, end-to-end
