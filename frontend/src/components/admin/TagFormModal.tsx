import { useState } from 'react';
import { createAdminTag, updateAdminTag, type AdminTag, type TagFormInput } from '../../api/admin';

interface Props {
  /** null for create, an existing tag for edit. */
  tag: AdminTag | null;
  onClose: () => void;
  onSaved: (tag: AdminTag) => void;
}

export function TagFormModal({ tag, onClose, onSaved }: Props) {
  const isEdit = tag !== null;
  const [name, setName] = useState(tag?.name ?? '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim().length < 2) {
      setError('Tag name must be at least 2 characters.');
      return;
    }
    setLoading(true);
    setError(null);

    const input: TagFormInput = { name: name.trim() };

    try {
      const saved = isEdit ? await updateAdminTag(tag.id, input) : await createAdminTag(input);
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save tag');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">{isEdit ? 'Edit Tag' : 'New Tag'}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="tag-name" className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              id="tag-name"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Eco-Friendly"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              required
              minLength={2}
              maxLength={100}
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
              {loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
