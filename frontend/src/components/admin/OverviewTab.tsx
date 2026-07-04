import { useAsync } from '../../hooks/useAsync';
import { fetchDashboardStats, type DashboardStats } from '../../api/admin';

interface Props { token: string; }

const STAT_CARDS = [
  { key: 'totalUsers'    as const, label: 'Total Users',    icon: '👥', color: 'bg-blue-50 text-blue-700',   border: 'border-blue-200' },
  { key: 'totalProducts' as const, label: 'Products',       icon: '📦', color: 'bg-amber-50 text-amber-700', border: 'border-amber-200' },
  { key: 'totalOrders'   as const, label: 'Total Orders',   icon: '🛒', color: 'bg-green-50 text-green-700', border: 'border-green-200' },
  { key: 'totalRevenue'  as const, label: 'Total Revenue',  icon: '💰', color: 'bg-purple-50 text-purple-700',border: 'border-purple-200' },
];

export function OverviewTab({ token }: Props) {
  const { data: stats, loading, error } = useAsync<DashboardStats>(
    () => fetchDashboardStats(token),
    [token]
  );

  if (loading) return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
      {[1,2,3,4].map(i => <div key={i} className="h-28 bg-gray-100 rounded-2xl" />)}
    </div>
  );

  if (error) return <p className="text-red-600 text-sm">{error}</p>;
  if (!stats) return null;

  const fmt = (key: keyof DashboardStats) =>
    key === 'totalRevenue'
      ? `₹${Number(stats[key]).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`
      : Number(stats[key]).toLocaleString('en-IN');

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Platform Overview</h2>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {STAT_CARDS.map(card => (
            <div key={card.key} className={`border ${card.border} rounded-2xl p-5`}>
              <div className={`inline-flex items-center justify-center w-10 h-10 rounded-xl text-xl ${card.color} mb-3`}>
                {card.icon}
              </div>
              <p className="text-2xl font-bold text-gray-900">{fmt(card.key)}</p>
              <p className="text-sm text-gray-500 mt-0.5">{card.label}</p>
            </div>
          ))}
        </div>
      </div>

      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Revenue vs Orders</h3>
        <div className="border border-gray-100 rounded-2xl p-6 bg-gray-50">
          <div className="flex items-end gap-6 h-32">
            {/* Normalised bar chart using the two main metrics */}
            {[
              { label: 'Orders', value: stats.totalOrders, max: stats.totalOrders, color: 'bg-amber-400' },
              { label: 'Revenue (₹00s)', value: Math.round(stats.totalRevenue / 100), max: Math.round(stats.totalRevenue / 100), color: 'bg-green-400' },
              { label: 'Users', value: stats.totalUsers, max: stats.totalOrders, color: 'bg-blue-400' },
              { label: 'Products', value: stats.totalProducts, max: stats.totalOrders, color: 'bg-purple-400' },
            ].map(bar => {
              const pct = bar.max > 0 ? Math.max(4, Math.round((bar.value / bar.max) * 100)) : 4;
              return (
                <div key={bar.label} className="flex flex-col items-center gap-1 flex-1">
                  <span className="text-xs font-semibold text-gray-700">{bar.value.toLocaleString()}</span>
                  <div className="w-full flex items-end" style={{ height: '80px' }}>
                    <div
                      className={`w-full rounded-t-lg ${bar.color} transition-all`}
                      style={{ height: `${pct}%` }}
                    />
                  </div>
                  <span className="text-xs text-gray-500 text-center leading-tight">{bar.label}</span>
                </div>
              );
            })}
          </div>
        </div>
        <p className="text-xs text-gray-400 mt-2 text-center">Snapshot — live time-series charts available in the Analytics section</p>
      </div>
    </div>
  );
}
