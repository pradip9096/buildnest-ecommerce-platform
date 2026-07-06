import { requestData } from './client';
import type { Order } from '../types';

const BASE = '/api/user/orders';

export async function fetchOrders(): Promise<Order[]> {
  const data = await requestData<Order[]>(BASE, {}, s => `Failed to fetch orders (${s})`);
  return data ?? [];
}

export async function fetchOrderById(id: number): Promise<Order> {
  return requestData<Order>(`${BASE}/${id}`, {}, s => `Failed to fetch order (${s})`);
}
