import { useAsync } from './useAsync';
import { fetchFeaturedProducts } from '../api/products';
import type { Product } from '../types';

export function useFeaturedProducts() {
  const { data, loading } = useAsync<Product[]>(() => fetchFeaturedProducts(), []);
  return { products: data ?? [], loading };
}
