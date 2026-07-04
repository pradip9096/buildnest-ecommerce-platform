import { describe, it, expect, vi, beforeEach } from 'vitest';
import { request, requestData, ApiError } from './client';
import type { ApiResponse } from '../types';

function jsonResponse(body: unknown, ok: boolean, status = 200): Response {
  return {
    ok,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

describe('api/client', () => {
  describe('request', () => {
    it('sends a GET with no body and no Content-Type header by default', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      const result = await request<{ ok: boolean }>('/api/thing');

      expect(fetch).toHaveBeenCalledWith('/api/thing', { headers: {}, body: undefined });
      expect(result).toEqual({ ok: true });
    });

    it('injects the Authorization header when a token is provided', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { token: 'abc123' });

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        headers: { Authorization: 'Bearer abc123' },
        body: undefined,
      });
    });

    it('serializes a body and sets Content-Type: application/json', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { method: 'POST', body: { a: 1 } });

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ a: 1 }),
      });
    });

    it('returns undefined for a 204 No Content response without parsing the body', async () => {
      const res = jsonResponse(undefined, true, 204);
      vi.mocked(fetch).mockResolvedValue(res);
      const jsonSpy = vi.spyOn(res, 'json');

      const result = await request('/api/thing');

      expect(result).toBeUndefined();
      expect(jsonSpy).not.toHaveBeenCalled();
    });

    it('prefers the backend error message over the fallback on a non-2xx response', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: false, message: 'Not allowed' }, false, 403));

      await expect(request('/api/thing', {}, 'Fallback message')).rejects.toThrow('Not allowed');
    });

    it('uses the string fallback message when the error body has no message', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

      await expect(request('/api/thing', {}, 'Something broke')).rejects.toThrow('Something broke');
    });

    it('uses a function fallback message with the status code when the error body has no message', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(request('/api/thing', {}, s => `Not found (${s})`)).rejects.toThrow('Not found (404)');
    });

    it('uses a generic fallback message when none is provided', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

      await expect(request('/api/thing')).rejects.toThrow('Request failed (500)');
    });

    it('throws an ApiError carrying the response status', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));

      const error = await request('/api/thing').catch(e => e);

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(401);
    });

    it('does not throw when the error response body is not valid JSON', async () => {
      const res = {
        ok: false,
        status: 500,
        json: () => Promise.reject(new Error('invalid json')),
      } as Response;
      vi.mocked(fetch).mockResolvedValue(res);

      await expect(request('/api/thing', {}, 'Fallback')).rejects.toThrow('Fallback');
    });
  });

  describe('requestData', () => {
    it('unwraps the ApiResponse data on success', async () => {
      const body: ApiResponse<{ id: number }> = { success: true, message: 'ok', data: { id: 1 } };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      const result = await requestData<{ id: number }>('/api/thing');

      expect(result).toEqual({ id: 1 });
    });

    it('throws when the envelope success flag is false even on a 2xx response', async () => {
      const body: ApiResponse<null> = { success: false, message: 'Business rule violated', data: null };
      vi.mocked(fetch).mockResolvedValue(jsonResponse(body, true));

      await expect(requestData('/api/thing')).rejects.toThrow('Business rule violated');
    });

    it('throws on a non-2xx response using the fallback message', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 404));

      await expect(requestData('/api/thing', {}, s => `Not found (${s})`)).rejects.toThrow('Not found (404)');
    });
  });
});
