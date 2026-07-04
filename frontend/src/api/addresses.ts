import { requestData } from './client';
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

export async function createAddress(input: CreateAddressInput, token: string): Promise<Address> {
  const data = await requestData<Address>(
    BASE,
    { method: 'POST', token, body: input },
    s => `Failed to create address (${s})`
  );
  if (!data) throw new Error(`Failed to create address`);
  return data;
}
