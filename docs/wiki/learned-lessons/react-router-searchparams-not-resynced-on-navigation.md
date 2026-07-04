---
title: "useSearchParams State Doesn't Auto-Resync on Browser Back/Forward Without an Explicit Effect"
category: tooling
tags: [react-router, useSearchParams, useState-initializer, back-forward-navigation, url-state]
keywords: [search param not hydrated on load, browser back forward stale state, useSearchParams initializer only runs once, ProductListingPage filters bug]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
related_lessons: []
---

# useSearchParams State Doesn't Auto-Resync on Browser Back/Forward Without an Explicit Effect

## Problem

Fixing #284 (search/category query params ignored on direct page load) started with the obvious fix:

```tsx
const [searchParams] = useSearchParams();
const [filters, setFilters] = useState<ProductFilters>(() => filtersFromSearchParams(searchParams));
```

This correctly hydrates state on the *first* render of a fresh page load — `/products?search=cement` now shows filtered results and a pre-populated search box. It looked complete: every acceptance criterion involving a direct URL load passed.

It was still broken for the criterion "browser back/forward through search history restores the correct filtered view." Manually testing it: submit a search (pushes a new URL via `setSearchParams`), then click browser Back. The URL correctly reverts to the unfiltered `/products` — but the rendered product grid and search box **kept showing the stale "cement" results**. React Router doesn't remount `ProductListingPage` on a same-route query-param-only navigation (back/forward or otherwise), so the `useState(() => ...)` initializer — which only runs once, at mount — never re-runs to pick up the new `searchParams` value.

## Why the naive fix looks correct but isn't

Manually testing only "load a URL with query params directly" (i.e., a fresh mount) will pass with just the initializer fix, because a fresh mount is the one case where the initializer *does* run. It's specifically the *same-mount* navigation case (back/forward, or any programmatic `setSearchParams`/`navigate` call that doesn't unmount the component) that the initializer-only fix misses — and that's also the case least likely to get manually re-tested once the "it loads correctly from a URL" box is checked.

## Fix

Add an effect that watches the derived primitive values from `searchParams` (not the `URLSearchParams` object itself, which changes identity on every navigation) and resyncs local state:

```tsx
const urlKeyword = searchParams.get('search') ?? '';
const urlCategoryIds = searchParams.getAll('category').map(Number).filter(n => !Number.isNaN(n)).join(',');

useEffect(() => {
  setFilters(f => ({
    ...f,
    keyword: urlKeyword,
    categoryIds: urlCategoryIds ? urlCategoryIds.split(',').map(Number) : [],
    page: 0,
  }));
  setSearchInput(urlKeyword);
}, [urlKeyword, urlCategoryIds]);
```

This effect also fires redundantly on the initial mount (recomputing the same values the initializer already set) — harmless, but worth knowing if profiling shows an extra render.

## Verification approach that actually caught this

`curl`/direct-URL-navigation testing is not sufficient for this class of bug. Verify by *interacting* with the app (click a search/filter control to trigger a `pushState`-style navigation), then use actual browser back/forward — not by re-navigating to a URL string a second time, which is indistinguishable from a fresh load and won't exercise the same-mount resync path at all.
