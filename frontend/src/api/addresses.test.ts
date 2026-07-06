import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from './addresses';
import type { Address, ApiResponse } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

const address: Address = {
  id: 1,
  streetAddress: '123 Main Street',
  city: 'Mumbai',
  state: 'Maharashtra',
  postalCode: '400001',
  country: 'India',
  isDefault: true,
  addressType: 'SHIPPING',
};

const input = {
  streetAddress: '123 Main Street',
  city: 'Mumbai',
  state: 'Maharashtra',
  postalCode: '400001',
  country: 'India',
  addressType: 'SHIPPING',
};

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/addresses', () => {
  describe('fetchAddresses', () => {
    it('returns the address list on success', async () => {
      const body: ApiResponse<Address[]> = { success: true, message: 'ok', data: [address] };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await fetchAddresses();

      expect(fetch).toHaveBeenCalledWith('/api/user/addresses', {
        method: undefined,
        credentials: 'include',
        headers: {},
        body: undefined,
      });
      expect(result).toEqual([address]);
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));

      await expect(fetchAddresses()).rejects.toThrow('Failed to fetch addresses (401)');
    });
  });

  describe('createAddress', () => {
    it('returns the created address', async () => {
      const body: ApiResponse<Address> = { success: true, message: 'ok', data: address };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await createAddress(input);

      expect(result).toEqual(address);
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 400));

      await expect(createAddress(input)).rejects.toThrow('Failed to create address (400)');
    });
  });

  describe('updateAddress', () => {
    it('returns the updated address', async () => {
      const body: ApiResponse<Address> = { success: true, message: 'ok', data: address };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await updateAddress(1, input);

      expect(fetch).toHaveBeenCalledWith('/api/user/addresses/1', expect.objectContaining({ method: 'PUT' }));
      expect(result).toEqual(address);
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(updateAddress(1, input)).rejects.toThrow('Failed to update address (404)');
    });
  });

  describe('deleteAddress', () => {
    it('resolves on success', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

      await expect(deleteAddress(1)).resolves.toBeUndefined();
      expect(fetch).toHaveBeenCalledWith('/api/user/addresses/1', expect.objectContaining({ method: 'DELETE' }));
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 403));

      await expect(deleteAddress(1)).rejects.toThrow('Failed to delete address (403)');
    });
  });

  describe('setDefaultAddress', () => {
    it('returns the updated address', async () => {
      const body: ApiResponse<Address> = { success: true, message: 'ok', data: address };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await setDefaultAddress(1);

      expect(fetch).toHaveBeenCalledWith('/api/user/addresses/1/default', expect.objectContaining({ method: 'PUT' }));
      expect(result).toEqual(address);
    });

    it('throws on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(setDefaultAddress(1)).rejects.toThrow('Failed to set default address (404)');
    });
  });
});
