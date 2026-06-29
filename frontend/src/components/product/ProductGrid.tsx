import type { Product } from '../../types';
import { ProductCard } from './ProductCard';

interface Props {
  products: Product[];
}

export function ProductGrid({ products }: Props) {
  if (products.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-gray-400">
        <span className="text-5xl mb-4">🔍</span>
        <p className="text-lg font-medium">No products found</p>
        <p className="text-sm mt-1">Try adjusting your filters or search term</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {products.map(p => (
        <ProductCard key={p.id} product={p} />
      ))}
    </div>
  );
}
