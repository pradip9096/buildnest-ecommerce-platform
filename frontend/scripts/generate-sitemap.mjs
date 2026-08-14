#!/usr/bin/env node
// Build-time sitemap.xml generator (#134, FE-SEO-01). Run as a postbuild step —
// fetches product/category lists from the backend public API and writes
// dist/sitemap.xml. Degrades gracefully to a static-routes-only sitemap when
// the backend isn't reachable at build time (e.g. a CI build with no live
// backend service) rather than failing the build.

import { writeFile } from 'node:fs/promises';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const distDir = resolve(here, '../dist');

const SITE_URL = process.env.VITE_SITE_URL ?? 'https://buildnest.com';
const API_BASE_URL = process.env.SITEMAP_API_BASE_URL ?? process.env.VITE_E2E_BACKEND_URL ?? 'http://localhost:8080';

export const STATIC_ROUTES = ['/', '/products', '/login', '/register', '/privacy-policy'];

export async function fetchJson(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, { signal: AbortSignal.timeout(5000) });
  if (!response.ok) throw new Error(`${path} responded ${response.status}`);
  return response.json();
}

export async function fetchDynamicUrls() {
  const [products, categories] = await Promise.all([
    fetchJson('/api/public/products'),
    fetchJson('/api/public/categories'),
  ]);

  const productUrls = products.map(p => `/products/${p.id}`);
  const categoryUrls = categories.map(c => `/products?category=${c.id}`);
  return [...productUrls, ...categoryUrls];
}

export function buildSitemapXml(paths, siteUrl = SITE_URL) {
  const urlEntries = paths
    .map(path => `  <url>\n    <loc>${siteUrl}${path}</loc>\n  </url>`)
    .join('\n');
  return `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urlEntries}\n</urlset>\n`;
}

async function main() {
  let dynamicUrls = [];
  try {
    dynamicUrls = await fetchDynamicUrls();
  } catch (error) {
    console.warn(
      `[generate-sitemap] Could not reach backend at ${API_BASE_URL} (${error.message}) — ` +
        'writing a static-routes-only sitemap.xml instead.'
    );
  }

  const xml = buildSitemapXml([...STATIC_ROUTES, ...dynamicUrls]);
  await writeFile(resolve(distDir, 'sitemap.xml'), xml, 'utf-8');
  console.log(`[generate-sitemap] Wrote dist/sitemap.xml with ${STATIC_ROUTES.length + dynamicUrls.length} URLs.`);
}

if (import.meta.url === `file://${process.argv[1]}`) {
  main();
}
