import { useState } from 'react';
import {
  createAdminProduct,
  updateAdminProduct,
  type AdminProduct,
  type ProductFormInput,
} from '../../api/admin';
import type { AdminCategory } from '../../api/admin';

interface Props {
  /** null for create, an existing product for edit. */
  product: AdminProduct | null;
  categories: AdminCategory[];
  onClose: () => void;
  onSaved: (product: AdminProduct) => void;
}

const SKU_PATTERN = /^[A-Z0-9-]{3,20}$/;

export function ProductFormModal({ product, categories, onClose, onSaved }: Props) {
  const isEdit = product !== null;
  const [name, setName] = useState(product?.name ?? '');
  const [description, setDescription] = useState(product?.description ?? '');
  const [price, setPrice] = useState(product?.price != null ? String(product.price) : '');
  const [discountPrice, setDiscountPrice] = useState(
    product?.discountPrice != null ? String(product.discountPrice) : ''
  );
  const [stockQuantity, setStockQuantity] = useState(
    product?.stockQuantity != null ? String(product.stockQuantity) : ''
  );
  const [sku, setSku] = useState(product?.sku ?? '');
  const [categoryId, setCategoryId] = useState<string>(
    product?.category?.id != null ? String(product.category.id) : ''
  );
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? '');
  const [isFeatured, setIsFeatured] = useState(product?.isFeatured ?? false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (name.trim().length < 3) {
      setError('Product name must be between 3 and 255 characters.');
      return;
    }
    if (description.trim().length < 10) {
      setError('Description must be between 10 and 2000 characters.');
      return;
    }
    const priceValue = Number(price);
    if (!price || Number.isNaN(priceValue) || priceValue <= 0 || priceValue > 999999.99) {
      setError('Price must be positive and not exceed 999999.99.');
      return;
    }
    if (!categoryId) {
      setError('Category is required.');
      return;
    }
    if (sku.trim() && !SKU_PATTERN.test(sku.trim())) {
      setError('SKU must be alphanumeric (uppercase/digits/hyphen), 3-20 characters.');
      return;
    }

    setLoading(true);
    setError(null);

    const input: ProductFormInput = {
      name: name.trim(),
      description: description.trim(),
      price: priceValue,
      discountPrice: discountPrice ? Number(discountPrice) : undefined,
      // Stock is Inventory-owned (#485) — the backend now ignores
      // stockQuantity on update, so only send it at creation time to avoid
      // an edit field that silently does nothing.
      stockQuantity: !isEdit && stockQuantity ? Number(stockQuantity) : undefined,
      sku: sku.trim() || undefined,
      categoryId: Number(categoryId),
      imageUrl: imageUrl.trim() || undefined,
      isFeatured,
    };

    try {
      const saved = isEdit
        ? await updateAdminProduct(product.id, input)
        : await createAdminProduct(input);
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save product');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">{isEdit ? 'Edit Product' : 'New Product'}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="product-name" className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              id="product-name"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Premium Cement 50kg"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
              minLength={3}
              maxLength={255}
            />
          </div>

          <div>
            <label htmlFor="product-description" className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              id="product-description"
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Product description"
              rows={3}
              maxLength={2000}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="product-price" className="block text-sm font-medium text-gray-700 mb-1">Price</label>
              <input
                id="product-price"
                type="number"
                step="0.01"
                min="0.01"
                max="999999.99"
                value={price}
                onChange={e => setPrice(e.target.value)}
                placeholder="499.99"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label htmlFor="product-discount-price" className="block text-sm font-medium text-gray-700 mb-1">Discount Price</label>
              <input
                id="product-discount-price"
                type="number"
                step="0.01"
                min="0.01"
                max="999999.99"
                value={discountPrice}
                onChange={e => setDiscountPrice(e.target.value)}
                placeholder="Optional"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="product-stock" className="block text-sm font-medium text-gray-700 mb-1">
                Initial Stock Quantity
              </label>
              <input
                id="product-stock"
                type="number"
                min="1"
                max="10000"
                value={stockQuantity}
                onChange={e => setStockQuantity(e.target.value)}
                placeholder="100"
                disabled={isEdit}
                title={isEdit ? 'Manage stock from the Inventory tab' : undefined}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 disabled:bg-gray-50 disabled:text-gray-400"
              />
              {isEdit && (
                <p className="mt-1 text-xs text-gray-400">Manage stock from the Inventory tab.</p>
              )}
            </div>
            <div>
              <label htmlFor="product-sku" className="block text-sm font-medium text-gray-700 mb-1">SKU</label>
              <input
                id="product-sku"
                type="text"
                value={sku}
                onChange={e => setSku(e.target.value.toUpperCase())}
                placeholder="CEM-50KG"
                maxLength={20}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>
          </div>

          <div>
            <label htmlFor="product-category" className="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <select
              id="product-category"
              value={categoryId}
              onChange={e => setCategoryId(e.target.value)}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            >
              <option value="">— Select a category —</option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="product-image-url" className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <input
              id="product-image-url"
              type="text"
              value={imageUrl}
              onChange={e => setImageUrl(e.target.value)}
              placeholder="https://…"
              maxLength={500}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            />
          </div>

          <div className="flex items-center gap-2">
            <input
              id="product-featured"
              type="checkbox"
              checked={isFeatured}
              onChange={e => setIsFeatured(e.target.checked)}
              className="rounded border-gray-300 text-primary-500 focus:ring-primary-400"
            />
            <label htmlFor="product-featured" className="text-sm font-medium text-gray-700">Featured on home page</label>
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
