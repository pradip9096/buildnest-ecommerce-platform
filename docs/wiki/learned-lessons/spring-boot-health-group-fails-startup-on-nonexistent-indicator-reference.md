---
title: "Spring Boot Fails Application Startup — Not Just a Warning — If a Health Group's include List References a Nonexistent Indicator"
category: infrastructure
tags: [spring-boot, actuator, health-groups, kubernetes-probes, conditional-beans, startup-failure]
keywords: [management.endpoint.health.group include, readiness group startup failure, ConditionalOnProperty health indicator, non-existent health contributor, Kubernetes readiness probe]
source_conversations: [Session 2026-08-08, issue #123]
last_updated: 2026-08-08
confidence: high
evidence_strength: strong
root_cause: "management.endpoint.health.group.<name>.include validates every listed indicator ID against the currently-registered HealthContributor beans at startup, with no lenient/ignore-unknown mode by default — a group referencing an indicator whose bean is absent (e.g. behind a @ConditionalOnProperty that evaluated false) fails ApplicationContext startup entirely, not just at query time"
impact: high — would have broken every environment where elasticsearch.enabled=false (the default here) had the readiness group hardcoded elasticsearch into its include list
related_lessons: []
---

# Spring Boot Fails Application Startup — Not Just a Warning — If a Health Group's `include` List References a Nonexistent Indicator

## Problem

Implementing #123 (Kubernetes readiness/liveness probes), the natural-looking fix for "the
readiness probe should reflect DB/Redis/Elasticsearch reachability" was:

```properties
management.endpoint.health.group.readiness.include=readinessState,db,redis,elasticsearch
```

This looks correct and would work — *if* an `ElasticsearchHealthIndicator` bean is always
registered. In this repo it isn't: the bean is gated by
`@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)`,
matching `ElasticsearchConfig`'s own convention, and `elasticsearch.enabled` defaults to `false`
in every environment (dev, test, and production unless explicitly overridden). Applying the
hardcoded include list above in that default state would have taken the entire application down
at startup — not a degraded health check, not a 503, a failed `ApplicationContext` refresh.

## Root Cause

Confirmed via Spring Boot's own reference docs (queried through context7 mid-implementation,
before the config was applied): "by default, the application will fail to start if a group
references a non-existent indicator." `HealthEndpointProperties`'s `Group.include`/`exclude`
sets are validated against the actually-registered `HealthContributorRegistry` contents at
startup, with no built-in "skip unknown names silently" mode. This is unrelated to whether the
indicator would *report* down — it's a *registration-existence* check, evaluated once, eagerly,
before any health check ever runs.

The gotcha is specifically that this failure mode is invisible from reading the property alone:
`readinessState`, `db`, and `redis` are always present in this repo (unconditional beans), so a
config edit adding `elasticsearch` to the same list looks symmetric and safe — the asymmetry
(three unconditional indicators plus one `@ConditionalOnProperty`-gated one) is what actually
matters, and nothing about the properties file syntax surfaces that difference.

## Fix

Made the group's `elasticsearch` membership itself conditional by routing it through an
env-var-driven property with a safe default, kept in sync with the same toggle that gates the
bean:

```properties
# HEALTH_READINESS_GROUP must be kept in sync with ELASTICSEARCH_ENABLED — see .env.example
management.endpoint.health.group.readiness.include=${HEALTH_READINESS_GROUP:readinessState,db,redis}
```

`.env.example` documents `HEALTH_READINESS_GROUP=readinessState,db,redis` as the default,
with a comment instructing deployers to add `,elasticsearch` to it only when they also set
`ELASTICSEARCH_ENABLED=true`.

## Generalization

Any time a health group's `include`/`exclude` list names an indicator whose backing bean is
itself conditionally registered (`@ConditionalOnProperty`, `@Profile`, a feature flag), the
group list must be conditional too — hardcoding the group membership silently reintroduces a
compile-time-looking dependency between two independently-toggleable pieces of config, and the
failure mode (total startup failure) is far more severe than a stale health-check reading would
be. Before adding any name to `management.endpoint.health.group.*.include`, check whether that
name's indicator bean is unconditionally registered; if not, drive the group membership from the
same toggle, not a separately-maintained hardcoded list.
