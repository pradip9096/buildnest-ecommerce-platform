import { requestData } from './client';
import type { Order } from '../types';

const BASE = '/api/user/seller/orders';

export interface SellerOrdersPage {
  content: Order[];
  totalElements: number;
  totalPages: number;
}

export async function fetchSellerOrders(
  params: { page?: number; size?: number } = {}
): Promise<SellerOrdersPage> {
  const q = new URLSearchParams();
  q.set('page', String(params.page ?? 0));
  q.set('size', String(params.size ?? 20));
  const data = await requestData<SellerOrdersPage>(
    `${BASE}?${q}`,
    {},
    s => `Failed to load orders (${s})`
  );
  return data ?? { content: [], totalElements: 0, totalPages: 0 };
}

export async function fetchSellerOrderById(id: number): Promise<Order> {
  return requestData<Order>(`${BASE}/${id}`, {}, s => `Failed to fetch order (${s})`);
}

export async function updateSellerOrderStatus(id: number, status: string): Promise<Order> {
  return requestData<Order>(
    `${BASE}/${id}/status`,
    { method: 'PATCH', body: { status } },
    s => `Failed to update order status (${s})`
  );
}
