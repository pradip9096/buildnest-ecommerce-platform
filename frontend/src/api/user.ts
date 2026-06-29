import type { ApiResponse, UserProfile } from '../types';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

export async function fetchProfile(token: string): Promise<UserProfile> {
  const res = await fetch('/api/user/profile', { headers: authHeaders(token) });
  const body: ApiResponse<UserProfile> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load profile');
  return body.data;
}

export async function updateProfile(
  token: string,
  data: { firstName: string; lastName: string; email: string; phone?: string; address?: string }
): Promise<UserProfile> {
  const res = await fetch('/api/user/profile', {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(data),
  });
  const body: ApiResponse<UserProfile> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to update profile');
  return body.data;
}

export async function changePassword(
  token: string,
  userId: number,
  oldPassword: string,
  newPassword: string
): Promise<void> {
  const params = new URLSearchParams({ userId: String(userId), oldPassword, newPassword });
  const res = await fetch(`/api/password/change?${params}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  });
  const body: ApiResponse<null> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to change password');
}
