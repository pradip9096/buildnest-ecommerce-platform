import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchOrders, fetchOrderById } from './orders';
import type { ApiResponse, Order } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

const order: Order = { id: 1, userId: 42, status: 'PENDING', totalAmount: 100, createdAt: '2026-07-04T00:00:00Z' };

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/orders', () => {
  describe('fetchOrders', () => {
    it('returns the order list on success', async () => {
      const body: ApiResponse<Order[]> = { success: true, message: 'ok', data: [order] };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchOrders();

      expect(fetch).toHaveBeenCalledWith('/api/user/orders', {
        method: undefined,
        credentials: 'include',
        headers: {},
        body: undefined,
      });
      expect(result).toEqual([order]);
    });

    it('throws instead of silently returning an empty list on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));

      await expect(fetchOrders()).rejects.toThrow('Failed to fetch orders (401)');
    });

    it('returns an empty array when data is absent on an otherwise successful response', async () => {
      const body = { success: true, message: 'ok', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchOrders();

      expect(result).toEqual([]);
    });
  });

  describe('fetchOrderById', () => {
    it('returns the order detail on success', async () => {
      const body: ApiResponse<Order> = { success: true, message: 'ok', data: order };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchOrderById(1);

      expect(result).toEqual(order);
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(fetchOrderById(1)).rejects.toThrow('Failed to fetch order (404)');
    });
  });
});
