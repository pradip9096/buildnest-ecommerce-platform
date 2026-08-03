# Knowledge Base — Learning

Beginner-oriented walkthroughs, Q&A articles, and roadmaps — distinct from
[`docs/knowledge-base/project/`](../project/README.md), which holds durable, reusable knowledge
about BuildNest's own engineering practices and tooling decisions. This folder favors a simpler
header (`**Category:**`/`**Tags:**`/`**Audience:**`, no YAML frontmatter) over `project/`'s stricter
Topic-format schema — pick whichever folder matches the content's actual purpose, not the header
style.

This file does two jobs, kept in two separate sections below: **Orientation** (how this folder
relates to its siblings, and the rules for adding to it) and **Index** (the manifest — the
authoritative list of every article/subfolder, as one-line rows). See
[Manifest and Surrogate Pattern for Index Files](../project/manifest-and-surrogate-pattern-for-index-files.md)
and the [README-as-Manifest Blueprint](../project/readme-manifest-blueprint.md) for why this split
exists.

---

## Orientation

### Housekeeping

- Add a row to the Index table below when creating a new article or subfolder in this folder.
- Update the `Last Updated` column on any substantive edit.
- A subfolder with its own README/index is listed as one row here, pointing at that index — don't
  duplicate its contents into this table.

---

## Index

The manifest — one row per article or subfolder, no prose beyond this table.

| Sr. No. | File | Topic | Last Updated |
| :--- | :--- | :--- | :--- |
| 1 | [ideal-initial-project-setup-from-scratch.md](ideal-initial-project-setup-from-scratch.md) | Command-level, 41-step walkthrough for bootstrapping a BuildNest-shaped project from scratch — git/GitHub/CI-CD wiring before any code, runtime/dependency skeleton, security baseline, testing/coverage gates, static analysis ratcheted from zero, API/code docs, process/institutional memory | 2026-07-15 |
| 2 | [native-mysql-vs-docker-mysql.md](native-mysql-vs-docker-mysql.md) | Native vs. Docker-hosted MySQL — differences and which to choose | 2026-06-24 |
| 3 | [api-authentication-and-rbac-authorization.md](api-authentication-and-rbac-authorization.md) | API authentication and RBAC authorization fundamentals | 2026-06-24 |
| 4 | [http-basic-authentication.md](http-basic-authentication.md) | HTTP Basic Authentication mechanics | 2026-06-24 |
| 5 | [jwt-authentication-learning-roadmap.md](jwt-authentication-learning-roadmap.md) | Topic learning roadmap for JWT authentication | 2026-06-24 |
| 6 | [user-signup-and-login-authentication-flow.md](user-signup-and-login-authentication-flow.md) | User sign-up and login authentication flow | 2026-06-24 |
| 7 | [software-testing-fundamentals-and-quality-metrics.md](software-testing-fundamentals-and-quality-metrics.md) | Software testing fundamentals and quality metrics (Q&A format) | 2026-07-01 |
| 8 | [software-testing-with-everyday-life.md](software-testing-with-everyday-life.md) | Software testing concepts explained through everyday-life analogies (Q&A format) | 2026-07-15 |
| 9 | [question-quality-evaluation-frameworks.md](question-quality-evaluation-frameworks.md) | Frameworks for evaluating the quality of a question | 2026-07-01 |
| 10 | [knowledge-organization-formats-qa-topic-chapter.md](knowledge-organization-formats-qa-topic-chapter.md) | Differences between Q&A, Topic, and Chapter documentation formats — the format-selection reference both KB folders point back to | 2026-07-07 |
| 11 | [learning-roadmap.md](learning-roadmap.md) | Overall learning roadmap | 2026-06-24 |
| 12 | [progression-dashboards-kpis-and-quality-attributes.md](progression-dashboards-kpis-and-quality-attributes.md) | Progression dashboards, KPIs, and quality attributes | 2026-07-07 |
| 13 | [process-improvement-frameworks.md](process-improvement-frameworks.md) | Process improvement frameworks | 2026-07-14 |
| 14 | [capa-corrective-and-preventive-action.md](capa-corrective-and-preventive-action.md) | CAPA (Corrective and Preventive Action) via PDCA/8D methodology | 2026-07-14 |
| 15 | [English Communication mastery-framework-beyond-grammar-and-syntax.md](English%20Communication%20mastery-framework-beyond-grammar-and-syntax.md) | Communication mastery framework beyond grammar/syntax | 2026-07-10 |
| 16 | [git-github-ecosystem/](git-github-ecosystem/README.md) | Subfolder — Git/GitHub ecosystem topic series (own README/index, 20 topics) | 2026-07-13 |
| 17 | [spring-core/](spring-core/spring-core-learning-outline.md) | Subfolder — Spring Core learning modules (motivation, architecture, IoC/IoC container); `spring-core-learning-outline.md` is its de facto index | 2026-07-15 |
| 18 | [_templates/](_templates/README.md) | Subfolder — reusable, standalone templates for producing this folder's own content (a per-subject outline skeleton + one Module file per planned entry): `learning-outline-template.md`, `chatgpt-project-setting-instrctions.md`, `module-file-template.md`, `module-file-quick-template.md` (own README/index, 4 files) | 2026-08-03 |
