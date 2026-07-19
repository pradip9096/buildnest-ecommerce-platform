import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchWishlist, addToWishlist, removeFromWishlist, clearWishlist, getWishlistCount, isInWishlist } from './wishlist';
import type { Product, ApiResponse } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

const product: Product = {
  id: 5,
  name: 'Hammer',
  price: 100,
  stockQuantity: 10,
  isActive: true,
} as Product;

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/wishlist', () => {
  describe('fetchWishlist', () => {
    it('returns the wishlist products on a 2xx response', async () => {
      const body: ApiResponse<Product[]> = { success: true, message: 'ok', data: [product] };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchWishlist();

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist', expect.objectContaining({ method: undefined }));
      expect(result).toEqual([product]);
    });

    it('returns an empty array when data is null', async () => {
      const body: ApiResponse<Product[] | null> = { success: true, message: 'ok', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      expect(await fetchWishlist()).toEqual([]);
    });
  });

  describe('addToWishlist', () => {
    it('posts to the items endpoint', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(addToWishlist(5)).resolves.toBeUndefined();

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist/items/5', expect.objectContaining({ method: 'POST' }));
    });
  });

  describe('removeFromWishlist', () => {
    it('deletes the item', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(removeFromWishlist(5)).resolves.toBeUndefined();

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist/items/5', expect.objectContaining({ method: 'DELETE' }));
    });
  });

  describe('clearWishlist', () => {
    it('sends a DELETE to the wishlist root', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(clearWishlist()).resolves.toBeUndefined();

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist', expect.objectContaining({ method: 'DELETE' }));
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

      await expect(clearWishlist()).rejects.toThrow('Failed to clear wishlist');
    });
  });

  describe('getWishlistCount', () => {
    it('returns the count on a 2xx response', async () => {
      const body: ApiResponse<number> = { success: true, message: 'ok', data: 3 };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      expect(await getWishlistCount()).toBe(3);

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist/count', expect.objectContaining({ method: undefined }));
    });

    it('returns 0 when data is null', async () => {
      const body: ApiResponse<number | null> = { success: true, message: 'ok', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      expect(await getWishlistCount()).toBe(0);
    });
  });

  describe('isInWishlist', () => {
    it('returns true when the product is in the wishlist', async () => {
      const body: ApiResponse<boolean> = { success: true, message: 'ok', data: true };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      expect(await isInWishlist(5)).toBe(true);

      expect(fetch).toHaveBeenCalledWith('/api/user/wishlist/contains/5', expect.objectContaining({ method: undefined }));
    });

    it('returns false when data is null', async () => {
      const body: ApiResponse<boolean | null> = { success: true, message: 'ok', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      expect(await isInWishlist(5)).toBe(false);
    });
  });
});
