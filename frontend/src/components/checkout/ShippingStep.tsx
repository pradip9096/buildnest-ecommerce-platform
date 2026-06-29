import { useState } from 'react';
import type { ShippingOption } from '../../types';

interface Props {
  options: ShippingOption[];
  loading: boolean;
  error: string | null;
  onNext: (shippingMethodId: number) => void;
  onBack: () => void;
}

export function ShippingStep({ options, loading, error, onNext, onBack }: Props) {
  const [selected, setSelected] = useState<number | null>(
    options.length > 0 ? options[0].id : null
  );
  const [submitError, setSubmitError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected) { setSubmitError('Please select a shipping method'); return; }
    onNext(selected);
  };

  if (loading) {
    return (
      <div className="space-y-3">
        <div className="h-4 bg-gray-100 rounded animate-pulse w-1/3" />
        {[1, 2].map(i => (
          <div key={i} className="h-20 bg-gray-100 rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Shipping Method</h2>

      {(error || submitError) && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error ?? submitError}
        </p>
      )}

      {options.length === 0 ? (
        <p className="text-gray-500 text-sm">No shipping options available for your area.</p>
      ) : (
        <div className="space-y-3">
          {options.map(opt => (
            <label
              key={opt.id}
              className={`flex items-start gap-3 border-2 rounded-xl p-4 cursor-pointer transition-colors ${
                selected === opt.id ? 'border-amber-500 bg-amber-50' : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <input
                type="radio"
                name="shipping"
                value={opt.id}
                checked={selected === opt.id}
                onChange={() => { setSelected(opt.id); setSubmitError(null); }}
                className="mt-0.5 accent-amber-500"
              />
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-gray-900">{opt.name}</span>
                  <span className="font-semibold text-gray-900">₹{Number(opt.calculatedCost).toFixed(2)}</span>
                </div>
                {opt.description && (
                  <p className="text-xs text-gray-500 mt-0.5">{opt.description}</p>
                )}
                <p className="text-xs text-gray-500 mt-0.5">
                  Estimated {opt.estimatedDaysMin}–{opt.estimatedDaysMax} business days
                </p>
              </div>
            </label>
          ))}
        </div>
      )}

      <div className="flex gap-3 mt-6">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 border border-gray-300 text-gray-700 font-semibold py-3 rounded-xl hover:bg-gray-50 transition-colors"
        >
          Back
        </button>
        <button
          type="submit"
          disabled={options.length === 0}
          className="flex-1 bg-amber-500 hover:bg-amber-600 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors"
        >
          Continue to Payment
        </button>
      </div>
    </form>
  );
}
