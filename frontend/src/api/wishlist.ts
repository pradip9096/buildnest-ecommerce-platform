import { request, requestData } from './client';
import type { Product } from '../types';

export async function fetchWishlist(): Promise<Product[]> {
  const data = await requestData<Product[]>('/api/user/wishlist', {}, 'Failed to load wishlist');
  return data ?? [];
}

export async function addToWishlist(productId: number): Promise<void> {
  await request(`/api/user/wishlist/items/${productId}`, { method: 'POST' }, 'Failed to add to wishlist');
}

export async function removeFromWishlist(productId: number): Promise<void> {
  await request(
    `/api/user/wishlist/items/${productId}`,
    { method: 'DELETE' },
    'Failed to remove from wishlist'
  );
}
