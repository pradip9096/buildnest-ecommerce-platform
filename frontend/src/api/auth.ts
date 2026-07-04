import { request, requestData } from './client';
import type { AuthTokens } from '../types';

export async function apiLogin(username: string, password: string): Promise<AuthTokens> {
  return requestData<AuthTokens>('/api/auth/login', {
    method: 'POST',
    body: { username, password },
  }, 'Login failed');
}

export async function apiRegister(payload: {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}): Promise<void> {
  await requestData<null>('/api/auth/register', { method: 'POST', body: payload }, 'Registration failed');
}

export async function apiRefresh(refreshToken: string): Promise<AuthTokens> {
  return requestData<AuthTokens>('/api/auth/refresh', {
    method: 'POST',
    body: { refreshToken },
  }, 'Token refresh failed');
}

export async function apiLogout(refreshToken: string | null): Promise<void> {
  await request('/api/auth/logout', { method: 'POST', body: { refreshToken } }, 'Logout failed');
}
