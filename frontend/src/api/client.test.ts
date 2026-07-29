import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { request, requestData, setUnauthorizedHandler, ApiError } from './client';
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
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
});

afterEach(() => {
  setUnauthorizedHandler(null);
});

describe('api/client', () => {
  describe('request', () => {
    it('sends a GET with no body and no Content-Type header by default, with credentials included', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      const result = await request<{ ok: boolean }>('/api/thing');

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        method: undefined,
        credentials: 'include',
        headers: {},
        body: undefined,
      });
      expect(result).toEqual({ ok: true });
    });

    it('attaches the X-XSRF-TOKEN header from the cookie on a mutating request', async () => {
      document.cookie = 'XSRF-TOKEN=abc123';
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { method: 'POST' });

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        method: 'POST',
        credentials: 'include',
        headers: { 'X-XSRF-TOKEN': 'abc123' },
        body: undefined,
      });
    });

    it('does not attach X-XSRF-TOKEN on a GET request', async () => {
      document.cookie = 'XSRF-TOKEN=abc123';
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { method: 'GET' });

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        method: 'GET',
        credentials: 'include',
        headers: {},
        body: undefined,
      });
    });

    it('serializes a body and sets Content-Type: application/json', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { method: 'POST', body: { a: 1 } });

      expect(fetch).toHaveBeenCalledWith('/api/thing', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ a: 1 }),
      });
    });

    it('sends a FormData body as-is, without JSON.stringify or a Content-Type header', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));
      const form = new FormData();
      form.append('file', new File(['data'], 'photo.jpg', { type: 'image/jpeg' }));

      await request('/api/thing', { method: 'POST', body: form });

      const [, init] = vi.mocked(fetch).mock.calls[0];
      expect(init?.body).toBe(form);
      expect((init?.headers as Record<string, string>)['Content-Type']).toBeUndefined();
    });

    it('still attaches X-XSRF-TOKEN on a mutating FormData request', async () => {
      document.cookie = 'XSRF-TOKEN=abc123';
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));
      const form = new FormData();

      await request('/api/thing', { method: 'POST', body: form });

      const [, init] = vi.mocked(fetch).mock.calls[0];
      expect((init?.headers as Record<string, string>)['X-XSRF-TOKEN']).toBe('abc123');
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

  describe('401 refresh-and-retry', () => {
    it('retries once when the unauthorized handler successfully refreshes', async () => {
      vi.mocked(fetch)
        .mockResolvedValueOnce(jsonResponse({}, false, 401))
        .mockResolvedValueOnce(jsonResponse({ ok: true }, true));
      const handler = vi.fn().mockResolvedValue(true);
      setUnauthorizedHandler(handler);

      const result = await request<{ ok: boolean }>('/api/thing');

      expect(handler).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledTimes(2);
      expect(result).toEqual({ ok: true });
    });

    it('throws the original 401 error when the unauthorized handler cannot refresh', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));
      const handler = vi.fn().mockResolvedValue(false);
      setUnauthorizedHandler(handler);

      await expect(request('/api/thing', {}, 'Session expired')).rejects.toThrow('Session expired');
      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('does not retry more than once even if the retried request also gets a 401', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));
      const handler = vi.fn().mockResolvedValue(true);
      setUnauthorizedHandler(handler);

      await expect(request('/api/thing')).rejects.toThrow(ApiError);
      expect(handler).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledTimes(2);
    });

    it('throws the original 401 error when no handler is registered', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));

      await expect(request('/api/thing')).rejects.toThrow(ApiError);
      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('does not call the unauthorized handler when skipAuthInterceptor is set, even on a 401', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));
      const handler = vi.fn().mockResolvedValue(true);
      setUnauthorizedHandler(handler);

      await expect(request('/api/thing', { skipAuthInterceptor: true })).rejects.toThrow(ApiError);

      expect(handler).not.toHaveBeenCalled();
      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('never recurses when apiRefresh-style calls (skipAuthInterceptor) themselves return 401', async () => {
      // Regression test for #516: apiRefresh() -> request(..., { skipAuthInterceptor: true })
      // getting a 401 (no valid refresh cookie) must not re-invoke the unauthorized handler,
      // which previously called apiRefresh() again, recursing indefinitely.
      vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 401));
      const handler = vi.fn(async () => {
        try {
          await request('/api/auth/refresh', { method: 'POST', skipAuthInterceptor: true });
          return true;
        } catch {
          return false;
        }
      });
      setUnauthorizedHandler(handler);

      await expect(request('/api/protected')).rejects.toThrow(ApiError);

      // The outer 401 triggers the handler exactly once; the handler's own internal
      // refresh call must not trigger the handler a second time.
      expect(handler).toHaveBeenCalledTimes(1);
    });

    it('does not include skipAuthInterceptor in the fetch() call options', async () => {
      vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }, true));

      await request('/api/thing', { method: 'POST', skipAuthInterceptor: true });

      const [, init] = vi.mocked(fetch).mock.calls[0];
      expect(init).not.toHaveProperty('skipAuthInterceptor');
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
