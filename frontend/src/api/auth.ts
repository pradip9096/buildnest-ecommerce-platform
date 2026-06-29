import type { ApiResponse, AuthTokens } from '../types';

export async function apiLogin(username: string, password: string): Promise<AuthTokens> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  const body: ApiResponse<AuthTokens> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Login failed');
  return body.data;
}

export async function apiRegister(payload: {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}): Promise<void> {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const body: ApiResponse<null> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Registration failed');
}

export async function apiRefresh(refreshToken: string): Promise<AuthTokens> {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  const body: ApiResponse<AuthTokens> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Token refresh failed');
  return body.data;
}

export async function apiLogout(refreshToken: string | null): Promise<void> {
  await fetch('/api/auth/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
}
