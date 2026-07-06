import { request, requestData } from './client';
import type { UserProfile } from '../types';

export async function fetchProfile(): Promise<UserProfile> {
  return requestData<UserProfile>('/api/user/profile', {}, 'Failed to load profile');
}

export async function updateProfile(
  data: { firstName: string; lastName: string; email: string; phone?: string; address?: string }
): Promise<UserProfile> {
  return requestData<UserProfile>(
    '/api/user/profile',
    { method: 'PUT', body: data },
    'Failed to update profile'
  );
}

export async function changePassword(
  oldPassword: string,
  newPassword: string
): Promise<void> {
  await request(
    '/api/password/change',
    { method: 'POST', body: { oldPassword, newPassword } },
    'Failed to change password'
  );
}
