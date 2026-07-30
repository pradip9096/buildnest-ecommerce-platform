# Knowledge Base — Learning

Beginner-oriented walkthroughs, Q&A articles, and roadmaps — distinct from
[`docs/knowledge-base/project/`](../project/README.md), which holds durable, reusable knowledge
about BuildNest's own engineering practices and tooling decisions. This folder favors a simpler
header (`**Category:**`/`**Tags:**`/`**Audience:**`, no YAML frontmatter) over `project/`'s stricter
Topic-format schema — pick whichever folder matches the content's actual purpose, not the header
style.

This is a manifest, not orientation content — one row per article. See
[Manifest and Surrogate Pattern for Index Files](../project/manifest-and-surrogate-pattern-for-index-files.md)
for why a manifest stays a clean set of rows rather than accumulating its own prose.

## Index

| File | Topic | Last Updated |
|---|---|---|
| [ideal-initial-project-setup-from-scratch.md](ideal-initial-project-setup-from-scratch.md) | Command-level, 41-step walkthrough for bootstrapping a BuildNest-shaped project from scratch — git/GitHub/CI-CD wiring before any code, runtime/dependency skeleton, security baseline, testing/coverage gates, static analysis ratcheted from zero, API/code docs, process/institutional memory | 2026-07-15 |
| [native-mysql-vs-docker-mysql.md](native-mysql-vs-docker-mysql.md) | Native vs. Docker-hosted MySQL — differences and which to choose | 2026-06-24 |
| [api-authentication-and-rbac-authorization.md](api-authentication-and-rbac-authorization.md) | API authentication and RBAC authorization fundamentals | 2026-06-24 |
| [http-basic-authentication.md](http-basic-authentication.md) | HTTP Basic Authentication mechanics | 2026-06-24 |
| [jwt-authentication-learning-roadmap.md](jwt-authentication-learning-roadmap.md) | Topic learning roadmap for JWT authentication | 2026-06-24 |
| [user-signup-and-login-authentication-flow.md](user-signup-and-login-authentication-flow.md) | User sign-up and login authentication flow | 2026-06-24 |
| [software-testing-fundamentals-and-quality-metrics.md](software-testing-fundamentals-and-quality-metrics.md) | Software testing fundamentals and quality metrics (Q&A format) | 2026-07-01 |
| [software-testing-with-everyday-life.md](software-testing-with-everyday-life.md) | Software testing concepts explained through everyday-life analogies (Q&A format) | 2026-07-15 |
| [question-quality-evaluation-frameworks.md](question-quality-evaluation-frameworks.md) | Frameworks for evaluating the quality of a question | 2026-07-01 |
| [knowledge-organization-formats-qa-topic-chapter.md](knowledge-organization-formats-qa-topic-chapter.md) | Differences between Q&A, Topic, and Chapter documentation formats — the format-selection reference both KB folders point back to | 2026-07-07 |
| [learning-roadmap.md](learning-roadmap.md) | Overall learning roadmap | 2026-06-24 |
| [progression-dashboards-kpis-and-quality-attributes.md](progression-dashboards-kpis-and-quality-attributes.md) | Progression dashboards, KPIs, and quality attributes | 2026-07-07 |
| [process-improvement-frameworks.md](process-improvement-frameworks.md) | Process improvement frameworks | 2026-07-14 |
| [capa-corrective-and-preventive-action.md](capa-corrective-and-preventive-action.md) | CAPA (Corrective and Preventive Action) via PDCA/8D methodology | 2026-07-14 |
| [English Communication mastery-framework-beyond-grammar-and-syntax.md](English%20Communication%20mastery-framework-beyond-grammar-and-syntax.md) | Communication mastery framework beyond grammar/syntax | 2026-07-10 |
| [git-github-ecosystem/](git-github-ecosystem/README.md) | Subfolder — Git/GitHub ecosystem topic series (own README/index, 20 topics) | 2026-07-13 |
| [spring-core/](spring-core/spring-core-learning-outline.md) | Subfolder — Spring Core learning modules (motivation, architecture, IoC/IoC container); `spring-core-learning-outline.md` is its de facto index | 2026-07-15 |
| [_templates/learning-outline-template.md](_templates/learning-outline-template.md) | Standalone, copy-anywhere Chapter-format outline template (Level → Module → Outcome + Check) for software-engineering subjects — includes a Definitions glossary, a 10-step SOP with a hard-stop Scope Check, Content Style rules, and a ready-to-use LLM Reuse Prompt | 2026-07-30 |
| [_templates/module-file-template.md](_templates/module-file-template.md) | Standalone, copy-anywhere template for authoring ONE Module's own file after a learning-outline-template.md skeleton exists — real explanatory prose (Topic Inner Structure, Learning Objective stated upfront) in a Header/Body/Footer shell, bullets/tables/diagrams as supporting devices not the primary content, a 9-step self-authoring checklist, no LLM automation | 2026-07-30 |
| [_templates/module-file-quick-template.md](_templates/module-file-quick-template.md) | Bare-skeleton companion to module-file-template.md — no guidance comments, just the Outer placeholders + one Body slot, for day-to-day copy/fill/paste once the rules are already understood | 2026-07-30 |

## Housekeeping

- Add a row when creating a new article or subfolder in this folder.
- Update the `Last Updated` column on any substantive edit.
- A subfolder with its own README/index is listed as one row here, pointing at that index — don't
  duplicate its contents into this table.
