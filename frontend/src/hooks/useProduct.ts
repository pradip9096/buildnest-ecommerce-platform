import { useAsync } from './useAsync';
import { fetchProductById } from '../api/products';
import type { Product } from '../types';

interface UseProductResult {
  product: Product | null;
  loading: boolean;
  error: string | null;
}

export function useProduct(id: number): UseProductResult {
  const { data: product, loading, error } = useAsync<Product>(() => fetchProductById(id), [id]);
  return { product, loading, error };
}
