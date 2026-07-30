# Difference Between Pull and Pull Request

`git pull` (a Git command that downloads changes) vs. a Pull Request (a GitHub feature that
requests review and merge) — two similarly-named but unrelated operations.

---

## Q: What is the difference between **Pull** and **Pull Request**?

**Beginner answer:**

They sound similar, but they solve completely different problems.

* **`git pull` = Get changes from another place**
* **Pull Request (PR) = Ask to put your changes into another place**

---

## 1. What is `git pull`?

`git pull` is a **Git command**.

It downloads the latest code from a remote repository (like GitHub) into your local computer.

It answers:

> "Give me the latest changes."

Example:

Your computer:

```text
Local Repository

main

A---B
```

GitHub has newer code:

```text
GitHub Repository

main

A---B---C---D
```

Run:

```bash
git pull origin main
```

After pull:

```text
Local Repository

main

A---B---C---D
```

Your local code is updated.

---

## 2. What is a Pull Request (PR)?

A Pull Request is a **GitHub collaboration feature**.

It means:

> "I finished my changes. Please review them and merge them."

Example:

You create a feature:

```text
main

A---B

     \
      C---D

 feature/login
```

Now you want:

```text
feature/login
        |
        |
        ▼
      main
```

You create:

```text
Pull Request:

"Please merge my login feature into main."
```

Team reviews:

```text
Pull Request

      ↓

Code Review

      ↓

CI Tests

      ↓

Approval

      ↓

Merge into main
```

---

## Simple Analogy

Imagine writing a shared document.

### `git pull`

Means:

> "Give me the latest copy of the document."

Example:

Your teammate updated the document yesterday.

You download those updates.

---

### Pull Request

Means:

> "I edited a chapter. Please check my changes and add them to the official document."

Someone reviews before accepting.

---

## Side-by-side Comparison

| Question       | git pull          | Pull Request       |
| -------------- | ----------------- | ------------------ |
| What is it?    | Git command       | GitHub feature     |
| Direction      | Receive changes   | Submit changes     |
| Purpose        | Update your code  | Merge your work    |
| Used by        | Developer locally | Team collaboration |
| Review needed? | No                | Usually yes        |
| Runs CI?       | No                | Often yes          |

---

## Direction Difference

`git pull`:

```text
GitHub
   |
   | download
   ↓

Your Laptop
```

Pull Request:

```text
Your Feature Branch
          |
          | request merge
          ↓

Main Branch
```

---

## Typical Developer Flow

```text
Start work

    ↓

git pull

(Get latest code)

    ↓

Create branch

    ↓

Write code

    ↓

Push branch

    ↓

Create Pull Request

(Ask to merge code)

    ↓

Review + CI

    ↓

Merge
```

---

### Final mental shortcut:

```text
git pull
=
"Bring other people's changes to me."


Pull Request
=
"Take my changes and add them to the shared project."
```

`pull` is about **receiving code**.
`Pull Request` is about **contributing code**.

---

## See Also

- [topic-16.md](topic-16.md) — can a Pull Request exist without GitHub?
- [topic-17.md](topic-17.md) — difference between Git merge and GitHub merge
- [topic-18.md](topic-18.md) — difference between Git Push and Pull Request
