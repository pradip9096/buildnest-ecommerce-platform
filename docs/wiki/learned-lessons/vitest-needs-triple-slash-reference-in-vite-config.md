---
title: Vitest Config in vite.config.ts Requires a Triple-Slash Type Reference
category: technical
tags: [vitest, vite, typescript, testing, frontend, config]
keywords: [vitest/config, test key, defineConfig, UserConfig test property, vite.config.ts, unknown property test]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
root_cause: "Vite's own UserConfig type doesn't declare a test property, so a Vitest test block colocated in vite.config.ts type-checks as unknown/errors without Vitest's ambient module augmentation, which only loads via an explicit triple-slash reference"
impact: low — a type-only issue with no runtime effect, resolved by adding one reference directive
related_lessons: []
---

# Vitest Config in `vite.config.ts` Requires a Triple-Slash Type Reference

## Problem

Adding Vitest to an existing Vite project (BuildNest frontend, `#293`) by putting a `test: {...}` block inside the same `defineConfig({...})` call in `vite.config.ts` looks correct and works at runtime, but `vite`'s own `UserConfig` type (from the `vite` package, not `vitest`) does not declare a `test` property. Without extra setup, this either silently type-checks as an unknown extra property (depending on `tsconfig` strictness) or produces a type error, and editor tooling won't autocomplete/validate the Vitest-specific options (`environment`, `setupFiles`, `css`, etc.).

## Fix

Add a triple-slash reference directive at the very top of `vite.config.ts`, before any imports:

```ts
/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
```

This pulls in Vitest's ambient module augmentation that extends Vite's `UserConfig` interface with the `test` property, so the same `defineConfig` call from `vite` gets full type support for the Vitest config block — no need for a separate `vitest.config.ts` file or `mergeConfig` boilerplate when the project only needs one config file.

## Rule

When colocating Vitest config inside `vite.config.ts` (the common case for small-to-medium projects that don't need a separate test config), always add `/// <reference types="vitest/config" />` as the first line. If the `test` key is used without it, don't assume a type error there means Vitest is misconfigured — it usually means the ambient types haven't been pulled in.
