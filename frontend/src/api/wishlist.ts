import { request, requestData } from './client';
import type { Product } from '../types';

export async function fetchWishlist(token: string): Promise<Product[]> {
  const data = await requestData<Product[]>('/api/user/wishlist', { token }, 'Failed to load wishlist');
  return data ?? [];
}

export async function addToWishlist(productId: number, token: string): Promise<void> {
  await request(`/api/user/wishlist/items/${productId}`, { method: 'POST', token }, 'Failed to add to wishlist');
}

export async function removeFromWishlist(productId: number, token: string): Promise<void> {
  await request(
    `/api/user/wishlist/items/${productId}`,
    { method: 'DELETE', token },
    'Failed to remove from wishlist'
  );
}
