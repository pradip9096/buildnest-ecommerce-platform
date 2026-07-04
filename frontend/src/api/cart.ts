import { request, requestData } from './client';
import type { Cart } from '../types';

const BASE = '/api/user/cart';

export async function fetchCart(userId: number, token: string): Promise<Cart> {
  return requestData<Cart>(`${BASE}/${userId}`, { token }, s => `Failed to fetch cart (${s})`);
}

export async function addToCart(
  userId: number,
  productId: number,
  quantity: number,
  token: string
): Promise<void> {
  await request(
    `${BASE}/add?userId=${userId}`,
    { method: 'POST', token, body: { productId, quantity } },
    s => `Failed to add item (${s})`
  );
}

export async function removeCartItem(cartItemId: number, token: string): Promise<void> {
  await request(`${BASE}/item/${cartItemId}`, { method: 'DELETE', token }, s => `Failed to remove item (${s})`);
}

export async function clearCart(userId: number, token: string): Promise<void> {
  await request(`${BASE}/clear/${userId}`, { method: 'DELETE', token }, s => `Failed to clear cart (${s})`);
}
