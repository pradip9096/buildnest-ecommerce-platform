import type { ApiResponse, Product } from '../types';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}` };
}

export async function fetchWishlist(token: string): Promise<Product[]> {
  const res = await fetch('/api/user/wishlist', { headers: authHeaders(token) });
  const body: ApiResponse<Product[]> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load wishlist');
  return Array.isArray(body.data) ? body.data : [];
}

export async function addToWishlist(productId: number, token: string): Promise<void> {
  await fetch(`/api/user/wishlist/items/${productId}`, {
    method: 'POST',
    headers: authHeaders(token),
  });
}

export async function removeFromWishlist(productId: number, token: string): Promise<void> {
  await fetch(`/api/user/wishlist/items/${productId}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
}
