import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchProductVariants,
  createProductVariant,
  updateProductVariant,
  deleteProductVariant,
  type AdminProduct,
  type AdminProductVariant,
  type VariantFormInput,
} from '../../api/admin';

interface Props {
  product: AdminProduct;
  onClose: () => void;
}

const EMPTY_FORM: VariantFormInput = {
  sku: '',
  size: '',
  colour: '',
  priceAdjustment: 0,
  isActive: true,
  initialStockQuantity: 0,
  minimumStockLevel: 0,
};

export function ProductVariantsModal({ product, onClose }: Props) {
  const { data, loading, error, setData } = useAsync<AdminProductVariant[]>(
    () => fetchProductVariants(product.id),
    [product.id]
  );
  const variants = data ?? [];
  const [editingId, setEditingId] = useState<number | 'new' | null>(null);
  const [form, setForm] = useState<VariantFormInput>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const startCreate = () => {
    setForm(EMPTY_FORM);
    setEditingId('new');
    setActionError(null);
  };

  const startEdit = (variant: AdminProductVariant) => {
    setForm({
      sku: variant.sku,
      size: variant.size ?? '',
      colour: variant.colour ?? '',
      priceAdjustment: variant.priceAdjustment,
      isActive: variant.isActive,
    });
    setEditingId(variant.id);
    setActionError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setActionError(null);
    try {
      if (editingId === 'new') {
        const created = await createProductVariant(product.id, form);
        setData(prev => [...(prev ?? []), created]);
      } else if (editingId !== null) {
        const updated = await updateProductVariant(product.id, editingId, form);
        setData(prev => (prev ?? []).map(v => (v.id === editingId ? updated : v)));
      }
      setEditingId(null);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Failed to save variant');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (variant: AdminProductVariant) => {
    if (!confirm(`Deactivate variant "${variant.sku}"?`)) return;
    setBusyId(variant.id);
    setActionError(null);
    try {
      await deleteProductVariant(product.id, variant.id);
      setData(prev => (prev ?? []).map(v => (v.id === variant.id ? { ...v, isActive: false } : v)));
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Failed to delete variant');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">Variants — {product.name}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <div className="px-6 py-4 space-y-4">
          {error && <p className="text-red-600 text-sm">{error}</p>}
          {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

          {editingId === null && (
            <button
              type="button"
              onClick={startCreate}
              className="text-sm font-medium text-primary-600 hover:text-primary-800"
            >
              + Add variant
            </button>
          )}

          {editingId !== null && (
            <form onSubmit={handleSubmit} className="space-y-2 border border-gray-100 rounded-xl p-3">
              <div className="grid grid-cols-2 gap-2">
                <input
                  aria-label="SKU"
                  placeholder="SKU"
                  value={form.sku}
                  onChange={e => setForm(f => ({ ...f, sku: e.target.value }))}
                  required
                  className="border border-gray-200 rounded px-2 py-1 text-sm"
                />
                <input
                  aria-label="Price adjustment"
                  type="number"
                  step="0.01"
                  placeholder="Price adjustment"
                  value={form.priceAdjustment}
                  onChange={e => setForm(f => ({ ...f, priceAdjustment: Number(e.target.value) }))}
                  required
                  className="border border-gray-200 rounded px-2 py-1 text-sm"
                />
                <input
                  aria-label="Size"
                  placeholder="Size"
                  value={form.size}
                  onChange={e => setForm(f => ({ ...f, size: e.target.value }))}
                  className="border border-gray-200 rounded px-2 py-1 text-sm"
                />
                <input
                  aria-label="Colour"
                  placeholder="Colour"
                  value={form.colour}
                  onChange={e => setForm(f => ({ ...f, colour: e.target.value }))}
                  className="border border-gray-200 rounded px-2 py-1 text-sm"
                />
                {editingId === 'new' && (
                  <>
                    <input
                      aria-label="Initial stock quantity"
                      type="number"
                      placeholder="Initial stock"
                      value={form.initialStockQuantity}
                      onChange={e => setForm(f => ({ ...f, initialStockQuantity: Number(e.target.value) }))}
                      required
                      className="border border-gray-200 rounded px-2 py-1 text-sm"
                    />
                    <input
                      aria-label="Minimum stock level"
                      type="number"
                      placeholder="Minimum stock level"
                      value={form.minimumStockLevel}
                      onChange={e => setForm(f => ({ ...f, minimumStockLevel: Number(e.target.value) }))}
                      required
                      className="border border-gray-200 rounded px-2 py-1 text-sm"
                    />
                  </>
                )}
              </div>
              <label className="flex items-center gap-2 text-sm text-gray-600">
                <input
                  type="checkbox"
                  checked={form.isActive}
                  onChange={e => setForm(f => ({ ...f, isActive: e.target.checked }))}
                />
                Active
              </label>
              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={saving}
                  className="text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 disabled:opacity-50 rounded px-3 py-1"
                >
                  {saving ? 'Saving…' : 'Save'}
                </button>
                <button
                  type="button"
                  onClick={() => setEditingId(null)}
                  className="text-sm font-medium text-gray-600 hover:text-gray-800"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}

          {loading ? (
            <p className="text-sm text-gray-400">Loading variants…</p>
          ) : variants.length === 0 ? (
            <p className="text-sm text-gray-400">No variants yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b border-gray-100">
                  <th className="py-1">SKU</th>
                  <th className="py-1">Size / Colour</th>
                  <th className="py-1">Adjustment</th>
                  <th className="py-1">Status</th>
                  <th className="py-1"></th>
                </tr>
              </thead>
              <tbody>
                {variants.map(variant => (
                  <tr key={variant.id} className="border-b border-gray-50">
                    <td className="py-1.5">{variant.sku}</td>
                    <td className="py-1.5">{[variant.size, variant.colour].filter(Boolean).join(' / ') || '—'}</td>
                    <td className="py-1.5">{variant.priceAdjustment.toFixed(2)}</td>
                    <td className="py-1.5">{variant.isActive ? 'Active' : 'Inactive'}</td>
                    <td className="py-1.5 text-right space-x-2">
                      <button
                        type="button"
                        onClick={() => startEdit(variant)}
                        disabled={busyId === variant.id}
                        className="text-xs font-medium text-primary-600 hover:text-primary-800"
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(variant)}
                        disabled={busyId === variant.id || !variant.isActive}
                        className="text-xs font-medium text-red-600 hover:text-red-800 disabled:opacity-40"
                      >
                        Deactivate
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
