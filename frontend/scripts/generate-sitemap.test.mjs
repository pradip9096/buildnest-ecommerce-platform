import { describe, it, expect } from 'vitest';
import { buildSitemapXml, STATIC_ROUTES } from './generate-sitemap.mjs';

describe('buildSitemapXml', () => {
  it('wraps each path in a <url><loc> entry prefixed with the site URL', () => {
    const xml = buildSitemapXml(['/', '/products'], 'https://buildnest.com');

    expect(xml).toContain('<loc>https://buildnest.com/</loc>');
    expect(xml).toContain('<loc>https://buildnest.com/products</loc>');
  });

  it('produces a valid urlset root element', () => {
    const xml = buildSitemapXml(['/'], 'https://buildnest.com');

    expect(xml).toMatch(/^<\?xml version="1\.0" encoding="UTF-8"\?>/);
    expect(xml).toContain('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">');
    expect(xml).toContain('</urlset>');
  });

  it('includes every static route in the fallback (no-backend) path', () => {
    const xml = buildSitemapXml(STATIC_ROUTES, 'https://buildnest.com');

    STATIC_ROUTES.forEach(route => {
      expect(xml).toContain(`<loc>https://buildnest.com${route}</loc>`);
    });
  });

  it('produces an empty urlset (no url entries) for an empty path list', () => {
    const xml = buildSitemapXml([], 'https://buildnest.com');

    expect(xml).not.toContain('<url>');
  });
});
