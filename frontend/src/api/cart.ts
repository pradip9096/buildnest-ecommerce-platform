import { request, requestData } from './client';
import type { Cart } from '../types';

const BASE = '/api/user/cart';

export async function fetchCart(userId: number): Promise<Cart> {
  return requestData<Cart>(`${BASE}/${userId}`, {}, s => `Failed to fetch cart (${s})`);
}

export async function addToCart(
  userId: number,
  productId: number,
  quantity: number
): Promise<void> {
  await request(
    `${BASE}/add?userId=${userId}`,
    { method: 'POST', body: { productId, quantity } },
    s => `Failed to add item (${s})`
  );
}

export async function removeCartItem(cartItemId: number): Promise<void> {
  await request(`${BASE}/item/${cartItemId}`, { method: 'DELETE' }, s => `Failed to remove item (${s})`);
}

export async function clearCart(userId: number): Promise<void> {
  await request(`${BASE}/clear/${userId}`, { method: 'DELETE' }, s => `Failed to clear cart (${s})`);
}
