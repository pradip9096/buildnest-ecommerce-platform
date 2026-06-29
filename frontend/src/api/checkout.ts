import type { ApiResponse, CheckoutSession, ShippingOption, Order } from '../types';

const BASE = '/api/v1/checkout';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

export async function fetchShippingOptions(
  token: string,
  postalCode?: string
): Promise<ShippingOption[]> {
  const url = postalCode
    ? `${BASE}/shipping-options?postalCode=${encodeURIComponent(postalCode)}`
    : `${BASE}/shipping-options`;
  const res = await fetch(url, { headers: authHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch shipping options (${res.status})`);
  const body: ApiResponse<ShippingOption[]> = await res.json();
  return body.data ?? [];
}

export async function setCheckoutAddress(
  addressId: number,
  token: string
): Promise<CheckoutSession> {
  const res = await fetch(`${BASE}/address`, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify({ addressId }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error((err as ApiResponse<null>).message ?? `Failed to set address (${res.status})`);
  }
  const body: ApiResponse<CheckoutSession> = await res.json();
  return body.data;
}

export async function selectCheckoutShipping(
  shippingMethodId: number,
  token: string
): Promise<CheckoutSession> {
  const res = await fetch(`${BASE}/shipping`, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify({ shippingMethodId }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error((err as ApiResponse<null>).message ?? `Failed to select shipping (${res.status})`);
  }
  const body: ApiResponse<CheckoutSession> = await res.json();
  return body.data;
}

export async function initiateCheckoutPayment(token: string): Promise<CheckoutSession> {
  const res = await fetch(`${BASE}/payment`, {
    method: 'POST',
    headers: authHeaders(token),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error((err as ApiResponse<null>).message ?? `Failed to initiate payment (${res.status})`);
  }
  const body: ApiResponse<CheckoutSession> = await res.json();
  return body.data;
}

export async function confirmCheckout(token: string): Promise<Order> {
  const res = await fetch(`${BASE}/confirm`, {
    method: 'POST',
    headers: authHeaders(token),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error((err as ApiResponse<null>).message ?? `Failed to confirm order (${res.status})`);
  }
  const body: ApiResponse<Order> = await res.json();
  return body.data;
}
