import { requestData } from './client';
import type { Category } from '../types';

export async function fetchCategories(): Promise<Category[]> {
  const data = await requestData<Category[]>('/api/public/categories', {}, s => `Failed to fetch categories: ${s}`);
  return data ?? [];
}
