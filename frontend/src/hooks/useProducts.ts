import { useState, useEffect } from 'react';
import { fetchProducts, searchProducts } from '../api/products';
import type { Product, ProductFilters, SortOption } from '../types';

function sortProducts(products: Product[], sort: SortOption): Product[] {
  const copy = [...products];
  switch (sort) {
    case 'price_asc':
      return copy.sort((a, b) => (a.discountPrice ?? a.price) - (b.discountPrice ?? b.price));
    case 'price_desc':
      return copy.sort((a, b) => (b.discountPrice ?? b.price) - (a.discountPrice ?? a.price));
    case 'newest':
      return copy.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    default:
      return copy;
  }
}

export function useProducts(filters: ProductFilters) {
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const keyword = filters.keyword.trim();

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const request = keyword ? searchProducts(keyword) : fetchProducts();

    request
      .then(data => {
        if (!cancelled) setAllProducts(data);
      })
      .catch(err => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Unknown error');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [keyword]);

  const filtered = allProducts.filter(p => {
    if (filters.categoryIds.length > 0) {
      return p.category != null && filters.categoryIds.includes(p.category.id);
    }
    return true;
  });

  const sorted = sortProducts(filtered, filters.sort);
  const totalItems = sorted.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / filters.pageSize));
  const safePage = Math.min(filters.page, totalPages - 1);
  const paginated = sorted.slice(safePage * filters.pageSize, (safePage + 1) * filters.pageSize);

  return { products: paginated, totalItems, totalPages, loading, error };
}
