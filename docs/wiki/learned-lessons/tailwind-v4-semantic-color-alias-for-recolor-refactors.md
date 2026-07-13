---
title: Tailwind v4 Semantic Color Aliasing Makes Future Recolor Refactors a One-Line Change
category: technical
tags: [tailwind, css, theme, design-system, refactor, frontend]
keywords: [tailwind v4 theme, color alias, primary color token, var(--color-x), brand color refactor, css custom properties]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
root_cause: "a naive find-and-replace from one hardcoded Tailwind palette name to another satisfies visual consistency but still hardcodes a literal color name across ~30 files, failing the acceptance criterion's actual requirement of a single edit point for future rebrands"
impact: low — caught before implementation by re-reading the acceptance criteria, no rework needed
related_lessons:
  - docs/wiki/learned-lessons/vitest-needs-triple-slash-reference-in-vite-config.md
---

# Tailwind v4 Semantic Color Aliasing Makes Future Recolor Refactors a One-Line Change

## Problem

`#245` required unifying two competing "primary" colors (`indigo-*` and `amber-*`, ~30 files) into one consistent brand color, with the acceptance criteria explicitly asking for a fix "via a Tailwind CSS theme extension... so future changes require editing one place." Simply find-and-replacing every `amber-*` class with the equivalent `indigo-*` shade would satisfy visual consistency today, but not the "one place to edit" requirement — the color name `indigo` would still be hardcoded as a literal string across ~30 files, so a future rebrand would mean repeating the same mechanical sweep.

## Fix

Tailwind v4's CSS-first `@theme` block lets you define a new semantic color scale that *points at* an existing built-in scale via CSS custom property references, rather than hardcoding new hex/oklch values:

```css
@theme {
  --color-primary-50: var(--color-indigo-50);
  --color-primary-100: var(--color-indigo-100);
  /* ...through 900 */
}
```

This immediately makes Tailwind generate `bg-primary-*`, `text-primary-*`, `ring-primary-*`, `border-primary-*`, etc. — full utility support for a name that doesn't exist as one of Tailwind's built-in palettes. Every component then uses `primary-*` instead of `indigo-*` or `amber-*` directly. A future rebrand (e.g., switching to a completely different color, or moving to a custom oklch palette) means editing only the right-hand side of the ten `@theme` lines — no component files touched.

## Rule

When a design-system/brand-color unification task explicitly asks for "one place to change this later," don't just pick a winning Tailwind color and find-replace its name everywhere — introduce a semantic alias in `@theme` (or equivalent CSS variable layer) that *references* the chosen palette, and migrate components to the semantic name. The extra ten lines of `@theme` aliasing is what actually satisfies "one place," not the choice of which built-in palette wins.

This also composes with selective conversion: when only some usages of a color are brand-related (CTAs, focus rings, active states) and others are genuinely semantic (star ratings, status badges, categorical chart colors), only the brand usages should move to the new alias — converting semantic color-coding to match the brand palette would be a correctness regression, not a fix, even though it's "the same Tailwind color name" being touched.
