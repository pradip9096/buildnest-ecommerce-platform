import { useAsync } from './useAsync';
import { fetchInventoryStatus } from '../api/inventory';
import type { InventoryStatusInfo } from '../types';

interface UseInventoryStatusResult {
  status: InventoryStatusInfo | null;
  loading: boolean;
  error: string | null;
}

export function useInventoryStatus(productId: number): UseInventoryStatusResult {
  const { data: status, loading, error } = useAsync<InventoryStatusInfo>(
    () => fetchInventoryStatus(productId),
    [productId]
  );
  return { status, loading, error };
}
