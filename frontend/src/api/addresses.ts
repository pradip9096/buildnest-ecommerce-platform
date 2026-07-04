import { request, requestData } from './client';
import type { Address } from '../types';

const BASE = '/api/user/addresses';

export interface CreateAddressInput {
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  addressType?: string;
}

export type UpdateAddressInput = CreateAddressInput;

export async function fetchAddresses(token: string): Promise<Address[]> {
  const data = await requestData<Address[]>(BASE, { token }, s => `Failed to fetch addresses (${s})`);
  return data ?? [];
}

export async function createAddress(input: CreateAddressInput, token: string): Promise<Address> {
  const data = await requestData<Address>(
    BASE,
    { method: 'POST', token, body: input },
    s => `Failed to create address (${s})`
  );
  if (!data) throw new Error(`Failed to create address`);
  return data;
}

export async function updateAddress(
  id: number,
  input: UpdateAddressInput,
  token: string
): Promise<Address> {
  const data = await requestData<Address>(
    `${BASE}/${id}`,
    { method: 'PUT', token, body: input },
    s => `Failed to update address (${s})`
  );
  if (!data) throw new Error(`Failed to update address`);
  return data;
}

export async function deleteAddress(id: number, token: string): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE', token }, s => `Failed to delete address (${s})`);
}

export async function setDefaultAddress(id: number, token: string): Promise<Address> {
  const data = await requestData<Address>(
    `${BASE}/${id}/default`,
    { method: 'PUT', token },
    s => `Failed to set default address (${s})`
  );
  if (!data) throw new Error(`Failed to set default address`);
  return data;
}
