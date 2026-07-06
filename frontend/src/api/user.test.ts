import { describe, it, expect, vi, beforeEach } from 'vitest';
import { changePassword } from './user';

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

describe('api/user — changePassword', () => {
  it('sends old and new passwords in the JSON request body, not as URL query params', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await changePassword('oldPass123!', 'newPass456!');

    expect(fetch).toHaveBeenCalledWith('/api/password/change', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ oldPassword: 'oldPass123!', newPassword: 'newPass456!' }),
    });
  });

  it('does not put credentials in the request URL', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await changePassword('oldPass123!', 'newPass456!');

    const [url] = vi.mocked(fetch).mock.calls[0];
    expect(String(url)).not.toContain('oldPass123!');
    expect(String(url)).not.toContain('newPass456!');
    expect(String(url)).toBe('/api/password/change');
  });

  it('resolves on a successful response', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ success: true }, true));

    await expect(changePassword('oldPass123!', 'newPass456!')).resolves.toBeUndefined();
  });

  it('surfaces the backend error message when the old password is incorrect', async () => {
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse({ success: false, message: 'Old password is incorrect' }, false, 400)
    );

    await expect(changePassword('wrongPass', 'newPass456!')).rejects.toThrow(
      'Old password is incorrect'
    );
  });

  it('uses a generic fallback message when the backend gives none', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({}, false, 500));

    await expect(changePassword('oldPass123!', 'newPass456!')).rejects.toThrow(
      'Failed to change password'
    );
  });
});
