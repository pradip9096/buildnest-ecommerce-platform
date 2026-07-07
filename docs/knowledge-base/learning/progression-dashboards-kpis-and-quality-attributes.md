---
title: Progression Dashboards, KPIs, and Quality Attributes
category: quality-engineering
tags: [dashboard, kpi, metrics, data-visualization, okr, quality-attributes]
keywords: [progression dashboard, dashboard visualization, key performance indicator, KPI vs quality metric, SMART KPIs, leading vs lagging indicators, drill-through analysis]
objective: Define progression dashboards, dashboard visualization, KPIs, and how KPIs differ from quality attributes/metrics — as a measurement-and-visualization companion to the broader continuous-improvement pattern in the closed-loop feedback article.
audience: engineers or team leads deciding what to measure about a process and how to visualize it
scope: general educational material, not BuildNest-specific
source_conversations: [Session 2026-07-07]
last_updated: 2026-07-07
confidence: medium
evidence_strength: weak
related_articles:
  - ../project/closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md
  - software-testing-fundamentals-and-quality-metrics.md
status: draft
---

# Progression Dashboards, KPIs, and Quality Attributes

> This article is general educational material, not BuildNest-specific — it's cross-referenced
> from [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](../project/closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)'s
> "Continuous Improvement Metrics" section, which asks *what* to measure about a process; this
> article covers *how* that gets visualized and which specific measures (KPIs) deserve a
> dashboard at all.
>
> **Status: draft.** Each section below still carries open questions that haven't been answered
> yet — this is deliberately preserved as a set of future-research prompts rather than removed,
> but it means the article isn't yet a finished reference the way its sibling articles are. Treat
> claims here as informational, not verified against a primary source (`evidence_strength: weak`
> in the frontmatter reflects this).

---

## Progression Dashboard

A **progression dashboard** is a visual interface that consolidates data to track advancement
toward goals, milestones, or targets across various domains. These tools provide real-time
insights into performance metrics, allowing users to identify bottlenecks, monitor resource
utilization, and compare actual progress against planned objectives.

Key applications include:

- **Project Management** — tracking task completion, budget variance, and schedule adherence
  using Gantt charts and KPIs, often integrated with tools like Jira, Power BI, or Primavera P6.
- **Education and Training** — monitoring learner competency, module completion, and assessment
  grades, such as the myTIPreport system for medical training or Mooncamp for OKRs.
- **Game Development** — analyzing user progression through levels, including completion rates,
  failure points, and scores via platforms like GameAnalytics.
- **Corporate Sustainability** — measuring progress against science-based targets and ESG goals,
  as demonstrated by the Science Based Targets Initiative (SBTi).
- **Construction and Operations** — visualizing site productivity, hours spent versus planned, and
  budget status in real time using specialized platforms like Dreeven.

These dashboards typically feature customizable filters, interactive charts, and automated data
integration to enable data-driven decision-making for stakeholders ranging from team leads to
executives.

**Open Questions**
1. How do you build a progression dashboard from scratch?
2. What are the best practices for dashboard visualization?
3. How to integrate APIs into progression dashboards?

---

## Dashboard Visualization

**Dashboard visualization** is an interactive user interface that consolidates data from multiple
sources into a single view using charts, graphs, maps, and gauges to enable real-time monitoring
and analysis. Unlike static reports that show historical data, dashboards provide up-to-the-minute
insights, allowing stakeholders to identify trends, spot anomalies, and make data-driven decisions
without switching between applications.

Effective dashboards typically include Key Performance Indicators (KPIs) at the top for
high-level health checks, followed by detailed visualizations that support interactivity such as
filtering, drilling down into specific metrics, and cross-filtering across charts. Common
visualization types include line charts for trends, bar charts for comparisons, and gauges for
tracking metrics against targets, all arranged logically to support the specific goals of the
audience.

Modern platforms like Tableau, Power BI, and Domo facilitate these dashboards by offering
drag-and-drop builders, hundreds of chart types, and automated data integration from sources like
CRM and ERP systems. Best practices for design emphasize clarity and context, ensuring that only
relevant metrics are displayed to avoid information overload while maintaining a consistent visual
language for easy interpretation.

