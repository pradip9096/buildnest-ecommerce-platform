import { requestData } from './client';
import type { CheckoutSession, ShippingOption, Order } from '../types';

const BASE = '/api/v1/checkout';

export async function fetchShippingOptions(
  postalCode?: string
): Promise<ShippingOption[]> {
  const url = postalCode
    ? `${BASE}/shipping-options?postalCode=${encodeURIComponent(postalCode)}`
    : `${BASE}/shipping-options`;
  const data = await requestData<ShippingOption[]>(url, {}, s => `Failed to fetch shipping options (${s})`);
  return data ?? [];
}

export async function setCheckoutAddress(
  addressId: number
): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/address`,
    { method: 'POST', body: { addressId } },
    s => `Failed to set address (${s})`
  );
}

export async function applyCheckoutCoupon(
  code: string
): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/coupon`,
    { method: 'POST', body: { code } },
    s => `Failed to apply coupon (${s})`
  );
}

export async function selectCheckoutShipping(
  shippingMethodId: number
): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/shipping`,
    { method: 'POST', body: { shippingMethodId } },
    s => `Failed to select shipping (${s})`
  );
}

export async function initiateCheckoutPayment(): Promise<CheckoutSession> {
  return requestData<CheckoutSession>(
    `${BASE}/payment`,
    { method: 'POST' },
    s => `Failed to initiate payment (${s})`
  );
}

export async function confirmCheckout(): Promise<Order> {
  return requestData<Order>(`${BASE}/confirm`, { method: 'POST' }, s => `Failed to confirm order (${s})`);
}
