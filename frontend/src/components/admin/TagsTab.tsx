import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import { fetchAdminTags, deleteAdminTag, type AdminTag } from '../../api/admin';
import { TagFormModal } from './TagFormModal';

export function TagsTab() {
  const { data, loading, error, setData } = useAsync<AdminTag[]>(() => fetchAdminTags(), []);
  const tags = data ?? [];
  const [search, setSearch] = useState('');
  const [deleting, setDeleting] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [modalTag, setModalTag] = useState<AdminTag | 'new' | null>(null);

  const filtered = search
    ? tags.filter(t => t.name.toLowerCase().includes(search.toLowerCase()))
    : tags;

  const handleDelete = async (tag: AdminTag) => {
    if (!confirm(`Delete tag "${tag.name}"? This cannot be undone.`)) return;
    setDeleting(tag.id);
    setDeleteError(null);
    try {
      await deleteAdminTag(tag.id);
      setData(prev => (prev ?? []).filter(t => t.id !== tag.id));
    } catch (e) {
      setDeleteError(e instanceof Error ? e.message : 'Failed to delete tag');
    } finally {
      setDeleting(null);
    }
  };

  const handleSaved = (saved: AdminTag) => {
    setData(prev => {
      const existing = prev ?? [];
      const index = existing.findIndex(t => t.id === saved.id);
      if (index === -1) return [...existing, saved];
      const next = [...existing];
      next[index] = saved;
      return next;
    });
    setModalTag(null);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Tags</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search tags…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{tags.length} total</span>
          <button
            type="button"
            onClick={() => setModalTag('new')}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Tag
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
              <th className="px-4 py-3">Slug</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(3)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No tags match your search' : 'No tags found'}
                </td>
              </tr>
            ) : filtered.map(tag => (
              <tr key={tag.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 font-medium text-gray-900">{tag.name}</td>
                <td className="px-4 py-3 text-gray-500">{tag.slug}</td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setModalTag(tag)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-800 border border-primary-200 hover:border-primary-400 rounded-lg px-3 py-1 transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(tag)}
                      disabled={deleting === tag.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {deleting === tag.id ? 'Deleting…' : 'Delete'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modalTag && (
        <TagFormModal
          tag={modalTag === 'new' ? null : modalTag}
          onClose={() => setModalTag(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
