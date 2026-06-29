import type { ApiResponse, Product } from '../types';

export async function fetchProducts(): Promise<Product[]> {
  const res = await fetch('/api/public/products');
  if (!res.ok) throw new Error(`Failed to fetch products: ${res.status}`);
  const json: ApiResponse<Product[]> = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data ?? [];
}

export async function searchProducts(keyword: string): Promise<Product[]> {
  const params = new URLSearchParams({ keyword });
  const res = await fetch(`/api/public/products/search?${params}`);
  if (!res.ok) throw new Error(`Search failed: ${res.status}`);
  const json: ApiResponse<Product[]> = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data ?? [];
}
