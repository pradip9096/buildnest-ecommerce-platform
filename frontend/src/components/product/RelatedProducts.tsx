import { Link } from 'react-router-dom';
import { ProductCard } from './ProductCard';
import type { Product } from '../../types';

interface Props {
  currentProductId: number;
  categoryId?: number;
  products: Product[];
}

export function RelatedProducts({ currentProductId, categoryId, products }: Props) {
  const related = products
    .filter(p => p.id !== currentProductId && p.category?.id === categoryId && p.isActive)
    .slice(0, 4);

  if (related.length === 0) return null;

  return (
    <section className="mt-12">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Related Products</h2>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {related.map(product => (
          <Link key={product.id} to={`/products/${product.id}`} className="block hover:no-underline">
            <ProductCard product={product} linkable={false} />
          </Link>
        ))}
      </div>
    </section>
  );
}
