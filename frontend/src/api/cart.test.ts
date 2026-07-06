import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchCart, addToCart, removeCartItem, clearCart } from './cart';
import type { Cart, ApiResponse } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

const cart: Cart = { cartId: 1, userId: 42, items: [], totalAmount: 0 };

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/cart', () => {
  describe('fetchCart', () => {
    it('returns the cart data on a 2xx response', async () => {
      const body: ApiResponse<Cart> = { success: true, message: 'ok', data: cart };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchCart(42);

      expect(fetch).toHaveBeenCalledWith('/api/user/cart/42', {
        method: undefined,
        credentials: 'include',
        headers: {},
        body: undefined,
      });
      expect(result).toEqual(cart);
    });

    it('throws with the status code on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));

      await expect(fetchCart(42)).rejects.toThrow('Failed to fetch cart (401)');
    });
  });

  describe('addToCart', () => {
    it('posts the item and does not throw on success', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(addToCart(42, 5, 2)).resolves.toBeUndefined();

      expect(fetch).toHaveBeenCalledWith('/api/user/cart/add?userId=42', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ productId: 5, quantity: 2 }),
      }));
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 409));

      await expect(addToCart(42, 5, 2)).rejects.toThrow('Failed to add item (409)');
    });
  });

  describe('removeCartItem', () => {
    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(removeCartItem(9)).rejects.toThrow('Failed to remove item (404)');
    });
  });

  describe('clearCart', () => {
    it('resolves on success', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(clearCart(42)).resolves.toBeUndefined();
    });
  });
});
