import { useAsync } from './useAsync';
import { fetchCategories } from '../api/categories';
import type { Category } from '../types';

export function useCategories() {
  const { data, loading } = useAsync<Category[]>(() => fetchCategories(), []);
  return { categories: data ?? [], loading };
}
