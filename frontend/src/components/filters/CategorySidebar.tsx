import type { Category } from '../../types';

interface Props {
  categories: Category[];
  selected: number[];
  onChange: (ids: number[]) => void;
  loading: boolean;
}

export function CategorySidebar({ categories, selected, onChange, loading }: Props) {
  function toggle(id: number) {
    onChange(
      selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id]
    );
  }

  return (
    <aside className="w-full lg:w-56 shrink-0">
      <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-3">
        Categories
      </h2>

      {loading ? (
        <div className="space-y-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-5 bg-gray-200 rounded animate-pulse" />
          ))}
        </div>
      ) : (
        <ul className="space-y-1">
          {categories.map(cat => (
            <li key={cat.id}>
              <label className="flex items-center gap-2 cursor-pointer group">
                <input
                  type="checkbox"
                  checked={selected.includes(cat.id)}
                  onChange={() => toggle(cat.id)}
                  className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span className="text-sm text-gray-700 group-hover:text-gray-900">
                  {cat.name}
                </span>
              </label>
            </li>
          ))}
          {categories.length === 0 && (
            <li className="text-sm text-gray-600">No categories</li>
          )}
        </ul>
      )}

      {selected.length > 0 && (
        <button
          type="button"
          onClick={() => onChange([])}
          className="mt-4 text-xs text-primary-600 hover:underline"
        >
          Clear filters
        </button>
      )}
    </aside>
  );
}
