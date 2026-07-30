# Git and GitHub FAQ (Beginner → Practical Developer Level)

25 beginner-to-practical questions covering the Git/GitHub fundamentals: repos, commits,
branches, remotes, merge/conflict, stash, and the Pull Request lifecycle.

## Table of Contents

1. [Are Git and GitHub the same?](#1-q-are-git-and-github-the-same)
2. [What is a repository?](#2-q-what-is-a-repository)
3. [What is a commit?](#3-q-what-is-a-commit)
4. [What is the difference between add and commit?](#4-q-what-is-the-difference-between-add-and-commit)
5. [Why do we need branches?](#5-q-why-do-we-need-branches)
6. [Can I switch between branches?](#6-q-can-i-switch-between-branches)
7. [What is main/master branch?](#7-q-what-is-mainmaster-branch)
8. [What is cloning?](#8-q-what-is-cloning)
9. [What is the difference between clone and pull?](#9-q-what-is-the-difference-between-clone-and-pull)
10. [What is origin?](#10-q-what-is-origin)
11. [What is push?](#11-q-what-is-push)
12. [What is pull?](#12-q-what-is-pull)
13. [What is fetch?](#13-q-what-is-fetch)
14. [Fetch vs Pull?](#14-q-fetch-vs-pull)
15. [What is merge?](#15-q-what-is-merge)
16. [What is a merge conflict?](#16-q-what-is-a-merge-conflict)
17. [What is stash?](#17-q-what-is-stash)
18. [What is a Pull Request (PR)?](#18-q-what-is-a-pull-request-pr)
19. [Why not directly push to main?](#19-q-why-not-directly-push-to-main)
20. [Who approves a PR?](#20-q-who-approves-a-pr)
21. [Can I update a PR after creating it?](#21-q-can-i-update-a-pr-after-creating-it)
22. [What happens after PR merge?](#22-q-what-happens-after-pr-merge)
23. [Should I delete branches after merge?](#23-q-should-i-delete-branches-after-merge)
24. [What is fork?](#24-q-what-is-fork)
25. [What is GitHub CLI?](#25-q-what-is-github-cli)
- [Core Mental Model](#core-mental-model)
- [See Also](#see-also)

---

### 1. Q: Are Git and GitHub the same?

**A: No.**

* **Git** = Version control system (tracks code history)
* **GitHub** = Online platform that hosts Git repositories

Analogy:

```text
Git      = Camera (takes snapshots)
GitHub   = Cloud album (stores and shares snapshots)
```

You can use Git without GitHub.

---

### 2. Q: What is a repository?

**A:** A repository (repo) is a project folder managed by Git.

It contains:

```text
Repository
│
├── Source Code
├── Configuration files
├── Documentation
└── .git
      └── Complete history
```

The `.git` directory stores commits, branches, and history.

---

### 3. Q: What is a commit?

**A:** A commit is a saved snapshot of your project at a point in time.

Example:

```bash
git add .
git commit -m "feat: add login feature"
```

Timeline:

```text
Commit A
   |
Commit B
   |
Commit C
```

You can return to previous commits.

---

### 4. Q: What is the difference between add and commit?

**A:**

`git add` prepares changes.

`git commit` permanently records them.

Flow:

```text
Working Directory

      |
      | git add
      ▼

Staging Area

      |
      | git commit
      ▼

Git Repository
```

---

### 5. Q: Why do we need branches?

**A:** Branches allow isolated development.

Example:

```text
main
 |
 A---B---C

      \
       D---E
        feature/login
```

You can build features without breaking production code.

---

### 6. Q: Can I switch between branches?

**A:** Yes.

```bash
git switch feature/payment
```

Return:

```bash
git switch main
```

Save unfinished work first:

```bash
git stash
```

or commit it.

---

### 7. Q: What is main/master branch?

**A:** It is usually the primary stable branch.

Typical:

```text
main
 |
 ├── Production-ready code
 ├── Tested code
 └── Released code
```

Modern repositories usually use `main`.

---

### 8. Q: What is cloning?

**A:** Cloning copies a remote repository to your computer.

GitHub:

```text
Remote Repository
        |
        |
        ▼
Local Computer
```

Command:

```bash
git clone <repository-url>
```

---

### 9. Q: What is the difference between clone and pull?

**A:**

`clone` = download first time

```bash
git clone repo-url
```

`pull` = update existing repo

```bash
git pull
```

Example:

```text
Day 1:
clone project

Day 2 onward:
pull latest changes
```

---

### 10. Q: What is origin?

**A:** `origin` is the default name for your remote GitHub repository.

Example:

```bash
git push origin main
```

Means:

```text
Send my main branch
        |
        ▼
GitHub repository
```

---

### 11. Q: What is push?

**A:** Push uploads local commits to GitHub.

```text
Local Git
    |
    | git push
    ▼
GitHub
```

Example:

```bash
git push origin feature/login
```

---

### 12. Q: What is pull?

**A:** Pull downloads changes from GitHub and merges them locally.

```text
GitHub
   |
   | git pull
   ▼
Local machine
```

Equivalent:

```text
git pull

=

git fetch
+
git merge
```

---

### 13. Q: What is fetch?

**A:** Fetch downloads information but does not modify your branch.

```bash
git fetch
```

Meaning:

```text
"Show me what changed,
but don't touch my files."
```

---

### 14. Q: Fetch vs Pull?

|                   | Fetch | Pull |
| ----------------- | ----- | ---- |
| Downloads updates | Yes   | Yes  |
| Changes files     | No    | Yes  |
| Safer preview     | Yes   | No   |
| Includes merge    | No    | Yes  |

---

### 15. Q: What is merge?

**A:** Merge combines branches.

Before:

```text
main

A---B

     \
      C---D feature
```

After:

```text
A---B-------M
     \     /
      C---D
```

---

### 16. Q: What is a merge conflict?

**A:** A conflict happens when Git cannot automatically combine changes.

Example:

Developer A:

```java
int price = 100;
```

Developer B:

```java
int price = 200;
```

Git asks:

```text
Which one should I keep?
```

You manually resolve it.

---

### 17. Q: What is stash?

**A:** Temporary storage for unfinished work.

Example:

```bash
git stash
```

Meaning:

```text
Current changes
      |
      ▼
Temporary shelf
```

Restore:

```bash
git stash pop
```

---

### 18. Q: What is a Pull Request (PR)?

**A:** A request to merge your branch into another branch after review.

Flow:

```text
feature branch

      |
      ▼

Pull Request

      |
      ▼

Review + Tests

      |
      ▼

main
```

---

### 19. Q: Why not directly push to main?

**A:** Because PR workflow provides:

* Code review
* Automated testing
* Discussion
* History tracking
* Quality control

Production projects protect `main`.

---

### 20. Q: Who approves a PR?

**A:** Usually:

* Team lead
* Senior developer
* Code owner
* Maintainer

For personal projects, you approve/merge yourself.

---

### 21. Q: Can I update a PR after creating it?

**A:** Yes.

Just push more commits:

```bash
git add .
git commit -m "fix review comments"
git push
```

Existing PR updates automatically.

---

### 22. Q: What happens after PR merge?

Before:

```text
main

A---B

feature

A---B---C
```

After:

```text
main

A---B---C
```

Feature becomes part of main.

---

### 23. Q: Should I delete branches after merge?

**A:** Usually yes.

Remote:

```bash
git push origin --delete feature/login
```

Local:

```bash
git branch -d feature/login
```

Keeps repository clean.

---

### 24. Q: What is fork?

**A:** A fork is your personal copy of another GitHub repository.

Common in open source:

```text
Original Project

        |
        ▼

Your Fork

        |
        ▼

Your Changes

        |
        ▼

Pull Request
```

---

### 25. Q: What is GitHub CLI?

**A:** GitHub CLI (`gh`) lets you control GitHub from terminal.

Examples:

Create PR:

```bash
gh pr create
```

Merge:

```bash
gh pr merge
```

View issues:

```bash
gh issue list
```

---

## Core Mental Model

```text
Edit Code
    |
    ▼
git add

    |
    ▼
git commit

    |
    ▼
git push

    |
    ▼
Pull Request

    |
    ▼
Review + CI

    |
    ▼
Merge

    |
    ▼
Production Branch
```

**Git manages history.
GitHub manages collaboration.
Pull Requests manage controlled change.**

---

## See Also

- [topic-1.md](topic-1.md) — full PR workflow lifecycle using GitHub CLI, worked example
- [topic-13.md](topic-13.md) — FAQ: GitHub professional workflow (Issue → Project → PR → CI → Merge)
- [topic-14.md](topic-14.md) — common Git/GitHub/GitHub Actions interview questions
- [topic-15.md](topic-15.md), [topic-16.md](topic-16.md), [topic-17.md](topic-17.md),
  [topic-18.md](topic-18.md) — deeper dives on specific FAQ items above (Pull vs Pull Request,
  PR without GitHub, Git merge vs GitHub merge, Push vs Pull Request)
