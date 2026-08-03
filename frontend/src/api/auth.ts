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
  consentGiven: boolean;
}): Promise<void> {
  await requestData<null>('/api/auth/register', { method: 'POST', body: payload }, 'Registration failed');
}

/**
 * Refresh travels via the refresh_token cookie automatically — no body needed.
 * `skipAuthInterceptor` is required: this call IS the 401 interceptor's own refresh attempt,
 * so a 401 here (no valid refresh cookie) must not re-trigger the interceptor recursively.
 */
export async function apiRefresh(): Promise<AuthUserResponse> {
  return requestData<AuthUserResponse>(
    '/api/auth/refresh',
    { method: 'POST', skipAuthInterceptor: true },
    'Token refresh failed'
  );
}

/** Logout travels via the refresh_token cookie automatically — no body needed. */
export async function apiLogout(): Promise<void> {
  await request('/api/auth/logout', { method: 'POST', skipAuthInterceptor: true }, 'Logout failed');
}

/** Bootstraps the XSRF-TOKEN cookie — call once at app startup before any mutating request. */
export async function apiFetchCsrf(): Promise<void> {
  await fetch('/api/auth/csrf', { credentials: 'include' });
}

/**
 * PasswordResetController's /forgot and /reset endpoints read @RequestParam values, not a
 * JSON body — unlike every other endpoint in this API, so the params travel in the query
 * string here instead of `client.ts`'s usual JSON-encoded body.
 */
export async function apiForgotPassword(email: string): Promise<void> {
  const params = new URLSearchParams({ email });
  await requestData<null>(
    `/api/password/forgot?${params.toString()}`,
    { method: 'POST' },
    'Failed to request a password reset'
  );
}

export async function apiResetPassword(token: string, newPassword: string): Promise<void> {
  const params = new URLSearchParams({ token, newPassword });
  await requestData<null>(
    `/api/password/reset?${params.toString()}`,
    { method: 'POST' },
    'Failed to reset password'
  );
}
