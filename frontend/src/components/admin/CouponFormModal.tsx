import { useState } from 'react';
import { createAdminCoupon, type AdminCoupon, type CouponDiscountType, type CouponFormInput } from '../../api/admin';

interface Props {
  onClose: () => void;
  onSaved: (coupon: AdminCoupon) => void;
}

export function CouponFormModal({ onClose, onSaved }: Props) {
  const [code, setCode] = useState('');
  const [discountType, setDiscountType] = useState<CouponDiscountType>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState('');
  const [minOrderValue, setMinOrderValue] = useState('');
  const [usageLimit, setUsageLimit] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedCode = code.trim();
    const value = Number(discountValue);
    if (trimmedCode.length < 2) {
      setError('Coupon code must be at least 2 characters.');
      return;
    }
    if (!Number.isFinite(value) || value <= 0) {
      setError('Discount value must be a positive number.');
      return;
    }
    if (discountType === 'PERCENTAGE' && value > 100) {
      setError('Percentage discount cannot exceed 100.');
      return;
    }
    setLoading(true);
    setError(null);

    const input: CouponFormInput = {
      code: trimmedCode,
      discountType,
      discountValue: value,
      minOrderValue: minOrderValue.trim() ? Number(minOrderValue) : undefined,
      usageLimit: usageLimit.trim() ? Number(usageLimit) : null,
      expiresAt: expiresAt.trim() ? new Date(expiresAt).toISOString() : null,
    };

    try {
      const saved = await createAdminCoupon(input);
      onSaved(saved);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create coupon');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">New Coupon</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="coupon-code" className="block text-sm font-medium text-gray-700 mb-1">Code</label>
            <input
              id="coupon-code"
              type="text"
              value={code}
              onChange={e => setCode(e.target.value.toUpperCase())}
              placeholder="e.g. SAVE10"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
              minLength={2}
              maxLength={50}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="coupon-discount-type" className="block text-sm font-medium text-gray-700 mb-1">Discount Type</label>
              <select
                id="coupon-discount-type"
                value={discountType}
                onChange={e => setDiscountType(e.target.value as CouponDiscountType)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              >
                <option value="PERCENTAGE">Percentage</option>
                <option value="FIXED">Fixed Amount</option>
              </select>
            </div>
            <div>
              <label htmlFor="coupon-discount-value" className="block text-sm font-medium text-gray-700 mb-1">
                {discountType === 'PERCENTAGE' ? 'Discount %' : 'Discount ₹'}
              </label>
              <input
                id="coupon-discount-value"
                type="number"
                value={discountValue}
                onChange={e => setDiscountValue(e.target.value)}
                min={0}
                step="0.01"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="coupon-min-order" className="block text-sm font-medium text-gray-700 mb-1">Min Order Value</label>
              <input
                id="coupon-min-order"
                type="number"
                value={minOrderValue}
                onChange={e => setMinOrderValue(e.target.value)}
                min={0}
                step="0.01"
                placeholder="0"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>
            <div>
              <label htmlFor="coupon-usage-limit" className="block text-sm font-medium text-gray-700 mb-1">Usage Limit</label>
              <input
                id="coupon-usage-limit"
                type="number"
                value={usageLimit}
                onChange={e => setUsageLimit(e.target.value)}
                min={1}
                placeholder="Unlimited"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>
          </div>

          <div>
            <label htmlFor="coupon-expires-at" className="block text-sm font-medium text-gray-700 mb-1">Expires At (optional)</label>
            <input
              id="coupon-expires-at"
              type="date"
              value={expiresAt}
              onChange={e => setExpiresAt(e.target.value)}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            />
          </div>

          {error && <p className="text-red-600 text-sm">{error}</p>}

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 border border-gray-200 text-gray-700 rounded-xl py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-primary-500 hover:bg-primary-600 text-white rounded-xl py-2.5 text-sm font-semibold disabled:opacity-60 transition-colors"
            >
              {loading ? 'Saving…' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
