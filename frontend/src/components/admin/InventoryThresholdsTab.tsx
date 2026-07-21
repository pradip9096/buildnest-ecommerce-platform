import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchInventoryBelowThreshold,
  fetchInventoryThresholdBreaches,
  fetchInventoryFrequentProblems,
  fetchInventoryReportSummary,
  setProductThreshold,
  type InventoryBelowThreshold,
  type InventoryThresholdBreach,
  type InventoryFrequentProblem,
  type InventorySummary,
} from '../../api/admin';

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

const BREACH_TYPE_COLORS: Record<string, string> = {
  LOW_STOCK: 'bg-orange-100 text-orange-800',
  OUT_OF_STOCK: 'bg-red-100 text-red-800',
};

export function InventoryThresholdsTab() {
  const [startDate, setStartDate] = useState(() => toIsoDate(new Date(Date.now() - 30 * 86400000)));
  const [endDate, setEndDate] = useState(() => toIsoDate(new Date()));

  const { data: summary, loading: summaryLoading, error: summaryError } = useAsync<InventorySummary>(
    () => fetchInventoryReportSummary(),
    []
  );
  const {
    data: belowThreshold,
    loading: belowThresholdLoading,
    error: belowThresholdError,
    reload: reloadBelowThreshold,
  } = useAsync<InventoryBelowThreshold[]>(() => fetchInventoryBelowThreshold(), []);
  const { data: breaches, loading: breachesLoading, error: breachesError } = useAsync<InventoryThresholdBreach[]>(
    () => fetchInventoryThresholdBreaches({ startDate, endDate }),
    [startDate, endDate]
  );
  const { data: frequentProblems, loading: frequentLoading, error: frequentError } = useAsync<InventoryFrequentProblem[]>(
    () => fetchInventoryFrequentProblems({ startDate, endDate }),
    [startDate, endDate]
  );

  const [thresholdEdits, setThresholdEdits] = useState<Record<number, string>>({});
  const [savingProductId, setSavingProductId] = useState<number | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  const handleSaveThreshold = async (productId: number) => {
    const raw = thresholdEdits[productId];
    const minimumLevel = Number(raw);
    if (!raw || Number.isNaN(minimumLevel) || minimumLevel < 0) {
      setSaveError('Enter a non-negative minimum level');
      return;
    }
    setSavingProductId(productId);
    setSaveError(null);
    try {
      await setProductThreshold(productId, minimumLevel);
      setThresholdEdits(prev => {
        const next = { ...prev };
        delete next[productId];
        return next;
      });
      reloadBelowThreshold();
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : 'Failed to save threshold');
    } finally {
      setSavingProductId(null);
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Inventory Thresholds & Breach Reporting</h2>
        <div className="flex items-end gap-3">
          <label className="text-xs text-gray-500">
            Start
            <input
              type="date"
              value={startDate}
              max={endDate}
              onChange={e => setStartDate(e.target.value)}
              className="block mt-1 border border-gray-200 rounded-lg px-2 py-1 text-sm"
            />
          </label>
          <label className="text-xs text-gray-500">
            End
            <input
              type="date"
              value={endDate}
              min={startDate}
              max={toIsoDate(new Date())}
              onChange={e => setEndDate(e.target.value)}
              className="block mt-1 border border-gray-200 rounded-lg px-2 py-1 text-sm"
            />
          </label>
        </div>
      </div>

      <div>
        {summaryLoading ? (
          <div className="animate-pulse h-20 bg-gray-100 rounded-2xl" />
        ) : summaryError ? (
          <p className="text-red-600 text-sm">{summaryError}</p>
        ) : summary ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="border border-gray-200 rounded-xl p-4">
              <p className="text-2xl font-bold text-gray-900">{summary.totalProducts}</p>
              <p className="text-sm text-gray-500 mt-0.5">Total Products</p>
            </div>
            <div className="border border-gray-200 rounded-xl p-4">
              <p className="text-2xl font-bold text-orange-600">{summary.lowStock}</p>
              <p className="text-sm text-gray-500 mt-0.5">Low Stock</p>
            </div>
            <div className="border border-gray-200 rounded-xl p-4">
              <p className="text-2xl font-bold text-red-600">{summary.outOfStock}</p>
              <p className="text-sm text-gray-500 mt-0.5">Out of Stock</p>
            </div>
            <div className="border border-gray-200 rounded-xl p-4">
              <p className="text-2xl font-bold text-gray-900">{summary.totalAvailable}</p>
              <p className="text-sm text-gray-500 mt-0.5">Total Available</p>
            </div>
          </div>
        ) : null}
      </div>

      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Below Threshold — Configure Minimum Levels</h3>
        {saveError && <p className="text-red-600 text-sm mb-2">{saveError}</p>}
        {belowThresholdLoading ? (
          <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
        ) : belowThresholdError ? (
          <p className="text-red-600 text-sm">{belowThresholdError}</p>
        ) : !belowThreshold || belowThreshold.length === 0 ? (
          <p className="text-sm text-gray-400">No products currently below their threshold.</p>
        ) : (
          <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
            {belowThreshold.map(p => (
              <li key={p.productId} className="flex items-center justify-between px-4 py-3 text-sm gap-3">
                <div>
                  <span className="text-gray-700">{p.productName}</span>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {p.currentQuantity} in stock · min {p.minimumThreshold} · shortfall {p.shortfall}
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <input
                    type="number"
                    min={0}
                    placeholder={String(p.minimumThreshold)}
                    value={thresholdEdits[p.productId] ?? ''}
                    onChange={e =>
                      setThresholdEdits(prev => ({ ...prev, [p.productId]: e.target.value }))
                    }
                    className="w-20 border border-gray-200 rounded-lg px-2 py-1 text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => handleSaveThreshold(p.productId)}
                    disabled={savingProductId === p.productId}
                    className="px-3 py-1 rounded-lg text-xs font-medium bg-primary-500 text-white disabled:opacity-50"
                  >
                    {savingProductId === p.productId ? 'Saving…' : 'Set'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Threshold Breaches</h3>
          {breachesLoading ? (
            <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
          ) : breachesError ? (
            <p className="text-red-600 text-sm">{breachesError}</p>
          ) : !breaches || breaches.length === 0 ? (
            <p className="text-sm text-gray-400">No breaches in this period.</p>
          ) : (
            <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
              {breaches.map(b => (
                <li key={b.id} className="px-4 py-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-700">{b.productName}</span>
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium shrink-0 ${
                        BREACH_TYPE_COLORS[b.breachType] ?? 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {b.breachType.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {b.currentQuantity} / threshold {b.thresholdLevel} · {new Date(b.timestamp).toLocaleString()}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Frequently Low-Stock Products</h3>
          {frequentLoading ? (
            <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
          ) : frequentError ? (
            <p className="text-red-600 text-sm">{frequentError}</p>
          ) : !frequentProblems || frequentProblems.length === 0 ? (
            <p className="text-sm text-gray-400">No recurring problem products in this period.</p>
          ) : (
            <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
              {frequentProblems.map(p => (
                <li key={p.productId} className="flex items-center justify-between px-4 py-3 text-sm gap-3">
                  <div>
                    <span className="text-gray-700">{p.productName}</span>
                    <p className="text-xs text-gray-500 mt-0.5">{p.currentStock} in stock</p>
                  </div>
                  <span className="px-2 py-1 rounded-full text-xs font-medium shrink-0 bg-orange-100 text-orange-800">
                    {p.breachCount} breaches
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
