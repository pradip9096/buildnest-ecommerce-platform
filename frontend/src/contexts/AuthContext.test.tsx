import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import { apiLogin, apiLogout } from '../api/auth';
import { fetchProfile } from '../api/user';
import { makeJwt } from '../test/jwt';
import type { AuthTokens, UserProfile } from '../types';

vi.mock('../api/auth', () => ({
  apiLogin: vi.fn(),
  apiLogout: vi.fn(),
  apiRegister: vi.fn(),
}));

vi.mock('../api/user', () => ({
  fetchProfile: vi.fn(),
}));

const mockApiLogin = vi.mocked(apiLogin);
const mockApiLogout = vi.mocked(apiLogout);
const mockFetchProfile = vi.mocked(fetchProfile);

const profile: UserProfile = {
  id: 1,
  username: 'alice',
  email: 'alice@example.com',
  firstName: 'Alice',
  lastName: 'Doe',
};

function wrapper({ children }: { children: React.ReactNode }) {
  return <AuthProvider>{children}</AuthProvider>;
}

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

describe('AuthContext', () => {
  it('starts unauthenticated when no token is stored', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
    expect(mockFetchProfile).not.toHaveBeenCalled();
  });

  it('restores a session from a valid stored access token', async () => {
    const token = makeJwt({ sub: 'alice', exp: Math.floor(Date.now() / 1000) + 3600 });
    localStorage.setItem('access_token', token);
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(mockFetchProfile).toHaveBeenCalledWith(token);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user?.username).toBe('alice');
    expect(result.current.token).toBe(token);
  });

  it('clears an expired stored access token without calling the profile API', async () => {
    const expiredToken = makeJwt({ sub: 'alice', exp: Math.floor(Date.now() / 1000) - 3600 });
    localStorage.setItem('access_token', expiredToken);
    localStorage.setItem('refresh_token', 'some-refresh-token');

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(mockFetchProfile).not.toHaveBeenCalled();
    expect(result.current.isAuthenticated).toBe(false);
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
  });

  it('logs a user in, stores tokens, and loads the profile', async () => {
    const tokens: AuthTokens = {
      accessToken: makeJwt({ sub: 'alice', exp: Math.floor(Date.now() / 1000) + 3600 }),
      refreshToken: 'refresh-abc',
      tokenType: 'Bearer',
      userId: 1,
      username: 'alice',
    };
    mockApiLogin.mockResolvedValue(tokens);
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.login('alice', 'password123');
    });

    expect(mockApiLogin).toHaveBeenCalledWith('alice', 'password123');
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user?.roles).toEqual(['USER']);
    expect(localStorage.getItem('access_token')).toBe(tokens.accessToken);
    expect(localStorage.getItem('refresh_token')).toBe('refresh-abc');
  });

  it('logs a user out and clears stored tokens', async () => {
    const token = makeJwt({ sub: 'alice', exp: Math.floor(Date.now() / 1000) + 3600 });
    localStorage.setItem('access_token', token);
    localStorage.setItem('refresh_token', 'refresh-abc');
    mockFetchProfile.mockResolvedValue({ ...profile, roles: ['USER'] });
    mockApiLogout.mockResolvedValue(undefined);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    await act(async () => {
      await result.current.logout();
    });

    expect(mockApiLogout).toHaveBeenCalledWith('refresh-abc');
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
  });
});
