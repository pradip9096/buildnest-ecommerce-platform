import { requestData } from './client';
import type { Product } from '../types';

export async function fetchProducts(): Promise<Product[]> {
  const data = await requestData<Product[]>('/api/public/products', {}, s => `Failed to fetch products: ${s}`);
  return data ?? [];
}

export async function fetchProductById(id: number): Promise<Product> {
  const data = await requestData<Product>(
    `/api/public/products/${id}`,
    {},
    s => `Failed to fetch product: ${s}`
  );
  if (!data) throw new Error('Product not found');
  return data;
}

export async function searchProducts(keyword: string): Promise<Product[]> {
  const params = new URLSearchParams({ keyword });
  const data = await requestData<Product[]>(
    `/api/public/products/search?${params}`,
    {},
    s => `Search failed: ${s}`
  );
  return data ?? [];
}

export async function fetchFeaturedProducts(): Promise<Product[]> {
  const data = await requestData<Product[]>(
    '/api/public/products/featured',
    {},
    s => `Failed to fetch featured products: ${s}`
  );
  return data ?? [];
}
