import { requestData } from './client';
import type { Order } from '../types';

const BASE = '/api/user/orders';

export async function fetchOrders(token: string): Promise<Order[]> {
  const data = await requestData<Order[]>(BASE, { token }, s => `Failed to fetch orders (${s})`);
  return data ?? [];
}

export async function fetchOrderById(id: number, token: string): Promise<Order> {
  return requestData<Order>(`${BASE}/${id}`, { token }, s => `Failed to fetch order (${s})`);
}
