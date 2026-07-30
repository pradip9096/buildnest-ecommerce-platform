# Issues, Projects, and Views as a Work Management System

How GitHub Issues (what work), Projects (how work is organized), and Views (how progress is
visualized) fit together as a lightweight work management system layered on top of a repository.

## Table of Contents

1. [GitHub Issue](#1-github-issue)
2. [GitHub Project](#2-github-project)
3. [Project Fields](#3-project-fields)
4. [GitHub Project Views](#4-github-project-views)
- [Relationship Between Issue, Project, View](#relationship-between-issue-project-view)
- [How It Connects With Development](#how-it-connects-with-development)
- [Real Software Engineering Mapping](#real-software-engineering-mapping)
- [See Also](#see-also)

---

**Claim:** In GitHub, **Issues, Projects, and Views form a work management system around your repository.**
**Git stores code history. PRs control code changes. Issues define work. Projects organize and track work. Views show the same work from different perspectives.**

```text
Idea / Problem / Requirement
          │
          ▼
     GitHub Issue
          │
          ▼
    GitHub Project
          │
          ▼
      Project Views
          │
          ▼
 Planning + Tracking
          │
          ▼
 Branch → Code → PR → Merge
```

---

## 1. GitHub Issue

### Q: What is a GitHub Issue?

A **GitHub Issue is a trackable work item.**

It represents:

* Bug
* Feature request
* Enhancement
* Documentation work
* Research task
* Technical debt
* Discussion item

Example:

```text
Issue #25

Title:
Add user authentication API

Description:
Implement login/logout functionality.

Requirements:
- JWT token generation
- Password validation
- Error handling

Acceptance Criteria:
✔ User can login
✔ Invalid password rejected
✔ Tests added
```

---

## Mental Model

Issue answers:

> "What work needs to be done and why?"

---

A repository becomes:

```text
Repository

├── Code
│     └── Git commits
│
└── Work Tracking
      └── Issues
```

---

## Issue Metadata

An issue is not only text.

It contains management information.

Example:

```text
Issue #45

Title:
Fix payment timeout bug

Labels:
bug
priority-high

Assignee:
Pradip

Milestone:
v1.2 Release

Status:
In Progress
```

---

Common metadata:

| Field       | Purpose             |
| ----------- | ------------------- |
| Title       | Short description   |
| Description | Requirement details |
| Assignee    | Who owns it         |
| Labels      | Classification      |
| Milestone   | Release grouping    |
| Comments    | Discussion          |
| Linked PR   | Implementation link |

---

## 2. GitHub Project

## Q: What is GitHub Project?

A **GitHub Project is a planning and tracking board that manages issues and PRs.**

Issue:

> "What needs to be done?"

Project:

> "Where is everything in the workflow?"

---

Example:

Issues:

```text
#10 Create login API

#11 Add payment gateway

#12 Write tests

#13 Update documentation
```

Project organizes them:

```text
BuildNest Backend Project

TODO

#10 Login API
#11 Payment


IN PROGRESS

#12 Testing


DONE

#13 Documentation
```

---

Project acts like:

```text
Mission Control Dashboard
```

---

## Project Does Not Replace Issues

Relationship:

```text
GitHub Project

      │

      manages

      │

      ▼

GitHub Issues


Issue still belongs to repository
```

---

One issue can appear in a project:

```text
Repository A

Issue #100

      │

      ▼

Backend Roadmap Project
```

---

## 3. Project Fields

Projects add extra tracking data.

Example:

```text
Issue:
Add shopping cart API


Project Fields:

Status:
In Progress

Priority:
High

Sprint:
Sprint 5

Size:
Medium

Deadline:
July 20
```

---

Common fields:

| Field       | Meaning            |
| ----------- | ------------------ |
| Status      | Workflow stage     |
| Priority    | Importance         |
| Estimate    | Effort             |
| Sprint      | Iteration          |
| Owner       | Responsible person |
| Target Date | Schedule           |

---

## 4. GitHub Project Views

## Q: What is a View?

A **View is a different visualization of the same project data.**

Same issues.

Different perspective.

---

Imagine data:

```text
Issues Database

#1 Login
#2 Payment
#3 Search
#4 Bug Fix
```

Views are windows:

```text
                Issues

                   │

     ┌─────────────┼─────────────┐

     ▼             ▼             ▼

 Board View    Table View   Roadmap View
```

---

### Board View (Kanban)

Best for workflow.

Example:

```text
TODO
 |
 |-- Login API
 |-- Payment API


IN PROGRESS
 |
 |-- Search API


DONE
 |
 |-- Documentation
```

Answers:

> "Where is each task?"

---

### Table View

Looks like spreadsheet.

Example:

| Issue     | Status | Priority | Owner |
| --------- | ------ | -------- | ----- |
| Login API | Todo   | High     | Dev A |
| Payment   | Doing  | High     | Dev B |

Answers:

> "What is the detailed status?"

---

### Roadmap View

Timeline planning.

Example:

```text
July

Week 1
████ Login

Week 2
████ Payment

Week 3
████ Testing
```

Answers:

> "When will work happen?"

---

## Relationship Between Issue, Project, View

Complete model:

```text
GitHub Repository

        │

        ▼

Issue
"What work?"

        │

        ▼

Project
"How do we organize work?"

        │

        ▼

View
"How do we look at progress?"
```

---

## How It Connects With Development

Example lifecycle:

```text
Requirement

    │

    ▼

Create Issue #50

    │

    ▼

Add Issue to Project

    │

    ▼

Status: TODO

    │

    ▼

Create Branch

feature/issue-50-login

    │

    ▼

Implement Code

    │

    ▼

Create PR

    │

    ▼

CI + Review

    │

    ▼

Merge

    │

    ▼

Issue Closed

    │

    ▼

Project Status: DONE
```

---

## Real Software Engineering Mapping

| Engineering Concept      | GitHub Feature      |
| ------------------------ | ------------------- |
| Requirement              | Issue               |
| Work Breakdown Structure | Issues/Sub-Issues   |
| Planning System          | Project             |
| Workflow State           | Status Field        |
| Dashboard                | View                |
| Implementation           | Branch              |
| Change Request           | PR                  |
| Verification             | CI                  |
| Completion               | Merge + Close Issue |

---

**Final mental model:**

```text
Issue  = Work definition
Project = Work coordination
View    = Work visualization
Branch  = Work isolation
PR      = Change approval
CI      = Automated verification
Merge   = Integration
```

Together they create a lightweight software delivery management system.

---

## See Also

- [topic-7.md](topic-7.md) — Milestone vs. Epic vs. Sprint, different ways to organize work
- [topic-8.md](topic-8.md), [topic-9.md](topic-9.md) — GitHub issue creation walkthroughs with `gh`
- [topic-10.md](topic-10.md) — two-layer mental model for a full delivery workflow
