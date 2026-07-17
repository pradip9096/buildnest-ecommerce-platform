import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import { fetchAdminCategories, deleteAdminCategory, type AdminCategory } from '../../api/admin';
import { CategoryFormModal } from './CategoryFormModal';

interface CategoryRow {
  category: AdminCategory;
  depth: number;
}

function buildHierarchy(categories: AdminCategory[]): CategoryRow[] {
  const byParent = new Map<number | null, AdminCategory[]>();
  for (const category of categories) {
    const parentId = category.parentCategory?.id ?? null;
    const siblings = byParent.get(parentId) ?? [];
    siblings.push(category);
    byParent.set(parentId, siblings);
  }

  const rows: CategoryRow[] = [];
  const visit = (parentId: number | null, depth: number, seen: Set<number>) => {
    const children = [...(byParent.get(parentId) ?? [])].sort((a, b) => a.name.localeCompare(b.name));
    for (const category of children) {
      if (seen.has(category.id)) continue; // defensive: never loop on a data-integrity cycle
      rows.push({ category, depth });
      visit(category.id, depth + 1, new Set(seen).add(category.id));
    }
  };
  visit(null, 0, new Set());

  // Any category whose parent id doesn't resolve to a row above (e.g. parent
  // filtered out by a race) still needs to be listed, at depth 0.
  const listedIds = new Set(rows.map(r => r.category.id));
  for (const category of categories) {
    if (!listedIds.has(category.id)) rows.push({ category, depth: 0 });
  }

  return rows;
}

export function CategoriesTab() {
  const { data, loading, error, setData } = useAsync<AdminCategory[]>(() => fetchAdminCategories(), []);
  const categories = data ?? [];
  const [search, setSearch] = useState('');
  const [deleting, setDeleting] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [modalCategory, setModalCategory] = useState<AdminCategory | 'new' | null>(null);

  const filtered = search
    ? categories.filter(c => c.name.toLowerCase().includes(search.toLowerCase()))
    : categories;
  const rows = buildHierarchy(filtered);

  const handleDelete = async (category: AdminCategory) => {
    if (!confirm(`Delete category "${category.name}"? This cannot be undone.`)) return;
    setDeleting(category.id);
    setDeleteError(null);
    try {
      await deleteAdminCategory(category.id);
      setData(prev => (prev ?? []).filter(c => c.id !== category.id));
    } catch (e) {
      setDeleteError(e instanceof Error ? e.message : 'Failed to delete category');
    } finally {
      setDeleting(null);
    }
  };

  const handleSaved = (saved: AdminCategory) => {
    setData(prev => {
      const existing = prev ?? [];
      const index = existing.findIndex(c => c.id === saved.id);
      if (index === -1) return [...existing, saved];
      const next = [...existing];
      next[index] = saved;
      return next;
    });
    setModalCategory(null);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Categories</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search categories…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{categories.length} total</span>
          <button
            type="button"
            onClick={() => setModalCategory('new')}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Category
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
              <th className="px-4 py-3">Description</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(4)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : rows.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No categories match your search' : 'No categories found'}
                </td>
              </tr>
            ) : rows.map(({ category, depth }) => (
              <tr key={category.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <span style={{ paddingLeft: `${depth * 1.25}rem` }} className="font-medium text-gray-900">
                    {depth > 0 && <span className="text-gray-300 mr-1">└</span>}
                    {category.name}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-600 max-w-xs truncate">{category.description ?? '—'}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${category.isActive !== false ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {category.isActive !== false ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setModalCategory(category)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-800 border border-primary-200 hover:border-primary-400 rounded-lg px-3 py-1 transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(category)}
                      disabled={deleting === category.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {deleting === category.id ? 'Deleting…' : 'Delete'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modalCategory && (
        <CategoryFormModal
          category={modalCategory === 'new' ? null : modalCategory}
          allCategories={categories}
          onClose={() => setModalCategory(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
