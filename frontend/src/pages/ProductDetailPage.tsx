import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useProduct } from '../hooks/useProduct';
import { useReviews } from '../hooks/useReviews';
import { useAuth } from '../hooks/useAuth';
import { fetchProducts } from '../api/products';
import { addToCart } from '../api/cart';
import { ImageGallery } from '../components/product/ImageGallery';
import { WishlistButton } from '../components/product/WishlistButton';
import type { Product } from '../types';
import { StarRating } from '../components/product/StarRating';
import { QuantitySelector } from '../components/product/QuantitySelector';
import { ReviewsSection } from '../components/product/ReviewsSection';
import { WriteReviewForm } from '../components/product/WriteReviewForm';
import { RelatedProducts } from '../components/product/RelatedProducts';
import { ErrorMessage } from '../components/common/ErrorMessage';

function ProductDetailSkeleton() {
  return (
    <div className="max-w-6xl mx-auto px-4 py-8 animate-pulse">
      <div className="h-4 bg-gray-200 rounded w-48 mb-8" />
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
        <div className="aspect-square bg-gray-200 rounded-2xl" />
        <div className="flex flex-col gap-4">
          <div className="h-3 bg-gray-200 rounded w-20" />
          <div className="h-7 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-24" />
          <div className="h-8 bg-gray-200 rounded w-32" />
          <div className="h-4 bg-gray-200 rounded w-full" />
          <div className="h-4 bg-gray-200 rounded w-5/6" />
          <div className="h-10 bg-gray-200 rounded w-40 mt-4" />
          <div className="h-12 bg-gray-200 rounded w-full mt-2" />
        </div>
      </div>
    </div>
  );
}

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const productId = Number(id);

  const { product, loading: productLoading, error: productError } = useProduct(productId);
  const [reviewPage, setReviewPage] = useState(0);
  const { reviews, summary, totalPages, loading: reviewsLoading, refetch: refetchReviews } = useReviews(productId, reviewPage);
  const [allProducts, setAllProducts] = useState<Product[]>([]);

  useEffect(() => {
    fetchProducts().then(setAllProducts).catch(() => {});
  }, []);

  const { user, isAuthenticated } = useAuth();
  const [quantity, setQuantity] = useState(1);
  const [cartMessage, setCartMessage] = useState<string | null>(null);
  const [cartAdding, setCartAdding] = useState(false);

  useEffect(() => {
    if (product) {
      document.title = `${product.name} | BuildNest`;
      const setMeta = (property: string, content: string) => {
        let el = document.querySelector<HTMLMetaElement>(`meta[property="${property}"]`);
        if (!el) {
          el = document.createElement('meta');
          el.setAttribute('property', property);
          document.head.appendChild(el);
        }
        el.setAttribute('content', content);
      };
      setMeta('og:title', `${product.name} | BuildNest`);
      setMeta('og:description', product.description ?? `Buy ${product.name} at BuildNest`);
      if (product.imageUrl) setMeta('og:image', product.imageUrl);
    }
    return () => { document.title = 'BuildNest'; };
  }, [product]);

  async function handleAddToCart() {
    if (!isAuthenticated || !user) {
      setCartMessage('Please sign in to add items to your cart.');
      setTimeout(() => setCartMessage(null), 4000);
      return;
    }
    setCartAdding(true);
    try {
      await addToCart(user.id, product!.id, quantity);
      setCartMessage(`Added ${quantity} item${quantity > 1 ? 's' : ''} to your cart.`);
      setTimeout(() => setCartMessage(null), 4000);
    } catch {
      setCartMessage('Failed to add to cart. Please try again.');
      setTimeout(() => setCartMessage(null), 4000);
    } finally {
      setCartAdding(false);
    }
  }

  if (productLoading) return <ProductDetailSkeleton />;

  if (productError || !product) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-16 flex flex-col items-center gap-4">
        <ErrorMessage message={productError ?? 'Product not found'} />
        <Link to="/products" className="text-sm text-primary-600 hover:underline">← Back to products</Link>
      </div>
    );
  }

  const displayPrice = product.discountPrice ?? product.price;
  const hasDiscount = product.discountPrice != null && product.discountPrice < product.price;
  const discountPct = hasDiscount
    ? Math.round(((product.price - product.discountPrice!) / product.price) * 100)
    : 0;
  const inStock = product.stockQuantity > 0;

  return (
    <div className="min-h-screen bg-white">
      <main className="max-w-6xl mx-auto px-4 py-8">
        <nav className="text-sm text-gray-500 mb-6 flex items-center gap-1.5 flex-wrap">
          <Link to="/" className="hover:text-primary-600">Home</Link>
          <span>/</span>
          {product.category && (
            <>
              <Link to={`/products?category=${product.category.id}`} className="hover:text-primary-600">
                {product.category.name}
              </Link>
              <span>/</span>
            </>
          )}
          <span className="text-gray-900 font-medium truncate max-w-[200px]">{product.name}</span>
        </nav>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-10 lg:gap-16">
          <ImageGallery imageUrl={product.imageUrl} productName={product.name} />

          <div className="flex flex-col gap-4">
            {product.category && (
              <span className="text-xs font-semibold text-primary-600 uppercase tracking-wider">
                {product.category.name}
              </span>
            )}

            <div className="flex items-start justify-between gap-3">
              <h1 className="text-2xl lg:text-3xl font-bold text-gray-900 leading-tight">{product.name}</h1>
              <WishlistButton productId={product.id} className="shrink-0 mt-1" />
            </div>

            {summary && summary.totalReviews > 0 && (
              <a href="#reviews" className="flex items-center gap-2 w-fit">
                <StarRating rating={summary.averageRating} size="sm" />
                <span className="text-sm text-gray-500">{summary.totalReviews} review{summary.totalReviews !== 1 ? 's' : ''}</span>
              </a>
            )}

            <div className="flex items-baseline gap-3 mt-1">
              <span className="text-3xl font-bold text-gray-900">₹{displayPrice.toLocaleString('en-IN')}</span>
              {hasDiscount && (
                <>
                  <span className="text-lg text-gray-400 line-through">₹{product.price.toLocaleString('en-IN')}</span>
                  <span className="text-sm font-semibold text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                    {discountPct}% off
                  </span>
                </>
              )}
            </div>

            <div className="flex items-center gap-2">
              {inStock ? (
                <span className="text-sm font-medium text-green-600">
                  ✓ In Stock ({product.stockQuantity} available)
                </span>
              ) : (
                <span className="text-sm font-medium text-red-500">✗ Out of Stock</span>
              )}
            </div>

            {product.description && (
              <p className="text-sm text-gray-600 leading-relaxed">{product.description}</p>
            )}

            <p className="text-xs text-gray-400">SKU: {product.sku}</p>

            {inStock && (
              <div className="flex flex-col gap-4 mt-4 pt-4 border-t border-gray-100">
                <div className="flex items-center gap-4">
                  <span className="text-sm font-medium text-gray-700">Quantity</span>
                  <QuantitySelector value={quantity} max={product.stockQuantity} onChange={setQuantity} />
                </div>

                <button
                  type="button"
                  onClick={handleAddToCart}
                  disabled={cartAdding}
                  className="w-full bg-primary-600 hover:bg-primary-700 active:bg-primary-800 disabled:opacity-60 text-white font-semibold py-3 px-6 rounded-xl transition-colors"
                >
                  {cartAdding ? 'Adding…' : 'Add to Cart'}
                </button>

                {cartMessage && (
                  <p className={`text-sm rounded-lg px-4 py-2 text-center ${
                    cartMessage.startsWith('Added')
                      ? 'text-green-700 bg-green-50 border border-green-200'
                      : 'text-amber-600 bg-amber-50 border border-amber-200'
                  }`}>
                    {cartMessage}
                    {cartMessage.startsWith('Added') && (
                      <Link to="/cart" className="ml-2 underline font-medium">View Cart →</Link>
                    )}
                  </p>
                )}
              </div>
            )}

            {!inStock && (
              <button
                type="button"
                disabled
                className="w-full mt-4 bg-gray-100 text-gray-400 font-semibold py-3 px-6 rounded-xl cursor-not-allowed"
              >
                Out of Stock
              </button>
            )}
          </div>
        </div>

        <div id="reviews">
          <ReviewsSection
            reviews={reviews}
            summary={summary}
            totalPages={totalPages}
            page={reviewPage}
            loading={reviewsLoading}
            onPageChange={setReviewPage}
          />
          <WriteReviewForm
            productId={product.id}
            isAuthenticated={isAuthenticated}
            onSubmitted={refetchReviews}
          />
        </div>

        <RelatedProducts
          currentProductId={product.id}
          categoryId={product.category?.id}
          products={allProducts}
        />
      </main>
    </div>
  );
}
