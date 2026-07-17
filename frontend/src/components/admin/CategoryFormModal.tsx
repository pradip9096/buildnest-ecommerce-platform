import { useState } from 'react';
import {
  createAdminCategory,
  updateAdminCategory,
  type AdminCategory,
  type CategoryFormInput,
} from '../../api/admin';

interface Props {
  /** null for create, an existing category for edit. */
  category: AdminCategory | null;
  /** Full flat category list, used to populate the parent selector. */
  allCategories: AdminCategory[];
  onClose: () => void;
  onSaved: (category: AdminCategory) => void;
}

/** IDs of `root` and everything transitively parented under it — excluded from its own parent options. */
function descendantIds(rootId: number, categories: AdminCategory[]): Set<number> {
  const childrenOf = new Map<number, number[]>();
  for (const c of categories) {
    const parentId = c.parentCategory?.id;
    if (parentId == null) continue;
    childrenOf.set(parentId, [...(childrenOf.get(parentId) ?? []), c.id]);
  }
  const result = new Set<number>([rootId]);
  const stack = [...(childrenOf.get(rootId) ?? [])];
  while (stack.length > 0) {
    const id = stack.pop()!;
    if (result.has(id)) continue;
    result.add(id);
    stack.push(...(childrenOf.get(id) ?? []));
  }
  return result;
}

export function CategoryFormModal({ category, allCategories, onClose, onSaved }: Props) {
  const isEdit = category !== null;
  const [name, setName] = useState(category?.name ?? '');
  const [description, setDescription] = useState(category?.description ?? '');
  const [imageUrl, setImageUrl] = useState(category?.imageUrl ?? '');
  const [parentId, setParentId] = useState<string>(
    category?.parentCategory?.id != null ? String(category.parentCategory.id) : ''
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const excluded = category ? descendantIds(category.id, allCategories) : new Set<number>();
  const parentOptions = allCategories.filter(c => !excluded.has(c.id));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim().length < 2) {
      setError('Category name must be at least 2 characters.');
      return;
    }
    setLoading(true);
    setError(null);

    const input: CategoryFormInput = {
      name: name.trim(),
      description: description.trim() || undefined,
      imageUrl: imageUrl.trim() || undefined,
      parentId: parentId ? Number(parentId) : null,
    };

    try {
      const saved = isEdit
        ? await updateAdminCategory(category.id, input)
        : await createAdminCategory(input);
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save category');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">{isEdit ? 'Edit Category' : 'New Category'}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="category-name" className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              id="category-name"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Power Tools"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
              minLength={2}
              maxLength={255}
            />
          </div>

          <div>
            <label htmlFor="category-description" className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              id="category-description"
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Optional description"
              rows={3}
              maxLength={2000}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            />
          </div>

          <div>
            <label htmlFor="category-image-url" className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <input
              id="category-image-url"
              type="text"
              value={imageUrl}
              onChange={e => setImageUrl(e.target.value)}
              placeholder="https://…"
              maxLength={500}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            />
          </div>

          <div>
            <label htmlFor="category-parent" className="block text-sm font-medium text-gray-700 mb-1">Parent Category</label>
            <select
              id="category-parent"
              value={parentId}
              onChange={e => setParentId(e.target.value)}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
            >
              <option value="">— None (top-level) —</option>
              {parentOptions.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
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
