import { describe, it, expect } from 'vitest';
import { render, waitFor } from '@testing-library/react';
import { HelmetProvider } from 'react-helmet-async';
import { SeoMeta } from './SeoMeta';

function renderSeoMeta(props: React.ComponentProps<typeof SeoMeta>) {
  return render(
    <HelmetProvider>
      <SeoMeta {...props} />
    </HelmetProvider>
  );
}

describe('SeoMeta', () => {
  it('sets the document title suffixed with the site name', async () => {
    renderSeoMeta({ title: 'Shop All Products', description: 'Browse our catalog.' });

    await waitFor(() => expect(document.title).toBe('Shop All Products | BuildNest'));
  });

  it('renders the meta description', async () => {
    renderSeoMeta({ title: 'Home', description: 'Everything for your next project.' });

    await waitFor(() => {
      const meta = document.querySelector('meta[name="description"]');
      expect(meta?.getAttribute('content')).toBe('Everything for your next project.');
    });
  });

  it('renders Open Graph tags with the product type and provided image', async () => {
    renderSeoMeta({
      title: 'Cordless Drill',
      description: 'A powerful cordless drill.',
      image: 'https://cdn.example.com/drill.jpg',
      type: 'product',
    });

    await waitFor(() => {
      expect(document.querySelector('meta[property="og:title"]')?.getAttribute('content')).toBe(
        'Cordless Drill | BuildNest'
      );
      expect(document.querySelector('meta[property="og:type"]')?.getAttribute('content')).toBe('product');
      expect(document.querySelector('meta[property="og:image"]')?.getAttribute('content')).toBe(
        'https://cdn.example.com/drill.jpg'
      );
    });
  });

  it('renders Twitter Card tags', async () => {
    renderSeoMeta({ title: 'Home', description: 'Everything for your next project.' });

    await waitFor(() => {
      expect(document.querySelector('meta[name="twitter:card"]')?.getAttribute('content')).toBe(
        'summary_large_image'
      );
    });
  });

  it('falls back to the default image when none is provided', async () => {
    renderSeoMeta({ title: 'Home', description: 'Everything for your next project.' });

    await waitFor(() => {
      expect(document.querySelector('meta[property="og:image"]')?.getAttribute('content')).toBe('/favicon.svg');
    });
  });
});
