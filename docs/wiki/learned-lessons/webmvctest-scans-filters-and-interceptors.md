---
title: "@WebMvcTest Scans Filters, Interceptors, and WebMvcConfigurers App-Wide"
category: testing
tags: [spring-boot, webmvctest, mockmvc, slice-test, dependency-injection]
keywords: [WebMvcTest cascading NoSuchBeanDefinitionException, WebMvcTest scans all filters, HandlerInterceptor WebMvcConfigurer picked up by slice test, converting SpringBootTest to WebMvcTest]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_lessons: []
---

# `@WebMvcTest` Scans Filters, Interceptors, and `WebMvcConfigurer`s App-Wide

## Problem

Converting `AdminInventoryControllerTest` from `@SpringBootTest` to `@WebMvcTest(AdminInventoryController.class)` (issue #259) was expected to load a minimal context containing only the target controller. Instead, the context repeatedly failed with `NoSuchBeanDefinitionException` for services completely unrelated to `AdminInventoryController` or `InventoryService`:

1. `PerformanceMonitoringService` — required by `PerformanceMonitoringInterceptor`, a `@Component implements HandlerInterceptor` that (it turned out) isn't even registered in any `WebMvcConfigurer.addInterceptors()` — dead code, but still bean-scanned.
2. `RateLimitUtil` — required by `AdminRateLimitFilter`, a `@Component extends OncePerRequestFilter` used app-wide, not specific to this controller.

`@WebMvcTest`'s slice does not limit component scanning to the target controller's own dependency graph. It includes **every** bean of certain stereotypes across the whole application: `@Controller`, `@ControllerAdvice`, `Converter`/`GenericConverter`, `Filter`, `WebMvcConfigurer`, `HandlerMethodArgumentResolver` — and, transitively, anything those beans' constructors require. A single unrelated `Filter` or `HandlerInterceptor` anywhere in the codebase can break every `@WebMvcTest` in the project until its dependencies are mocked.

## Fix

Iteratively run the test, read the `Caused by: ... NoSuchBeanDefinitionException` (not the outer `ApplicationContext failure threshold exceeded` wrapper, which repeats and hides the real cause — check `target/surefire-reports/<Test>.txt` directly for the full stack), add a `@MockBean` for the missing type, and repeat until the context loads. In this case: `JwtTokenProvider`, `CustomUserDetailsService` (needed by the JWT filter inside the imported `TestSecurityConfig`), `PerformanceMonitoringService`, and `RateLimitUtil`.

## Rule

Before converting a `@SpringBootTest` MockMvc test to `@WebMvcTest`, expect to need `@MockBean`s for every `Filter`/`HandlerInterceptor`/`WebMvcConfigurer` bean's dependencies in the whole app, not just the target controller's own service. Budget iteration time for this — it is trial-and-error by nature (read the actual `Caused by`, add a mock, rerun), not something to fully predict up front from reading the controller alone.
