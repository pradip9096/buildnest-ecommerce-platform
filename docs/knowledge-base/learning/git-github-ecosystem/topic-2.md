# Can You Switch Branches Mid-Work and Come Back Later?

Branch-switching mechanics and safety — what happens to uncommitted work when you jump between
branches, and the three safe ways to handle it.

---

**Q: Can I jump from one branch to another branch and come back later?**

**A: Yes.** In Git, switching between branches is normal. You can move from one branch to another and return whenever you want.

Think of branches like different workspaces:

```text
main
 │
 ├── feature/login
 │        ↑
 │     working here
 │
 └── bugfix/payment
```

You can leave `feature/login`, go to `bugfix/payment`, and come back.

---

Example:

Current branch:

```bash
git branch
```

Output:

```text
main
* feature/login
bugfix/payment
```

`*` means your current branch.

---

Switch to another branch:

```bash
git switch bugfix/payment
```

Now:

```text
main
 │
 ├── feature/login
 │
 └── bugfix/payment
          ↑
       you are here
```

Later return:

```bash
git switch feature/login
```

Back again:

```text
main
 │
 ├── feature/login
 │        ↑
 │     you are here
 │
 └── bugfix/payment
```

---

Important condition:

Before switching, Git checks your working directory.

If everything is committed:

```bash
git status
```

Example:

```text
nothing to commit, working tree clean
```

Safe:

```bash
git switch another-branch
```

---

If you have uncommitted changes:

```text
feature/login

modified:
  AuthService.java
```

and run:

```bash
git switch bugfix/payment
```

Git may say:

```text
error: Your local changes would be overwritten by checkout
```

because switching could destroy your changes.

You have three choices.

---

### Option 1: Commit before switching (recommended)

Save progress:

```bash
git add .
git commit -m "wip: save login progress"
```

Switch:

```bash
git switch bugfix/payment
```

Later:

```bash
git switch feature/login
```

Your work remains.

---

### Option 2: Temporarily store changes using stash

If work is incomplete:

```bash
git stash
```

Now switch:

```bash
git switch bugfix/payment
```

Later return:

```bash
git switch feature/login
```

Restore:

```bash
git stash pop
```

---

### Option 3: Discard changes

Only if you don't need them:

```bash
git restore .
```

Then:

```bash
git switch bugfix/payment
```

---

Typical developer workflow:

```text
feature/payment
      |
      | urgent bug arrives
      ▼

git stash

      |
      ▼

bugfix/security

      |
      | fix + commit + PR
      ▼

feature/payment

      |
      ▼

git stash pop

continue work
```

---

Useful branch navigation commands:

| Action                 | Command                    |
| ---------------------- | -------------------------- |
| Show branches          | `git branch`               |
| Switch branch          | `git switch branch-name`   |
| Return previous branch | `git switch -`             |
| Create + switch        | `git switch -c new-branch` |
| See current branch     | `git status`               |

Rule of thumb:

> **Branches are cheap and movable. Jumping between them is safe as long as your current work is saved by either a commit or stash before switching.**

---

## See Also

- [topic-1.md](topic-1.md) — full PR workflow lifecycle this branch-switching fits into
- [topic-20.md](topic-20.md) — feature branching for parallel, independent issue development
- `docs/wiki/learned-lessons/` — this repo's own git-workflow gotchas (see
  `lessons_git_workflow_and_commit_hygiene.md` in project auto-memory) around `git checkout`
  silently overwriting a working tree
