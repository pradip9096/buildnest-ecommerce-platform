import type { ApiResponse, Cart } from '../types';

const BASE = '/api/user/cart';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

export async function fetchCart(userId: number, token: string): Promise<Cart> {
  const res = await fetch(`${BASE}/${userId}`, { headers: authHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch cart (${res.status})`);
  const body: ApiResponse<Cart> = await res.json();
  return body.data;
}

export async function addToCart(
  userId: number,
  productId: number,
  quantity: number,
  token: string
): Promise<void> {
  const res = await fetch(`${BASE}/add?userId=${userId}`, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify({ productId, quantity }),
  });
  if (!res.ok) throw new Error(`Failed to add item (${res.status})`);
}

export async function removeCartItem(cartItemId: number, token: string): Promise<void> {
  const res = await fetch(`${BASE}/item/${cartItemId}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to remove item (${res.status})`);
}

export async function clearCart(userId: number, token: string): Promise<void> {
  const res = await fetch(`${BASE}/clear/${userId}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to clear cart (${res.status})`);
}
