import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchInventoryDetail,
  addStock,
  setStock,
  checkStockAvailability,
  type InventoryItem,
  type InventoryDetail,
} from '../../api/admin';

interface Props {
  item: InventoryItem;
  onClose: () => void;
  onChanged: () => void;
}

export function InventoryDetailModal({ item, onClose, onChanged }: Props) {
  const { data, loading, error, setData } = useAsync<InventoryDetail>(
    () => fetchInventoryDetail(item.productId),
    [item.productId]
  );

  const [addQty, setAddQty] = useState('');
  const [setQty, setSetQty] = useState('');
  const [checkQty, setCheckQty] = useState('');
  const [availability, setAvailability] = useState<boolean | null>(null);
  const [busy, setBusy] = useState<'add' | 'set' | 'check' | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const parsePositiveInt = (value: string): number | null => {
    const n = parseInt(value, 10);
    return Number.isNaN(n) || n < 0 ? null : n;
  };

  const handleAddStock = async () => {
    const n = parsePositiveInt(addQty);
    if (n === null) { setActionError('Enter a non-negative integer quantity.'); return; }
    setBusy('add');
    setActionError(null);
    try {
      const updated = await addStock(item.productId, n);
      setData(updated);
      setAddQty('');
      onChanged();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to add stock');
    } finally {
      setBusy(null);
    }
  };

  const handleSetStock = async () => {
    const n = parsePositiveInt(setQty);
    if (n === null) { setActionError('Enter a non-negative integer quantity.'); return; }
    setBusy('set');
    setActionError(null);
    try {
      const updated = await setStock(item.productId, n);
      setData(updated);
      setSetQty('');
      onChanged();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to update stock');
    } finally {
      setBusy(null);
    }
  };

  const handleCheckAvailability = async () => {
    const n = parsePositiveInt(checkQty);
    if (n === null) { setActionError('Enter a non-negative integer quantity.'); return; }
    setBusy('check');
    setActionError(null);
    setAvailability(null);
    try {
      const available = await checkStockAvailability(item.productId, n);
      setAvailability(available);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to check availability');
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">Inventory Details</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <div className="px-6 py-4 space-y-4">
          <div>
            <p className="text-sm text-gray-500 mb-1">Product</p>
            <p className="font-medium text-gray-900">{item.productName}</p>
          </div>

          {loading && <p className="text-sm text-gray-400">Loading…</p>}
          {error && <p className="text-red-600 text-sm">{error}</p>}
          {data && (
            <div className="grid grid-cols-3 gap-3 text-sm">
              <div>
                <p className="text-gray-500">Total</p>
                <p className="font-semibold text-gray-900">{data.quantity}</p>
              </div>
              <div>
                <p className="text-gray-500">Reserved</p>
                <p className="font-semibold text-gray-900">{data.reservedQuantity}</p>
              </div>
              <div>
                <p className="text-gray-500">Available</p>
                <p className="font-semibold text-gray-900">{data.availableQuantity}</p>
              </div>
            </div>
          )}

          {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

          <div className="border-t border-gray-100 pt-4 space-y-3">
            <div className="flex gap-2">
              <input
                type="number"
                min="0"
                value={addQty}
                onChange={e => setAddQty(e.target.value)}
                placeholder="Quantity to add"
                aria-label="Quantity to add"
                className="flex-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
              <button
                type="button"
                onClick={handleAddStock}
                disabled={busy !== null}
                className="bg-primary-500 hover:bg-primary-600 text-white rounded-xl px-4 py-2 text-sm font-semibold disabled:opacity-60 transition-colors"
              >
                {busy === 'add' ? 'Adding…' : 'Add Stock'}
              </button>
            </div>

            <div className="flex gap-2">
              <input
                type="number"
                min="0"
                value={setQty}
                onChange={e => setSetQty(e.target.value)}
                placeholder="Set exact quantity"
                aria-label="Set exact quantity"
                className="flex-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
              <button
                type="button"
                onClick={handleSetStock}
                disabled={busy !== null}
                className="border border-gray-200 hover:bg-gray-50 text-gray-700 rounded-xl px-4 py-2 text-sm font-medium disabled:opacity-60 transition-colors"
              >
                {busy === 'set' ? 'Setting…' : 'Set Stock'}
              </button>
            </div>

            <div className="flex gap-2 items-center">
              <input
                type="number"
                min="0"
                value={checkQty}
                onChange={e => setCheckQty(e.target.value)}
                placeholder="Quantity to check"
                aria-label="Quantity to check"
                className="flex-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
              <button
                type="button"
                onClick={handleCheckAvailability}
                disabled={busy !== null}
                className="border border-gray-200 hover:bg-gray-50 text-gray-700 rounded-xl px-4 py-2 text-sm font-medium disabled:opacity-60 transition-colors"
              >
                {busy === 'check' ? 'Checking…' : 'Check'}
              </button>
            </div>
            {availability !== null && (
              <p className={`text-sm font-medium ${availability ? 'text-green-700' : 'text-red-600'}`}>
                {availability ? 'Available' : 'Not available'}
              </p>
            )}
          </div>
        </div>

        <div className="px-6 pb-6">
          <button
            type="button"
            onClick={onClose}
            className="w-full border border-gray-200 text-gray-700 rounded-xl py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
