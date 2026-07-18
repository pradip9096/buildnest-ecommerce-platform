import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchAdminProducts,
  deleteAdminProduct,
  fetchAdminCategories,
  type AdminProduct,
} from '../../api/admin';
import { ProductFormModal } from './ProductFormModal';
import { ProductImagesModal } from './ProductImagesModal';

export function ProductsTab() {
  const { data, loading, error, setData } = useAsync<AdminProduct[]>(() => fetchAdminProducts(), []);
  const { data: categoriesData } = useAsync(() => fetchAdminCategories(), []);
  const products = data ?? [];
  const categories = categoriesData ?? [];
  const [search, setSearch] = useState('');
  const [deleting, setDeleting] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [modalProduct, setModalProduct] = useState<AdminProduct | 'new' | null>(null);
  const [imagesProduct, setImagesProduct] = useState<AdminProduct | null>(null);

  const filtered = search
    ? products.filter(
        p =>
          p.name.toLowerCase().includes(search.toLowerCase()) ||
          (p.sku ?? '').toLowerCase().includes(search.toLowerCase())
      )
    : products;

  const handleDelete = async (product: AdminProduct) => {
    if (!confirm(`Delete product "${product.name}"? This cannot be undone.`)) return;
    setDeleting(product.id);
    setDeleteError(null);
    try {
      await deleteAdminProduct(product.id);
      setData(prev => (prev ?? []).map(p => (p.id === product.id ? { ...p, isActive: false } : p)));
    } catch (e) {
      setDeleteError(e instanceof Error ? e.message : 'Failed to delete product');
    } finally {
      setDeleting(null);
    }
  };

  const handleSaved = (saved: AdminProduct) => {
    setData(prev => {
      const existing = prev ?? [];
      const index = existing.findIndex(p => p.id === saved.id);
      if (index === -1) return [...existing, saved];
      const next = [...existing];
      next[index] = saved;
      return next;
    });
    setModalProduct(null);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Products</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search products…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{products.length} total</span>
          <button
            type="button"
            onClick={() => setModalProduct('new')}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Product
          </button>
        </div>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}
      {deleteError && <p className="text-red-600 text-sm">{deleteError}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">SKU</th>
              <th className="px-4 py-3">Category</th>
              <th className="px-4 py-3">Price</th>
              <th className="px-4 py-3">Stock</th>
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
                  {search ? 'No products match your search' : 'No products found'}
                </td>
              </tr>
            ) : filtered.map(product => (
              <tr key={product.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 font-medium text-gray-900">{product.name}</td>
                <td className="px-4 py-3 text-gray-600">{product.sku ?? '—'}</td>
                <td className="px-4 py-3 text-gray-600">{product.category?.name ?? '—'}</td>
                <td className="px-4 py-3 text-gray-600">₹{Number(product.price).toFixed(2)}</td>
                <td className="px-4 py-3 text-gray-600">{product.stockQuantity ?? '—'}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${product.isActive !== false ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {product.isActive !== false ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setModalProduct(product)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-800 border border-primary-200 hover:border-primary-400 rounded-lg px-3 py-1 transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => setImagesProduct(product)}
                      className="text-xs font-medium text-gray-600 hover:text-gray-800 border border-gray-200 hover:border-gray-400 rounded-lg px-3 py-1 transition-colors"
                    >
                      Images
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(product)}
                      disabled={deleting === product.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {deleting === product.id ? 'Deleting…' : 'Delete'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modalProduct && (
        <ProductFormModal
          product={modalProduct === 'new' ? null : modalProduct}
          categories={categories}
          onClose={() => setModalProduct(null)}
          onSaved={handleSaved}
        />
      )}

      {imagesProduct && (
        <ProductImagesModal product={imagesProduct} onClose={() => setImagesProduct(null)} />
      )}
    </div>
  );
}
