import { Link } from 'react-router-dom';
import { useCategories } from '../hooks/useCategories';
import { useFeaturedProducts } from '../hooks/useFeaturedProducts';
import { ProductGrid } from '../components/product/ProductGrid';
import { LoadingSkeleton } from '../components/product/LoadingSkeleton';

// Static placeholder pending #290 (backend-driven promotional content) — deliberately
// temporary, see issue #290 for the deferred backend/admin-UI version.
const PROMO_BANNER = {
  headline: 'Build Smarter. Build Stronger.',
  subhead: 'Everything you need for your next construction project, delivered to your door.',
};

export function HomePage() {
  const { categories, loading: categoriesLoading } = useCategories();
  const { products: featuredProducts, loading: featuredLoading } = useFeaturedProducts();

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-12">
        <section className="bg-primary-600 rounded-2xl px-8 py-12 sm:py-16 text-center text-white">
          <h1 className="text-2xl sm:text-4xl font-bold mb-3">{PROMO_BANNER.headline}</h1>
          <p className="text-primary-100 text-sm sm:text-base max-w-xl mx-auto mb-6">
            {PROMO_BANNER.subhead}
          </p>
          <Link
            to="/products"
            className="inline-block bg-white text-primary-600 font-semibold px-6 py-3 rounded-xl hover:bg-primary-50 transition-colors"
          >
            Shop All Products
          </Link>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Shop by Category</h2>
          {categoriesLoading ? (
            <div className="flex gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-20 w-32 bg-gray-200 rounded-xl animate-pulse" />
              ))}
            </div>
          ) : (
            <div className="flex flex-wrap gap-3">
              {categories.map(category => (
                <Link
                  key={category.id}
                  to={`/products?category=${category.id}`}
                  className="px-5 py-4 bg-white border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:border-primary-400 hover:text-primary-600 transition-colors"
                >
                  {category.name}
                </Link>
              ))}
            </div>
          )}
        </section>

        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Featured Products</h2>
            <Link to="/products" className="text-sm text-primary-600 hover:underline">
              View all →
            </Link>
          </div>
          {featuredLoading ? (
            <LoadingSkeleton count={4} />
          ) : featuredProducts.length === 0 ? (
            <p className="text-sm text-gray-500 py-8 text-center">
              No featured products yet — check back soon.
            </p>
          ) : (
            <ProductGrid products={featuredProducts} />
          )}
        </section>
      </main>
    </div>
  );
}
