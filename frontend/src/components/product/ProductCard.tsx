import { useState, type MouseEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { addToCart } from '../../api/cart';
import type { Product } from '../../types';

interface Props {
  product: Product;
  linkable?: boolean;
}

type AddState = 'idle' | 'loading' | 'added' | 'error' | 'signin';

const BUTTON_LABEL: Record<AddState, string> = {
  idle: 'Add to Cart',
  loading: 'Adding…',
  added: 'Added ✓',
  error: 'Failed — retry',
  signin: 'Sign in to add',
};

const BUTTON_STYLE: Record<AddState, string> = {
  idle: 'bg-primary-500 hover:bg-primary-600 text-white',
  loading: 'bg-primary-500 text-white',
  added: 'bg-green-500 text-white',
  error: 'bg-red-500 text-white',
  signin: 'bg-gray-200 text-gray-600',
};

export function ProductCard({ product, linkable = true }: Props) {
  const { user, token, isAuthenticated } = useAuth();
  const [addState, setAddState] = useState<AddState>('idle');
  const displayPrice = product.discountPrice ?? product.price;
  const hasDiscount = product.discountPrice != null && product.discountPrice < product.price;
  const outOfStock = product.stockQuantity === 0;

  const handleAddToCart = async (e: MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated || !user || !token) {
      setAddState('signin');
      setTimeout(() => setAddState('idle'), 2000);
      return;
    }

    setAddState('loading');
    try {
      await addToCart(user.id, product.id, 1, token);
      setAddState('added');
    } catch {
      setAddState('error');
    } finally {
      setTimeout(() => setAddState('idle'), 2000);
    }
  };

  const card = (
    <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm hover:shadow-md transition-shadow flex flex-col">
      <div className="aspect-square bg-gray-100 flex items-center justify-center overflow-hidden">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        ) : (
          <span className="text-gray-400 text-4xl">🏗️</span>
        )}
      </div>

      <div className="p-4 flex flex-col gap-2 flex-1">
        {product.category && (
          <span className="text-xs font-medium text-primary-600 uppercase tracking-wide">
            {product.category.name}
          </span>
        )}

        <h3 className="text-sm font-semibold text-gray-900 line-clamp-2 leading-snug">
          {product.name}
        </h3>

        <div className="mt-auto flex items-center gap-2">
          <span className="text-base font-bold text-gray-900">
            ₹{displayPrice.toLocaleString('en-IN')}
          </span>
          {hasDiscount && (
            <span className="text-xs text-gray-400 line-through">
              ₹{product.price.toLocaleString('en-IN')}
            </span>
          )}
        </div>

        {outOfStock ? (
          <span className="text-xs text-red-500 font-medium">Out of stock</span>
        ) : (
          <button
            type="button"
            onClick={handleAddToCart}
            disabled={addState === 'loading'}
            className={`w-full text-xs font-semibold py-2 rounded-lg transition-colors disabled:opacity-70 ${BUTTON_STYLE[addState]}`}
          >
            {BUTTON_LABEL[addState]}
          </button>
        )}
      </div>
    </div>
  );

  if (!linkable) return card;
  return (
    <Link to={`/products/${product.id}`} className="block hover:no-underline">
      {card}
    </Link>
  );
}
