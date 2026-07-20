import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchInventoryStatus } from './inventory';
import type { ApiResponse, InventoryStatusInfo } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

const statusInfo: InventoryStatusInfo = {
  productId: 5,
  status: 'IN_STOCK',
  displayName: 'In Stock',
  description: 'Product is available',
};

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/inventory', () => {
  describe('fetchInventoryStatus', () => {
    it('returns the inventory status on a 2xx response', async () => {
      const body: ApiResponse<InventoryStatusInfo> = { success: true, message: 'ok', data: statusInfo };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchInventoryStatus(5);

      expect(fetch).toHaveBeenCalledWith('/api/inventory/5/status', expect.objectContaining({ method: undefined }));
      expect(result).toEqual(statusInfo);
    });

    it('throws when data is null', async () => {
      const body: ApiResponse<InventoryStatusInfo | null> = { success: true, message: 'ok', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      await expect(fetchInventoryStatus(5)).rejects.toThrow('Inventory status not found');
    });

    it('throws on a non-2xx response', async () => {
      const body: ApiResponse<null> = { success: false, message: 'Product not found', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, false, 404));

      await expect(fetchInventoryStatus(999)).rejects.toThrow('Product not found');
    });
  });
});
