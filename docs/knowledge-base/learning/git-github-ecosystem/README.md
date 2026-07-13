# Git & GitHub Ecosystem

Learning notes on Git, GitHub, and GitHub CLI/Actions workflows — PR lifecycle, issue/project
management, CI integration, and common beginner-to-practical FAQ material.

This file does two jobs, kept in two separate sections below: **Orientation** (answer/topic
structure conventions used across these files) and **Index** (the manifest — the authoritative
list of every topic file, as one-line rows). See
[Manifest and Surrogate Pattern for Index Files](../../project/manifest-and-surrogate-pattern-for-index-files.md)
and the [README-as-Manifest Blueprint](../../project/readme-manifest-blueprint.md) for why this
split exists.

---

## Orientation

### Naming Convention

Files are named `topic-<N>.md`, numbered sequentially in creation order — not by subject. Use
the Index table below to find a file by subject; don't infer topic from the number.

### Answer / Topic Structure

Two structural conventions used across these files, inherited from this repo's broader KB
authoring conventions:

- **Answer structure**: Short Answer → Detailed Answer → Bottom Line
- **Topic structure**: Introduction (establish context and direction) → Main Body (develop and
  support ideas) → Conclusion (synthesize and close)

### Housekeeping

- Add a row to the Index table below when creating a new `topic-N.md` file.
- Keep one topic per file — if a file starts covering two unrelated questions, split it.

---

## Index

The manifest — one row per topic file, no prose beyond this table.

| File | Topic |
|---|---|
| [topic-1.md](topic-1.md) | Full PR workflow lifecycle using GitHub CLI (`gh`) — branch, commit, push, PR create/review/merge, cleanup — with a complete reusable command sequence |
| [topic-2.md](topic-2.md) | Can you switch branches mid-work and come back later? Branch-switching mechanics and safety |
| [topic-3.md](topic-3.md) | Git and GitHub FAQ, beginner → practical developer level |
| [topic-4.md](topic-4.md) | Automated Testing → Quality Gate → CI → Pull Request as a closed-loop quality control system |
| [topic-5.md](topic-5.md) | Can a Pull Request exist without CI? |
| [topic-6.md](topic-6.md) | Issues, Projects, and Views as a work management system around a repository |
| [topic-7.md](topic-7.md) | Milestone vs. Epic vs. Sprint — different ways to organize work and the questions each answers |
| [topic-8.md](topic-8.md) | GitHub issue creation walkthrough using GitHub CLI |
| [topic-9.md](topic-9.md) | GitHub issue creation with label, milestone, and project using GitHub CLI |
| [topic-10.md](topic-10.md) | Two-layer mental model for a production-style GitHub software delivery workflow |
| [topic-11.md](topic-11.md) | Worked example: BuildNest e-commerce "Add Product Review Feature" end-to-end |
| [topic-12.md](topic-12.md) | CI with Pull Request using GitHub Actions — end-to-end walkthrough |
| [topic-13.md](topic-13.md) | FAQ: GitHub professional workflow (Issue → Project → PR → CI → Merge) |
| [topic-14.md](topic-14.md) | Common interview questions on Git, GitHub, and GitHub Actions, with answers |
| [topic-15.md](topic-15.md) | Difference between Pull and Pull Request |
| [topic-16.md](topic-16.md) | Can a Pull Request exist without GitHub? |
| [topic-17.md](topic-17.md) | Difference between Git merge and GitHub merge |
| [topic-18.md](topic-18.md) | Difference between Git Push and Pull Request |
| [topic-19.md](topic-19.md) | Why Git/GitHub/GitHub Actions are hard for beginners — distributed collaboration model, state management, and automated delivery workflow as three distinct learning burdens |
| [topic-20.md](topic-20.md) | Feature branching for parallel, independent issue development |
