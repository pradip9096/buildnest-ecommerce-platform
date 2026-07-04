import type { Address, ApiResponse } from '../types';

const BASE = '/api/user/addresses';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

export interface CreateAddressInput {
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  addressType?: string;
}

export async function createAddress(input: CreateAddressInput, token: string): Promise<Address> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(input),
  });
  const body: ApiResponse<Address> = await res.json();
  if (!res.ok || !body.data) throw new Error(body.message ?? `Failed to create address (${res.status})`);
  return body.data;
}
