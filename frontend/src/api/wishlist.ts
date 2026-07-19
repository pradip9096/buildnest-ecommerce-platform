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

export async function clearWishlist(): Promise<void> {
  await request('/api/user/wishlist', { method: 'DELETE' }, 'Failed to clear wishlist');
}

export async function getWishlistCount(): Promise<number> {
  const data = await requestData<number>('/api/user/wishlist/count', {}, 'Failed to load wishlist count');
  return data ?? 0;
}

export async function isInWishlist(productId: number): Promise<boolean> {
  const data = await requestData<boolean>(
    `/api/user/wishlist/contains/${productId}`,
    {},
    'Failed to check wishlist status'
  );
  return data ?? false;
}
