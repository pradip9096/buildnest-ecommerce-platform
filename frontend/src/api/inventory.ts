import { requestData } from './client';
import type { InventoryStatusInfo } from '../types';

export async function fetchInventoryStatus(productId: number): Promise<InventoryStatusInfo> {
  const data = await requestData<InventoryStatusInfo>(
    `/api/inventory/${productId}/status`,
    {},
    s => `Failed to fetch inventory status: ${s}`
  );
  if (!data) throw new Error('Inventory status not found');
  return data;
}
