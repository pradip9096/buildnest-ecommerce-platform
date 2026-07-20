import { useState } from 'react';
import {
  createAdminShippingMethod,
  updateAdminShippingMethod,
  type AdminShippingMethod,
  type ShippingMethodFormInput,
} from '../../api/admin';

interface Props {
  /** null for create, an existing shipping method for edit. */
  method: AdminShippingMethod | null;
  onClose: () => void;
  onSaved: (method: AdminShippingMethod) => void;
}

export function ShippingMethodFormModal({ method, onClose, onSaved }: Props) {
  const isEdit = method !== null;
  const [name, setName] = useState(method?.name ?? '');
  const [description, setDescription] = useState(method?.description ?? '');
  const [baseCost, setBaseCost] = useState(method ? String(method.baseCost) : '');
  const [costPerKg, setCostPerKg] = useState(method ? String(method.costPerKg) : '0');
  const [estimatedDaysMin, setEstimatedDaysMin] = useState(
    method ? String(method.estimatedDaysMin) : ''
  );
  const [estimatedDaysMax, setEstimatedDaysMax] = useState(
    method ? String(method.estimatedDaysMax) : ''
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedName = name.trim();
    const cost = Number(baseCost);
    const perKg = Number(costPerKg || 0);
    const daysMin = Number(estimatedDaysMin);
    const daysMax = Number(estimatedDaysMax);

    if (trimmedName.length < 2) {
      setError('Name must be at least 2 characters.');
      return;
    }
    if (!Number.isFinite(cost) || cost < 0) {
      setError('Base cost must be a non-negative number.');
      return;
    }
    if (!Number.isFinite(perKg) || perKg < 0) {
      setError('Cost per kg must be a non-negative number.');
      return;
    }
    if (!Number.isInteger(daysMin) || daysMin < 0 || !Number.isInteger(daysMax) || daysMax < 0) {
      setError('Estimated days must be non-negative whole numbers.');
      return;
    }
    if (daysMax < daysMin) {
      setError('Max estimated days cannot be less than min estimated days.');
      return;
    }

    setLoading(true);
    setError(null);

    const input: ShippingMethodFormInput = {
      name: trimmedName,
      description: description.trim() || undefined,
      baseCost: cost,
      costPerKg: perKg,
      estimatedDaysMin: daysMin,
      estimatedDaysMax: daysMax,
    };

    try {
      const saved = isEdit
        ? await updateAdminShippingMethod(method.id, input)
        : await createAdminShippingMethod(input);
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save shipping method');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">
            {isEdit ? 'Edit Shipping Method' : 'New Shipping Method'}
          </h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="shipping-name" className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              id="shipping-name"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Standard Shipping"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
              minLength={2}
              maxLength={100}
            />
          </div>

          <div>
            <label htmlFor="shipping-description" className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              id="shipping-description"
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Optional description"
              rows={2}
              maxLength={500}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="shipping-base-cost" className="block text-sm font-medium text-gray-700 mb-1">Base Cost (₹)</label>
              <input
                id="shipping-base-cost"
                type="number"
                value={baseCost}
                onChange={e => setBaseCost(e.target.value)}
                min={0}
                step="0.01"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label htmlFor="shipping-cost-per-kg" className="block text-sm font-medium text-gray-700 mb-1">Cost per kg (₹)</label>
              <input
                id="shipping-cost-per-kg"
                type="number"
                value={costPerKg}
                onChange={e => setCostPerKg(e.target.value)}
                min={0}
                step="0.01"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="shipping-days-min" className="block text-sm font-medium text-gray-700 mb-1">Min Days</label>
              <input
                id="shipping-days-min"
                type="number"
                value={estimatedDaysMin}
                onChange={e => setEstimatedDaysMin(e.target.value)}
                min={0}
                step="1"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label htmlFor="shipping-days-max" className="block text-sm font-medium text-gray-700 mb-1">Max Days</label>
              <input
                id="shipping-days-max"
                type="number"
                value={estimatedDaysMax}
                onChange={e => setEstimatedDaysMax(e.target.value)}
                min={0}
                step="1"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
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
              {loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
