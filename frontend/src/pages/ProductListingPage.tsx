import { useState, useCallback } from 'react';
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

const DEFAULT_FILTERS: ProductFilters = {
  keyword: '',
  categoryIds: [],
  sort: 'relevance',
  page: 0,
  pageSize: PAGE_SIZE,
};

export function ProductListingPage() {
  const [filters, setFilters] = useState<ProductFilters>(DEFAULT_FILTERS);
  const [searchInput, setSearchInput] = useState('');

  const { products, totalItems, totalPages, loading, error } = useProducts(filters);
  const { categories, loading: categoriesLoading } = useCategories();

  const setPage = useCallback((page: number) => {
    setFilters(f => ({ ...f, page }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const setSort = useCallback((sort: SortOption) => {
    setFilters(f => ({ ...f, sort, page: 0 }));
  }, []);

  const setCategoryIds = useCallback((categoryIds: number[]) => {
    setFilters(f => ({ ...f, categoryIds, page: 0 }));
  }, []);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setFilters(f => ({ ...f, keyword: searchInput.trim(), page: 0 }));
  }

  function handleClearSearch() {
    setSearchInput('');
    setFilters(f => ({ ...f, keyword: '', page: 0 }));
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex flex-col sm:flex-row items-center gap-4">
          <h1 className="text-xl font-bold text-gray-900 shrink-0">🏗️ BuildNest</h1>

          {/* Search bar */}
          <form onSubmit={handleSearch} className="flex-1 flex gap-2 w-full sm:max-w-lg">
            <div className="relative flex-1">
              <input
                type="search"
                value={searchInput}
                onChange={e => setSearchInput(e.target.value)}
                placeholder="Search products…"
                className="w-full border border-gray-300 rounded-lg pl-4 pr-10 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
              {filters.keyword && (
                <button
                  type="button"
                  onClick={handleClearSearch}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-xs"
                  aria-label="Clear search"
                >
                  ✕
                </button>
              )}
            </div>
            <button
              type="submit"
              className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors"
            >
              Search
            </button>
          </form>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
          <p className="text-sm text-gray-500">
            {loading ? 'Loading…' : `${totalItems.toLocaleString()} product${totalItems !== 1 ? 's' : ''}`}
            {filters.keyword && (
              <span> for &ldquo;<strong className="text-gray-700">{filters.keyword}</strong>&rdquo;</span>
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
