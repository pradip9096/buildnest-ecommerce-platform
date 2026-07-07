import { describe, it, expect, vi, beforeEach } from 'vitest';
import { apiForgotPassword, apiResetPassword } from './auth';

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

describe('api/auth — apiForgotPassword', () => {
  it('sends the email as a URL query param, not a JSON body (backend uses @RequestParam)', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await apiForgotPassword('aarav@example.com');

    const [url, options] = vi.mocked(fetch).mock.calls[0];
    expect(String(url)).toBe('/api/password/forgot?email=aarav%40example.com');
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expect((options as RequestInit).body).toBeUndefined();
  });

  it('resolves on a successful response, regardless of whether the email exists', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await expect(apiForgotPassword('nobody@example.com')).resolves.toBeUndefined();
  });

  it('surfaces the backend error message on a rate-limited response', async () => {
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse({ success: false, message: 'Too many password reset requests. Please try again later.' }, false, 429)
    );

    await expect(apiForgotPassword('aarav@example.com')).rejects.toThrow(
      'Too many password reset requests. Please try again later.'
    );
  });

  it('uses a generic fallback message when the backend gives none', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

    await expect(apiForgotPassword('aarav@example.com')).rejects.toThrow(
      'Failed to request a password reset'
    );
  });
});

describe('api/auth — apiResetPassword', () => {
  it('sends token and newPassword as URL query params, not a JSON body', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await apiResetPassword('abc-123-token', 'newPass456!');

    const [url, options] = vi.mocked(fetch).mock.calls[0];
    expect(String(url)).toBe('/api/password/reset?token=abc-123-token&newPassword=newPass456%21');
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expect((options as RequestInit).body).toBeUndefined();
  });

  it('resolves on a successful response', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await expect(apiResetPassword('abc-123-token', 'newPass456!')).resolves.toBeUndefined();
  });

  it('surfaces the backend error message for an invalid or expired token', async () => {
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse({ success: false, message: 'Invalid or expired token' }, false, 400)
    );

    await expect(apiResetPassword('expired-token', 'newPass456!')).rejects.toThrow(
      'Invalid or expired token'
    );
  });

  it('uses a generic fallback message when the backend gives none', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

    await expect(apiResetPassword('abc-123-token', 'newPass456!')).rejects.toThrow(
      'Failed to reset password'
    );
  });
});
