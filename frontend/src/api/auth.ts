import { request, requestData } from './client';
import type { AuthUserResponse } from '../types';

export async function apiLogin(username: string, password: string): Promise<AuthUserResponse> {
  return requestData<AuthUserResponse>('/api/auth/login', {
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

/** Refresh travels via the refresh_token cookie automatically — no body needed. */
export async function apiRefresh(): Promise<AuthUserResponse> {
  return requestData<AuthUserResponse>('/api/auth/refresh', { method: 'POST' }, 'Token refresh failed');
}

/** Logout travels via the refresh_token cookie automatically — no body needed. */
export async function apiLogout(): Promise<void> {
  await request('/api/auth/logout', { method: 'POST' }, 'Logout failed');
}

/** Bootstraps the XSRF-TOKEN cookie — call once at app startup before any mutating request. */
export async function apiFetchCsrf(): Promise<void> {
  await fetch('/api/auth/csrf', { credentials: 'include' });
}
