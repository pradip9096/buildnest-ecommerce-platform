import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchInventoryHighDemandLowStock,
  fetchInventorySeasonalPatterns,
  fetchInventoryStockTurnover,
  fetchInventoryRestockingPlan,
  type InventoryDemandProduct,
  type InventorySeasonalPattern,
  type InventoryTurnoverProduct,
  type InventoryRestockingPlan,
} from '../../api/admin';

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

const RISK_COLORS: Record<InventoryDemandProduct['riskLevel'], string> = {
  CRITICAL: 'bg-red-100 text-red-800',
  HIGH: 'bg-orange-100 text-orange-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  LOW: 'bg-gray-100 text-gray-700',
};

const HEALTH_COLORS: Record<InventoryTurnoverProduct['healthStatus'], string> = {
  HEALTHY: 'bg-green-100 text-green-800',
  OVERSTOCKED: 'bg-blue-100 text-blue-800',
  UNDERSTOCKED: 'bg-orange-100 text-orange-800',
  CRITICAL: 'bg-red-100 text-red-800',
};

export function InventoryAnalyticsTab() {
  const [startDate, setStartDate] = useState(() => toIsoDate(new Date(Date.now() - 30 * 86400000)));
  const [endDate, setEndDate] = useState(() => toIsoDate(new Date()));

  const { data: demandProducts, loading: demandLoading, error: demandError } = useAsync<InventoryDemandProduct[]>(
    () => fetchInventoryHighDemandLowStock({ startDate, endDate }),
    [startDate, endDate]
  );
  const { data: patterns, loading: patternsLoading, error: patternsError } = useAsync<InventorySeasonalPattern[]>(
    () => fetchInventorySeasonalPatterns({ startDate, endDate }),
    [startDate, endDate]
  );
  const { data: turnover, loading: turnoverLoading, error: turnoverError } = useAsync<InventoryTurnoverProduct[]>(
    () => fetchInventoryStockTurnover({ startDate, endDate }),
    [startDate, endDate]
  );

  const [daysPeriod, setDaysPeriod] = useState(30);
  const [plan, setPlan] = useState<InventoryRestockingPlan | null>(null);
  const [planLoading, setPlanLoading] = useState(false);
  const [planError, setPlanError] = useState<string | null>(null);

  const handleGeneratePlan = async () => {
    setPlanLoading(true);
    setPlanError(null);
    try {
      const result = await fetchInventoryRestockingPlan(daysPeriod);
      setPlan(result);
    } catch (e) {
      setPlanError(e instanceof Error ? e.message : 'Failed to generate restocking plan');
      setPlan(null);
    } finally {
      setPlanLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Inventory Analytics</h2>
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
        <h3 className="font-semibold text-gray-900 mb-3">High Demand, Low Stock</h3>
        {demandLoading ? (
          <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
        ) : demandError ? (
          <p className="text-red-600 text-sm">{demandError}</p>
        ) : !demandProducts || demandProducts.length === 0 ? (
          <p className="text-sm text-gray-400">No high-demand, low-stock products in this period.</p>
        ) : (
          <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
            {demandProducts.map(p => (
              <li key={p.productId} className="flex items-center justify-between px-4 py-3 text-sm gap-3">
                <div>
                  <span className="text-gray-700">{p.productName}</span>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {p.currentStock} in stock · shortfall {p.shortfall} · {p.recommendedAction}
                  </p>
                </div>
                <span className={`px-2 py-1 rounded-full text-xs font-medium shrink-0 ${RISK_COLORS[p.riskLevel]}`}>
                  {p.riskLevel}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Seasonal Demand Patterns</h3>
          {patternsLoading ? (
            <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
          ) : patternsError ? (
            <p className="text-red-600 text-sm">{patternsError}</p>
          ) : !patterns || patterns.length === 0 ? (
            <p className="text-sm text-gray-400">No seasonal patterns detected in this period.</p>
          ) : (
            <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
              {patterns.map(p => (
                <li key={p.productId} className="px-4 py-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-700">{p.productName}</span>
                    <span className="text-gray-500 text-xs">{p.pattern.replace(/_/g, ' ')}</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {p.totalBreaches} breaches · {p.breachFrequency} · suggested safety stock {p.suggestedSafetyStock}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Stock Turnover</h3>
          {turnoverLoading ? (
            <div className="animate-pulse h-24 bg-gray-100 rounded-2xl" />
          ) : turnoverError ? (
            <p className="text-red-600 text-sm">{turnoverError}</p>
          ) : !turnover || turnover.length === 0 ? (
            <p className="text-sm text-gray-400">No turnover data for this period.</p>
          ) : (
            <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
              {turnover.map(t => (
                <li key={t.productId} className="flex items-center justify-between px-4 py-3 text-sm gap-3">
                  <div>
                    <span className="text-gray-700">{t.productName}</span>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {t.currentStock} in stock · {t.recentTransactions} transactions · {t.turnoverCategory.replace(/_/g, ' ')}
                    </p>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium shrink-0 ${HEALTH_COLORS[t.healthStatus]}`}>
                    {t.healthStatus}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Predictive Restocking Plan</h3>
        <div className="border border-gray-100 rounded-2xl p-4 space-y-4">
          <div className="flex flex-wrap items-end gap-3">
            <label className="text-xs text-gray-500">
              Analysis period (days)
              <input
                type="number"
                min={1}
                value={daysPeriod}
                onChange={e => setDaysPeriod(Number(e.target.value))}
                className="block mt-1 border border-gray-200 rounded-lg px-2 py-1 text-sm w-28"
              />
            </label>
            <button
              type="button"
              onClick={handleGeneratePlan}
              disabled={planLoading}
              className="px-4 py-2 rounded-xl text-sm font-medium bg-primary-500 text-white disabled:opacity-50"
            >
              {planLoading ? 'Generating…' : 'Generate Plan'}
            </button>
            {planError && <span className="text-sm text-red-600">{planError}</span>}
          </div>

          {plan && (
            <div className="grid grid-cols-2 gap-4">
              <div className="border border-gray-200 rounded-xl p-4">
                <p className="text-2xl font-bold text-gray-900">{plan.urgentCount}</p>
                <p className="text-sm text-gray-500 mt-0.5">Urgent Restocks</p>
              </div>
              <div className="border border-gray-200 rounded-xl p-4">
                <p className="text-2xl font-bold text-gray-900">{plan.patternCount}</p>
                <p className="text-sm text-gray-500 mt-0.5">Seasonal Patterns</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
