import { Link } from 'react-router-dom';
import type { Product } from '../../types';

interface Props {
  product: Product;
  linkable?: boolean;
}

export function ProductCard({ product, linkable = true }: Props) {
  const displayPrice = product.discountPrice ?? product.price;
  const hasDiscount = product.discountPrice != null && product.discountPrice < product.price;

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

        {product.stockQuantity === 0 && (
          <span className="text-xs text-red-500 font-medium">Out of stock</span>
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
