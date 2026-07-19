import { useState, useEffect, type MouseEvent } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { isInWishlist, addToWishlist, removeFromWishlist } from '../../api/wishlist';

type Props = {
  productId: number;
  className?: string;
};

export function WishlistButton({ productId, className = '' }: Props) {
  const { isAuthenticated } = useAuth();
  const [inWishlist, setInWishlist] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      setInWishlist(false);
      return;
    }
    let cancelled = false;
    isInWishlist(productId)
      .then(result => {
        if (!cancelled) setInWishlist(result);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, productId]);

  const handleToggle = async (e: MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated || loading) return;

    setLoading(true);
    const next = !inWishlist;
    setInWishlist(next);
    try {
      if (next) await addToWishlist(productId);
      else await removeFromWishlist(productId);
    } catch {
      setInWishlist(!next);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) return null;

  return (
    <button
      type="button"
      onClick={handleToggle}
      disabled={loading}
      aria-label={inWishlist ? 'Remove from wishlist' : 'Add to wishlist'}
      aria-pressed={inWishlist}
      className={`text-lg leading-none disabled:opacity-60 transition-colors ${
        inWishlist ? 'text-red-500' : 'text-gray-300 hover:text-red-400'
      } ${className}`}
    >
      {inWishlist ? '❤️' : '🤍'}
    </button>
  );
}
