import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { fetchWishlist, removeFromWishlist } from '../../api/wishlist';
import { addToCart } from '../../api/cart';
import type { Product } from '../../types';

interface Props { token: string; userId: number; }

export function WishlistTab({ token, userId }: Props) {
  const [items, setItems] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [removing, setRemoving] = useState<number | null>(null);
  const [movingToCart, setMovingToCart] = useState<number | null>(null);
  const [feedback, setFeedback] = useState<{ id: number; msg: string } | null>(null);

  useEffect(() => {
    fetchWishlist(token)
      .then(setItems)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load wishlist'))
      .finally(() => setLoading(false));
  }, [token]);

  const handleRemove = async (productId: number) => {
    setRemoving(productId);
    try {
      await removeFromWishlist(productId, token);
      setItems(prev => prev.filter(p => p.id !== productId));
    } catch {
      // silently ignore
    } finally {
      setRemoving(null);
    }
  };

  const handleMoveToCart = async (product: Product) => {
    setMovingToCart(product.id);
    try {
      await addToCart(userId, product.id, 1, token);
      await removeFromWishlist(product.id, token);
      setItems(prev => prev.filter(p => p.id !== product.id));
      setFeedback({ id: product.id, msg: `${product.name} moved to cart.` });
      setTimeout(() => setFeedback(null), 3000);
    } catch {
      setFeedback({ id: product.id, msg: 'Failed to move to cart.' });
      setTimeout(() => setFeedback(null), 3000);
    } finally {
      setMovingToCart(null);
    }
  };

  if (loading) return (
    <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 animate-pulse">
      {[1,2,3].map(i => <div key={i} className="h-48 bg-gray-100 rounded-xl" />)}
    </div>
  );

  if (error) return <p className="text-red-600 text-sm">{error}</p>;

  return (
    <>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        Wishlist {items.length > 0 && <span className="text-sm font-normal text-gray-400">({items.length} items)</span>}
      </h2>

      {feedback && (
        <p className="mb-4 text-sm text-green-700 bg-green-50 border border-green-200 rounded-xl px-3 py-2">{feedback.msg}</p>
      )}

      {items.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-4xl mb-3">❤️</div>
          <p className="text-gray-500 mb-4">Your wishlist is empty.</p>
          <Link to="/products" className="inline-block bg-primary-500 hover:bg-primary-600 text-white font-semibold px-5 py-2.5 rounded-xl text-sm transition-colors">
            Browse Products
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map(product => (
            <div key={product.id} className="border border-gray-100 rounded-xl overflow-hidden hover:shadow-sm transition-shadow">
              <Link to={`/products/${product.id}`} className="block">
                <div className="h-36 bg-gray-50 flex items-center justify-center text-3xl">
                  {product.imageUrl
                    ? <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
                    : '🏗️'}
                </div>
                <div className="p-3">
                  <p className="font-medium text-gray-900 text-sm truncate">{product.name}</p>
                  <p className="text-primary-600 font-semibold text-sm mt-1">
                    ₹{(product.discountPrice ?? product.price).toFixed(2)}
                  </p>
                </div>
              </Link>
              <div className="px-3 pb-3 flex gap-2">
                <button
                  onClick={() => handleMoveToCart(product)}
                  disabled={movingToCart === product.id || !product.isActive || product.stockQuantity === 0}
                  className="flex-1 bg-primary-500 hover:bg-primary-600 disabled:opacity-50 text-white text-xs font-semibold py-2 rounded-lg transition-colors"
                >
                  {movingToCart === product.id ? 'Moving…' : 'Move to Cart'}
                </button>
                <button
                  onClick={() => handleRemove(product.id)}
                  disabled={removing === product.id}
                  className="border border-gray-200 text-gray-500 hover:text-red-500 hover:border-red-300 disabled:opacity-50 text-xs px-2.5 py-2 rounded-lg transition-colors"
                  aria-label="Remove from wishlist"
                >
                  {removing === product.id ? '…' : '✕'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
