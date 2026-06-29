import type { ApiResponse, Category } from '../types';

export async function fetchCategories(): Promise<Category[]> {
  const res = await fetch('/api/public/categories');
  if (!res.ok) throw new Error(`Failed to fetch categories: ${res.status}`);
  const json: ApiResponse<Category[]> = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data ?? [];
}