**Open Questions**
1. What are common dashboard design mistakes?
2. How do you choose the right KPIs?
3. What is drill-through analysis?

---

## KPIs

**Key Performance Indicators (KPIs)** are quantifiable measurements used to evaluate the success
of an organization, project, or activity in achieving defined objectives. They serve as critical
metrics that help businesses monitor progress toward strategic and operational goals, enabling
data-driven decision-making and identifying areas for improvement.

Their function and scope typically categorize KPIs:

- **Financial KPIs** measure profitability and revenue, such as net profit margin or revenue
  growth.
- **Customer KPIs** track satisfaction and retention, including customer lifetime value or churn
  rate.
- **Operational KPIs** assess internal efficiency, such as production cycle time or error rates.
- **Leading vs. Lagging** — leading indicators predict future outcomes (e.g., sales pipeline
  value), while lagging indicators reflect past performance (e.g., annual revenue).

Effective KPIs are **SMART** (Specific, Measurable, Achievable, Relevant, and Time-bound) and are
distinct from general metrics by focusing specifically on the "key" areas that drive business
success. They provide a clear view of performance against targets, allowing organizations to align
daily activities with long-term strategic visions.

**Open Questions**
1. How do you select the right KPIs for a startup?
2. What are common KPI mistakes to avoid?
3. How can KPIs be visualized effectively?

---

## Quality Attributes

A **quality attribute**, in this article's usage, is any measurable characteristic of a process or
its outputs — for example defect density, requirements stability, or cycle time. This is a
**different sense of the term than the ISO/IEC 25010-style usage** in
[Closed-Loop Feedback and Amendment Mechanisms for Process Documents](../project/closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)
(Maintainability, Reliability, Security, etc. — architectural qualities a *system* has). Don't
conflate the two: that article's "quality attribute" describes what a *system* is; this article's
describes a *data point measured about a process*. Where this article says "quality attribute,"
it means the same thing as "quality metric" — used interchangeably here.

Quality attributes/metrics are numerous, often unassigned to a specific owner, and don't
necessarily trigger any response when they move — they're raw operational data, not a strategic
signal on their own. The next section contrasts this directly against KPIs, which are the small,
owned, response-triggering subset that matters at the strategic level.

---

## KPIs vs. Quality Attributes

**Key Performance Indicators (KPIs)** are strategic, high-impact measures that track progress
toward specific business objectives and always trigger a defined response when targets are missed.
In contrast, **Quality Attributes** (or Quality Metrics, per the definition above) are tactical,
operational data points that measure specific process characteristics or outputs, such as defect
density or requirements stability, without necessarily being tied to a strategic goal.

The distinction lies in scope and action: quality metrics are numerous and can be unassigned,
serving as raw data, whereas KPIs are a selective set (typically 8–12) with clear ownership and
escalation paths. For example, the number of units scrapped in an hour is a quality metric, while
the scrap rate as a percentage of total production tied to a Cost of Poor Quality (COPQ) reduction
goal is a KPI.

| Feature | Quality Metrics (Attributes) | Quality KPIs |
|---|---|---|
| **Scope** | Tactical and operational | Strategic and high-impact |
| **Volume** | Numerous (hundreds of data points) | Selective (small, focused set) |
| **Ownership** | Often unassigned | Assigned to a specific owner |
| **Trigger** | May not trigger a response | Always triggers a defined response |
| **Purpose** | Measure specific process attributes | Measure progress toward strategic objectives |

**Open Questions**
1. How do you select the right KPIs from many metrics?
2. What are examples of quality KPIs in software development?
3. How do you set targets for quality KPIs?

---

## Related Articles

- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](../project/closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md) —
  the broader continuous-improvement/feedback-loop pattern this article's KPIs and dashboards feed
  measurements into (see that article's "Continuous Improvement Metrics" and "Change Impact
  Scoring" sections). **Note the terminology difference** flagged in "Quality Attributes" above —
  the same term means something different in that article.
- [Software Testing Fundamentals and Quality Metrics](software-testing-fundamentals-and-quality-metrics.md) —
  a distinct, deeper treatment of code-level quality metrics (coverage, mutation score)
  specifically, rather than organization-level KPIs.
