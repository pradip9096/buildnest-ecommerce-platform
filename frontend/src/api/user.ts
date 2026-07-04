import { request, requestData } from './client';
import type { UserProfile } from '../types';

export async function fetchProfile(token: string): Promise<UserProfile> {
  return requestData<UserProfile>('/api/user/profile', { token }, 'Failed to load profile');
}

export async function updateProfile(
  token: string,
  data: { firstName: string; lastName: string; email: string; phone?: string; address?: string }
): Promise<UserProfile> {
  return requestData<UserProfile>(
    '/api/user/profile',
    { method: 'PUT', token, body: data },
    'Failed to update profile'
  );
}

export async function changePassword(
  token: string,
  oldPassword: string,
  newPassword: string
): Promise<void> {
  await request(
    '/api/password/change',
    { method: 'POST', token, body: { oldPassword, newPassword } },
    'Failed to change password'
  );
}
