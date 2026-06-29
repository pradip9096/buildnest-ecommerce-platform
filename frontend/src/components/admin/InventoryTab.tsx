import { useState, useEffect, useCallback } from 'react';
import { fetchAdminInventory, type InventoryItem } from '../../api/admin';
import { InventoryAdjustModal } from './InventoryAdjustModal';

interface Props { token: string; }

const STATUS_COLORS: Record<string, string> = {
  IN_STOCK:     'bg-green-100 text-green-800',
  LOW_STOCK:    'bg-yellow-100 text-yellow-800',
  OUT_OF_STOCK: 'bg-red-100 text-red-800',
};

export function InventoryTab({ token }: Props) {
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [adjusting, setAdjusting] = useState<InventoryItem | null>(null);
  const [search, setSearch] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchAdminInventory(token)
      .then(setItems)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load inventory'))
      .finally(() => setLoading(false));
  }, [token]);

  useEffect(() => { load(); }, [load]);

  const filtered = items.filter(i =>
    i.productName.toLowerCase().includes(search.toLowerCase()) ||
    String(i.productId).includes(search)
  );

  return (
    <div className="space-y-4">
      {adjusting && (
        <InventoryAdjustModal
          item={adjusting}
          token={token}
          onClose={() => setAdjusting(null)}
          onSuccess={() => { setAdjusting(null); load(); }}
        />
      )}

      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Inventory</h2>
        <input
          type="search"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search products…"
          className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-amber-400"
        />
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Product</th>
              <th className="px-4 py-3 text-right">Total</th>
              <th className="px-4 py-3 text-right">Reserved</th>
              <th className="px-4 py-3 text-right">Available</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(6)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(6)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No products match your search' : 'No inventory data found'}
                </td>
              </tr>
            ) : filtered.map(item => (
              <tr key={item.productId} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <p className="font-medium text-gray-900">{item.productName}</p>
                  <p className="text-xs text-gray-400">ID #{item.productId}</p>
                </td>
                <td className="px-4 py-3 text-right text-gray-700">{item.quantity}</td>
                <td className="px-4 py-3 text-right text-gray-500">{item.reservedQuantity}</td>
                <td className="px-4 py-3 text-right font-semibold text-gray-900">{item.availableQuantity}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${STATUS_COLORS[item.status] ?? 'bg-gray-100 text-gray-600'}`}>
                    {item.status.replace(/_/g, ' ')}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => setAdjusting(item)}
                    className="text-xs font-medium text-amber-600 hover:text-amber-800 border border-amber-200 hover:border-amber-400 rounded-lg px-3 py-1 transition-colors"
                  >
                    Adjust
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
