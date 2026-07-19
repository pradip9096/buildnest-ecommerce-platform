import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import { fetchAdminCoupons, deactivateAdminCoupon, type AdminCoupon } from '../../api/admin';
import { CouponFormModal } from './CouponFormModal';

function formatDiscount(coupon: AdminCoupon): string {
  return coupon.discountType === 'PERCENTAGE'
    ? `${coupon.discountValue}%`
    : `₹${coupon.discountValue}`;
}

function formatUsage(coupon: AdminCoupon): string {
  return coupon.usageLimit == null
    ? `${coupon.usageCount} / ∞`
    : `${coupon.usageCount} / ${coupon.usageLimit}`;
}

export function CouponsTab() {
  const { data, loading, error, setData } = useAsync<AdminCoupon[]>(() => fetchAdminCoupons(), []);
  const coupons = data ?? [];
  const [search, setSearch] = useState('');
  const [deactivating, setDeactivating] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);

  const filtered = search
    ? coupons.filter(c => c.code.toLowerCase().includes(search.toLowerCase()))
    : coupons;

  const handleDeactivate = async (coupon: AdminCoupon) => {
    if (!confirm(`Deactivate coupon "${coupon.code}"? It can no longer be applied.`)) return;
    setDeactivating(coupon.id);
    setActionError(null);
    try {
      const updated = await deactivateAdminCoupon(coupon.id);
      setData(prev => (prev ?? []).map(c => (c.id === updated.id ? updated : c)));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to deactivate coupon');
    } finally {
      setDeactivating(null);
    }
  };

  const handleSaved = (saved: AdminCoupon) => {
    setData(prev => [...(prev ?? []), saved]);
    setShowModal(false);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Coupons</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search coupons…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{coupons.length} total</span>
          <button
            type="button"
            onClick={() => setShowModal(true)}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Coupon
          </button>
        </div>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}
      {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Code</th>
              <th className="px-4 py-3">Discount</th>
              <th className="px-4 py-3">Min Order</th>
              <th className="px-4 py-3">Usage</th>
              <th className="px-4 py-3">Expires</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(7)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No coupons match your search' : 'No coupons found'}
                </td>
              </tr>
            ) : filtered.map(coupon => (
              <tr key={coupon.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 font-medium text-gray-900">{coupon.code}</td>
                <td className="px-4 py-3 text-gray-500">{formatDiscount(coupon)}</td>
                <td className="px-4 py-3 text-gray-500">₹{coupon.minOrderValue}</td>
                <td className="px-4 py-3 text-gray-500">{formatUsage(coupon)}</td>
                <td className="px-4 py-3 text-gray-500">
                  {coupon.expiresAt ? new Date(coupon.expiresAt).toLocaleDateString() : 'Never'}
                </td>
                <td className="px-4 py-3">
                  <span
                    className={`text-xs font-medium px-2 py-1 rounded-full ${
                      coupon.isActive
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {coupon.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  {coupon.isActive && (
                    <button
                      type="button"
                      onClick={() => handleDeactivate(coupon)}
                      disabled={deactivating === coupon.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {deactivating === coupon.id ? 'Deactivating…' : 'Deactivate'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <CouponFormModal
          onClose={() => setShowModal(false)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
