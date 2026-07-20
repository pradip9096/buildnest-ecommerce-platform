import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchAdminShippingMethods,
  deactivateAdminShippingMethod,
  type AdminShippingMethod,
} from '../../api/admin';
import { ShippingMethodFormModal } from './ShippingMethodFormModal';

export function ShippingMethodsTab() {
  const { data, loading, error, setData } = useAsync<AdminShippingMethod[]>(
    () => fetchAdminShippingMethods(),
    []
  );
  const methods = data ?? [];
  const [search, setSearch] = useState('');
  const [deactivating, setDeactivating] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [modalMethod, setModalMethod] = useState<AdminShippingMethod | 'new' | null>(null);

  const filtered = search
    ? methods.filter(m => m.name.toLowerCase().includes(search.toLowerCase()))
    : methods;

  const handleDeactivate = async (method: AdminShippingMethod) => {
    if (!confirm(`Deactivate shipping method "${method.name}"? It can no longer be selected at checkout.`)) return;
    setDeactivating(method.id);
    setActionError(null);
    try {
      await deactivateAdminShippingMethod(method.id);
      setData(prev => (prev ?? []).map(m => (m.id === method.id ? { ...m, isActive: false } : m)));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to deactivate shipping method');
    } finally {
      setDeactivating(null);
    }
  };

  const handleSaved = (saved: AdminShippingMethod) => {
    setData(prev => {
      const existing = prev ?? [];
      const index = existing.findIndex(m => m.id === saved.id);
      if (index === -1) return [...existing, saved];
      const next = [...existing];
      next[index] = saved;
      return next;
    });
    setModalMethod(null);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Shipping Methods</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search shipping methods…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{methods.length} total</span>
          <button
            type="button"
            onClick={() => setModalMethod('new')}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Method
          </button>
        </div>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}
      {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Base Cost</th>
              <th className="px-4 py-3">Cost / kg</th>
              <th className="px-4 py-3">Est. Days</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(6)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No shipping methods match your search' : 'No shipping methods found'}
                </td>
              </tr>
            ) : filtered.map(method => (
              <tr key={method.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <div className="font-medium text-gray-900">{method.name}</div>
                  {method.description && (
                    <div className="text-gray-400 text-xs max-w-xs truncate">{method.description}</div>
                  )}
                </td>
                <td className="px-4 py-3 text-gray-500">₹{method.baseCost}</td>
                <td className="px-4 py-3 text-gray-500">₹{method.costPerKg}</td>
                <td className="px-4 py-3 text-gray-500">
                  {method.estimatedDaysMin}–{method.estimatedDaysMax} days
                </td>
                <td className="px-4 py-3">
                  <span
                    className={`text-xs font-medium px-2 py-1 rounded-full ${
                      method.isActive
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {method.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setModalMethod(method)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-800 border border-primary-200 hover:border-primary-400 rounded-lg px-3 py-1 transition-colors"
                    >
                      Edit
                    </button>
                    {method.isActive && (
                      <button
                        type="button"
                        onClick={() => handleDeactivate(method)}
                        disabled={deactivating === method.id}
                        className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                      >
                        {deactivating === method.id ? 'Deactivating…' : 'Deactivate'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modalMethod && (
        <ShippingMethodFormModal
          method={modalMethod === 'new' ? null : modalMethod}
          onClose={() => setModalMethod(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
