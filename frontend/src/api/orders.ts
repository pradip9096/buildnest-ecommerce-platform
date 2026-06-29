import type { ApiResponse, Order } from '../types';

const BASE = '/api/user/orders';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}` };
}

export async function fetchOrderById(id: number, token: string): Promise<Order> {
  const res = await fetch(`${BASE}/${id}`, { headers: authHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch order (${res.status})`);
  const body: ApiResponse<Order> = await res.json();
  return body.data;
}
