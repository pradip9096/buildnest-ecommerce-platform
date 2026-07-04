import { requestData } from './client';
import type { CheckoutSession, ShippingOption, Order } from '../types';

const BASE = '/api/v1/checkout';

export async function fetchShippingOptions(
  token: string,
  postalCode?: string
): Promise<ShippingOption[]> {
  const url = postalCode
    ? `${BASE}/shipping-options?postalCode=${encodeURIComponent(postalCode)}`
    : `${BASE}/shipping-options`;
  const data = await requestData<ShippingOption[]>(url, { token }, s => `Failed to fetch shipping options (${s})`);
  return data ?? [];
}

export async function setCheckoutAddress(
  addressId: number,
  token: string
): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/address`,
    { method: 'POST', token, body: { addressId } },
    s => `Failed to set address (${s})`
  );
}

export async function selectCheckoutShipping(
  shippingMethodId: number,
  token: string
): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/shipping`,
    { method: 'POST', token, body: { shippingMethodId } },
    s => `Failed to select shipping (${s})`
  );
}

export async function initiateCheckoutPayment(token: string): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/payment`,
    { method: 'POST', token },
    s => `Failed to initiate payment (${s})`
  );
}

export async function confirmCheckout(token: string): Promise<Order> {
  return requestData<Order>(`${BASE}/confirm`, { method: 'POST', token }, s => `Failed to confirm order (${s})`);
}
