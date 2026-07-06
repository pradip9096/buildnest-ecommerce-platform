import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import { apiFetchCsrf, apiLogin, apiLogout, apiRefresh } from '../api/auth';
import { fetchProfile } from '../api/user';
import { request } from '../api/client';
import type { AuthUserResponse, UserProfile } from '../types';

vi.mock('../api/auth', () => ({
  apiFetchCsrf: vi.fn(),
  apiLogin: vi.fn(),
  apiLogout: vi.fn(),
  apiRefresh: vi.fn(),
  apiRegister: vi.fn(),
}));

vi.mock('../api/user', () => ({
  fetchProfile: vi.fn(),
}));

const mockApiFetchCsrf = vi.mocked(apiFetchCsrf);
const mockApiLogin = vi.mocked(apiLogin);
const mockApiLogout = vi.mocked(apiLogout);
const mockApiRefresh = vi.mocked(apiRefresh);
const mockFetchProfile = vi.mocked(fetchProfile);

const profile: UserProfile = {
  id: 1,
  username: 'alice',
  email: 'alice@example.com',
  firstName: 'Alice',
  lastName: 'Doe',
};

const authUserResponse: AuthUserResponse = { userId: 1, username: 'alice' };

function wrapper({ children }: { children: React.ReactNode }) {
  return <AuthProvider>{children}</AuthProvider>;
}

beforeEach(() => {
  vi.clearAllMocks();
  mockApiFetchCsrf.mockResolvedValue(undefined);
});

describe('AuthContext', () => {
  it('starts unauthenticated when the profile call fails (no valid session cookie)', async () => {
    mockFetchProfile.mockRejectedValue(new Error('Not authenticated'));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('restores a session by calling the profile endpoint when a valid session cookie exists', async () => {
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(mockFetchProfile).toHaveBeenCalledWith();
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user?.username).toBe('alice');
  });

  it('bootstraps the CSRF cookie on mount', async () => {
    mockFetchProfile.mockRejectedValue(new Error('Not authenticated'));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(mockApiFetchCsrf).toHaveBeenCalled();
  });

  it('logs a user in and loads the profile — no tokens ever touched by JS', async () => {
    mockApiLogin.mockResolvedValue(authUserResponse);
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.login('alice', 'password123');
    });

    expect(mockApiLogin).toHaveBeenCalledWith('alice', 'password123');
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user?.roles).toEqual(['USER']);
  });

  it('logs a user out — apiLogout takes no arguments, cookie carries the refresh token', async () => {
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });
    mockApiLogout.mockResolvedValue(undefined);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    await act(async () => {
      await result.current.logout();
    });

    expect(mockApiLogout).toHaveBeenCalledWith();
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('silently refreshes via the cookie and lets the retried request succeed when a request hits a 401', async () => {
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });
    mockApiRefresh.mockResolvedValue(authUserResponse);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: false, status: 401, json: () => Promise.resolve({}) } as Response)
      .mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({ ok: true }) } as Response));

    await act(async () => {
      await request('/api/protected-thing');
    });

    expect(mockApiRefresh).toHaveBeenCalledWith();
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('logs the user out when the refresh attempt itself fails after a 401', async () => {
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });
    mockApiRefresh.mockRejectedValue(new Error('Token refresh failed'));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValue({ ok: false, status: 401, json: () => Promise.resolve({}) } as Response));

    await act(async () => {
      await request('/api/protected-thing').catch(() => {});
    });

    expect(mockApiRefresh).toHaveBeenCalledWith();
    expect(result.current.isAuthenticated).toBe(false);
  });
});
