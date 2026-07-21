import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchSalesDashboard,
  fetchCustomerLifetimeValue,
  type SalesDashboard,
} from '../../api/admin';

const fmtCurrency = (value: number) =>
  `₹${Number(value).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;

const fmtPercent = (value: number) => `${(value * 100).toFixed(1)}%`;

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

const STAT_CARDS: {
  key: keyof SalesDashboard;
  label: string;
  icon: string;
  color: string;
  border: string;
  fmt: (v: number) => string;
}[] = [
  { key: 'monthlyRevenue', label: 'Revenue (30d)', icon: '💰', color: 'bg-green-50 text-green-700', border: 'border-green-200', fmt: fmtCurrency },
  { key: 'averageOrderValue', label: 'Avg Order Value', icon: '🧾', color: 'bg-blue-50 text-blue-700', border: 'border-blue-200', fmt: fmtCurrency },
  { key: 'conversionRate', label: 'Conversion Rate', icon: '📈', color: 'bg-purple-50 text-purple-700', border: 'border-purple-200', fmt: fmtPercent },
  { key: 'cartAbandonmentRate', label: 'Cart Abandonment', icon: '🛒', color: 'bg-amber-50 text-amber-700', border: 'border-amber-200', fmt: fmtPercent },
];

export function SalesAnalyticsTab() {
  const [startDate, setStartDate] = useState(() => toIsoDate(new Date(Date.now() - 30 * 86400000)));
  const [endDate, setEndDate] = useState(() => toIsoDate(new Date()));
  const { data: dashboard, loading, error } = useAsync<SalesDashboard>(
    () => fetchSalesDashboard({ startDate, endDate }),
    [startDate, endDate]
  );

  const [clvUserId, setClvUserId] = useState('');
  const [clv, setClv] = useState<number | null>(null);
  const [clvError, setClvError] = useState<string | null>(null);
  const [clvLoading, setClvLoading] = useState(false);

  const handleClvLookup = async () => {
    const userId = Number(clvUserId);
    if (!Number.isInteger(userId) || userId <= 0) {
      setClvError('Enter a valid user ID');
      setClv(null);
      return;
    }
    setClvLoading(true);
    setClvError(null);
    try {
      const value = await fetchCustomerLifetimeValue(userId);
      setClv(value);
    } catch (e) {
      setClvError(e instanceof Error ? e.message : 'Failed to load customer lifetime value');
      setClv(null);
    } finally {
      setClvLoading(false);
    }
  };

  if (loading) return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
      {[1, 2, 3, 4].map(i => <div key={i} className="h-28 bg-gray-100 rounded-2xl" />)}
    </div>
  );

  if (error) return <p className="text-red-600 text-sm">{error}</p>;
  if (!dashboard) return null;

  const trend = dashboard.revenueTrend ?? [];
  const maxTrendRevenue = Math.max(1, ...trend.map(p => p.revenue));
  const topProducts = dashboard.topSellingProducts ?? [];
  const revenueByCategory = Object.entries(dashboard.revenueByCategory ?? {});
  const maxCategoryRevenue = Math.max(1, ...revenueByCategory.map(([, v]) => v));

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Sales Analytics</h2>
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

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {STAT_CARDS.map(card => (
          <div key={card.key} className={`border ${card.border} rounded-2xl p-5`}>
            <div className={`inline-flex items-center justify-center w-10 h-10 rounded-xl text-xl ${card.color} mb-3`}>
              {card.icon}
            </div>
            <p className="text-2xl font-bold text-gray-900">{card.fmt(Number(dashboard[card.key]))}</p>
            <p className="text-sm text-gray-500 mt-0.5">{card.label}</p>
          </div>
        ))}
      </div>

      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Revenue Trend</h3>
        {trend.length === 0 ? (
          <p className="text-sm text-gray-400">No revenue data for this period.</p>
        ) : (
          <div className="border border-gray-100 rounded-2xl p-6 bg-gray-50">
            <div className="flex items-end gap-2 h-32 overflow-x-auto">
              {trend.map(point => {
                const pct = Math.max(4, Math.round((point.revenue / maxTrendRevenue) * 100));
                return (
                  <div key={point.date} className="flex flex-col items-center gap-1 min-w-[2rem] flex-1">
                    <div className="w-full flex items-end" style={{ height: '80px' }}>
                      <div
                        className="w-full rounded-t-lg bg-green-400 transition-all"
                        style={{ height: `${pct}%` }}
                        title={`${point.date}: ${fmtCurrency(point.revenue)} (${point.orderCount} orders)`}
                      />
                    </div>
                    <span className="text-[10px] text-gray-500">{point.date.slice(5)}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Top Selling Products</h3>
          {topProducts.length === 0 ? (
            <p className="text-sm text-gray-400">No product sales in this period.</p>
          ) : (
            <ul className="border border-gray-100 rounded-2xl divide-y divide-gray-100">
              {topProducts.map(p => (
                <li key={p.productId} className="flex items-center justify-between px-4 py-3 text-sm">
                  <span className="text-gray-700">{p.productName}</span>
                  <span className="text-gray-500">{p.unitsSold} units · {fmtCurrency(p.revenue)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div>
          <h3 className="font-semibold text-gray-900 mb-3">Revenue by Category</h3>
          {revenueByCategory.length === 0 ? (
            <p className="text-sm text-gray-400">No category revenue in this period.</p>
          ) : (
            <div className="border border-gray-100 rounded-2xl p-4 space-y-2">
              {revenueByCategory.map(([category, revenue]) => (
                <div key={category} className="flex items-center gap-3 text-sm">
                  <span className="w-28 truncate text-gray-600">{category}</span>
                  <div className="flex-1 h-2.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-purple-400 rounded-full"
                      style={{ width: `${Math.max(4, Math.round((revenue / maxCategoryRevenue) * 100))}%` }}
                    />
                  </div>
                  <span className="text-gray-500 w-20 text-right">{fmtCurrency(revenue)}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Customer Lifetime Value Lookup</h3>
        <div className="border border-gray-100 rounded-2xl p-4 flex flex-wrap items-end gap-3">
          <label className="text-xs text-gray-500">
            User ID
            <input
              type="number"
              min={1}
              value={clvUserId}
              onChange={e => setClvUserId(e.target.value)}
              className="block mt-1 border border-gray-200 rounded-lg px-2 py-1 text-sm w-32"
            />
          </label>
          <button
            type="button"
            onClick={handleClvLookup}
            disabled={clvLoading}
            className="px-4 py-2 rounded-xl text-sm font-medium bg-primary-500 text-white disabled:opacity-50"
          >
            {clvLoading ? 'Looking up…' : 'Lookup'}
          </button>
          {clv !== null && (
            <span className="text-sm text-gray-700 font-semibold">{fmtCurrency(clv)}</span>
          )}
          {clvError && <span className="text-sm text-red-600">{clvError}</span>}
        </div>
      </div>
    </div>
  );
}
