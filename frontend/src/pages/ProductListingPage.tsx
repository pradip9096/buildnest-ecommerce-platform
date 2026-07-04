import { useState, useCallback, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useProducts } from '../hooks/useProducts';
import { useCategories } from '../hooks/useCategories';
import { ProductGrid } from '../components/product/ProductGrid';
import { LoadingSkeleton } from '../components/product/LoadingSkeleton';
import { CategorySidebar } from '../components/filters/CategorySidebar';
import { SortDropdown } from '../components/filters/SortDropdown';
import { Pagination } from '../components/common/Pagination';
import { ErrorMessage } from '../components/common/ErrorMessage';
import type { ProductFilters, SortOption } from '../types';

const PAGE_SIZE = 12;

function filtersFromSearchParams(params: URLSearchParams): ProductFilters {
  return {
    keyword: params.get('search') ?? '',
    categoryIds: params.getAll('category').map(Number).filter(n => !Number.isNaN(n)),
    sort: 'relevance',
    page: 0,
    pageSize: PAGE_SIZE,
  };
}

export function ProductListingPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<ProductFilters>(() => filtersFromSearchParams(searchParams));

  const { products, totalItems, totalPages, loading, error } = useProducts(filters);
  const { categories, loading: categoriesLoading } = useCategories();

  // Keep filters in sync when the URL changes — e.g. a search from the global
  // Navbar, or browser back/forward through search history.
  const urlKeyword = searchParams.get('search') ?? '';
  const urlCategoryIds = searchParams.getAll('category').map(Number).filter(n => !Number.isNaN(n)).join(',');
  useEffect(() => {
    setFilters(f => ({
      ...f,
      keyword: urlKeyword,
      categoryIds: urlCategoryIds ? urlCategoryIds.split(',').map(Number) : [],
      page: 0,
    }));
  }, [urlKeyword, urlCategoryIds]);

  const setPage = useCallback((page: number) => {
    setFilters(f => ({ ...f, page }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const setSort = useCallback((sort: SortOption) => {
    setFilters(f => ({ ...f, sort, page: 0 }));
  }, []);

  const setCategoryIds = useCallback((categoryIds: number[]) => {
    setFilters(f => ({ ...f, categoryIds, page: 0 }));
    setSearchParams(prev => {
      const next = new URLSearchParams(prev);
      next.delete('category');
      categoryIds.forEach(id => next.append('category', String(id)));
      return next;
    });
  }, [setSearchParams]);

  function handleClearSearch() {
    setFilters(f => ({ ...f, keyword: '', page: 0 }));
    setSearchParams(prev => {
      const next = new URLSearchParams(prev);
      next.delete('search');
      return next;
    });
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
          <p className="text-sm text-gray-500">
            {loading ? 'Loading…' : `${totalItems.toLocaleString()} product${totalItems !== 1 ? 's' : ''}`}
            {filters.keyword && (
              <>
                <span> for &ldquo;<strong className="text-gray-700">{filters.keyword}</strong>&rdquo;</span>
                <button
                  type="button"
                  onClick={handleClearSearch}
                  className="ml-2 text-indigo-600 hover:underline"
                >
                  Clear
                </button>
              </>
            )}
          </p>
          <SortDropdown value={filters.sort} onChange={setSort} />
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Sidebar */}
          <CategorySidebar
            categories={categories}
            selected={filters.categoryIds}
            onChange={setCategoryIds}
            loading={categoriesLoading}
          />

          {/* Main content */}
          <div className="flex-1 min-w-0">
            {error ? (
              <ErrorMessage
                message={error}
                onRetry={() => setFilters(f => ({ ...f }))}
              />
            ) : loading ? (
              <LoadingSkeleton count={PAGE_SIZE} />
            ) : (
              <>
                <ProductGrid products={products} />
                <Pagination
                  page={filters.page}
                  totalPages={totalPages}
                  onPageChange={setPage}
                />
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
